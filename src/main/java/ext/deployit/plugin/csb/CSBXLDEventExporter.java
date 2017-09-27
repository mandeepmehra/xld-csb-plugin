package ext.deployit.plugin.csb;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.engine.spi.event.AuditableDeployitEvent;
import com.xebialabs.deployit.engine.spi.event.DeployitEventListener;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.repository.RepositoryServiceHolder;
import com.xebialabs.deployit.repository.SearchParameters;

import ext.deployit.plugin.csb.config.CSBConfig;
import ext.deployit.plugin.csb.domain.CSBLogEntry;
import ext.deployit.plugin.csb.util.PingURL;
import ext.deployit.plugin.csb.util.XLUtil;

@DeployitEventListener
public class CSBXLDEventExporter implements EventExporter {

	private static final Logger logger = LoggerFactory.getLogger(CSBXLDEventExporter.class);
	private final static int CSB_TIMEOUT_MS = 5000;
	private String CSB_URL;
	private String clientName;
	private String CSB_API_TOKEN;
	private final static String CSB_SUCCESS_RESPONSE = "{\"success\":true}";
	private final static String CSB_FEATURE_API = "/api/v1_1/feature";

	private static final String STARTED_TASK = "Started task";
	private static final String CANCELLED_TASK = "Cancelled task";
	private static final String ARCHIVED_TASK = "Archived task";
	private final CSBFileExporter csbFileExporter = new CSBFileExporter();

	public void intializeCSBParams() throws CSBLogException {
		final ConfigurationItem csbConfig = getCSBConfig();
		if (csbConfig == null) {
			throw new CSBLogException("csb.CSBConfig not initialized");
		}
		clientName = csbConfig.getProperty("customerName");
		CSB_URL = csbConfig.getProperty("url");
		CSB_API_TOKEN = csbConfig.getProperty("authToken");

	}

	public ConfigurationItem getCSBConfig() {
		List<ConfigurationItem> configItems = RepositoryServiceHolder.getRepositoryService()
				.listEntities(new SearchParameters().setType(Type.valueOf(CSBConfig.class)));
		ConfigurationItem csbConfig = null;
		for (ConfigurationItem config : configItems) {
			csbConfig = config;
			break;
		}
		return csbConfig;
	}

	@Override
	public void exportEvent(AuditableDeployitEvent event) throws CSBLogException {

		intializeCSBParams();

		CSBLogEntry logEntry = getCSBLogEntryInstance(event);

		try {
			if (isCSBAPIReachable(CSB_URL, CSB_TIMEOUT_MS)) {
				sendToCSBWeb(logEntry);
			} else {
				sendToCSBFile(logEntry);
			}
		} catch (CSBLogException ex) {
			logger.info("API not reachable, logging deploy event to file");
			sendToCSBFile(logEntry);
		}

	}

	private void sendToCSBFile(final CSBLogEntry logEntry) {
		logger.info("Logging deploy event to file");
		csbFileExporter.logEntry(logEntry);
	}

	private void sendToCSBWeb(final CSBLogEntry logEntry) throws CSBLogException {
		try {
			logger.info("Sending Deploy event to CSB API : {}{}", CSB_URL, CSB_FEATURE_API);

			Client client = ClientBuilder.newClient();

			String payload = "{\"account_id\": \"" + logEntry.getClientName() + "\",  \"user_id\": \""
					+ logEntry.getUserId() + "\",  \"product_id\": \"" + logEntry.getProduct() + "\",  \"module_id\":\""
					+ logEntry.getModule() + "\",  \"feature_id\": \"" + logEntry.getFeature()
					+ "\",  \"timestamp\": \"" + logEntry.getTimestamp() + "\"}";

			logger.debug("CSB payload : {}", payload);

			Entity<String> payloadEntity = Entity.json(payload);
			Response response = client.target(CSB_URL + CSB_FEATURE_API).request(MediaType.APPLICATION_JSON_TYPE)
					.header("Authorization", "Bearer " + CSB_API_TOKEN).post(payloadEntity);

			String responseBody = response.readEntity(String.class);
			logger.debug("CSB response Status : {}, Body : {}", response.getStatus(), responseBody);

			if (response.getStatus() != 200)
				throw new CSBLogException("Invalid response code received : " + response.getStatus());
			else if (!responseBody.equalsIgnoreCase(CSB_SUCCESS_RESPONSE))
				throw new CSBLogException("Invalid response success message : " + responseBody);

		} catch (Exception ex) {
			throw new CSBLogException(ex.getMessage());
		}
	}

	private boolean isCSBAPIReachable(String url, int timeout) {
		return PingURL.pingURL(url, timeout);
	}

	public CSBLogEntry getCSBLogEntryInstance(final AuditableDeployitEvent event) throws CSBLogException{
		CSBLogEntry csbLogEntry = new CSBLogEntry();

		String eventType = null;
		if (event.message.startsWith(STARTED_TASK))
			eventType = "Deployment Initiated";
		else if (event.message.startsWith(ARCHIVED_TASK))
			eventType = "Deployment Completed";
		else if (event.message.startsWith(CANCELLED_TASK))
			eventType = "Deploynent Aborted";
		else
			throw new CSBLogException("Unkown event type :" + event.message);

		csbLogEntry.setUserId(event.username);
		csbLogEntry.setProduct("XL Deploy");
		csbLogEntry.setModule("Deploy");
		csbLogEntry.setFeature(eventType);
		csbLogEntry.setTimestamp(XLUtil.getCurrentTimeStampInGMT());
		csbLogEntry.setClientName(clientName);

		return csbLogEntry;
	}

}

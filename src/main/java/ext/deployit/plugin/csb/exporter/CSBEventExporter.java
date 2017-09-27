package ext.deployit.plugin.csb.exporter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.engine.spi.event.AuditableDeployitEvent;
import com.xebialabs.deployit.engine.spi.event.DeployitEventListener;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.repository.RepositoryServiceHolder;
import com.xebialabs.deployit.repository.SearchParameters;

import ext.deployit.plugin.csb.config.CSBConfig;
import ext.deployit.plugin.csb.domain.CSBConfiguration;
import ext.deployit.plugin.csb.domain.CSBLogEntry;
import ext.deployit.plugin.csb.exception.CSBPluginException;
import ext.deployit.plugin.csb.util.PingURL;
import ext.deployit.plugin.csb.util.XLUtil;

@DeployitEventListener
public class CSBEventExporter {

	private static final Logger logger = LoggerFactory.getLogger(CSBEventExporter.class);
	private final static int CSB_TIMEOUT_MS = 5000;

	private static final String STARTED_TASK = "Started task";
	private static final String CANCELLED_TASK = "Cancelled task";
	private static final String ARCHIVED_TASK = "Archived task";

	private final CSBFileExporter csbFileExporter = new CSBFileExporter();
	private final CSBWebExporter csbWebExporter = new CSBWebExporter();

	public CSBConfiguration intializeCSBConfiguration() throws CSBPluginException {
		final ConfigurationItem csbConfig = getCSBConfig();
		CSBConfiguration csbConfiguration = new CSBConfiguration();

		if (csbConfig == null) {
			throw new CSBPluginException("csb.CSBConfig not initialized");
		}
		csbConfiguration.setClientName(csbConfig.getProperty("customerName"));
		csbConfiguration.setUrl(csbConfig.getProperty("url"));
		csbConfiguration.setAuthToken(csbConfig.getProperty("authToken"));

		return csbConfiguration;
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

	public void exportEvent(AuditableDeployitEvent event) throws CSBPluginException {

		final CSBConfiguration csbConfiguration = intializeCSBConfiguration();
		final CSBLogEntry logEntry = getCSBLogEntryInstance(csbConfiguration, event);

		try {
			if (isCSBAPIReachable(csbConfiguration.getUrl(), CSB_TIMEOUT_MS)) {
				sendToCSBWeb(csbConfiguration, logEntry);
			} else {
				sendToCSBFile(csbConfiguration, logEntry);
			}
		} catch (CSBPluginException ex) {
			logger.error("Error posting to web", ex);
			logger.info("API not reachable or error occurred, logging deploy event to file");
			sendToCSBFile(csbConfiguration, logEntry);
		}

	}

	private void sendToCSBFile(final CSBConfiguration csbConfiguration, final CSBLogEntry logEntry) {
		logger.info("Logging deploy event to file");
		csbFileExporter.logEntry(logEntry);
	}

	private void sendToCSBWeb(final CSBConfiguration csbConfiguration, final CSBLogEntry logEntry)
			throws CSBPluginException {
		logger.info("Sending deploy event to :" + csbConfiguration.getUrl());
		csbWebExporter.logEntry(csbConfiguration, logEntry);
	}

	private boolean isCSBAPIReachable(String url, int timeout) {
		return PingURL.pingURL(url, timeout);
	}

	public CSBLogEntry getCSBLogEntryInstance(final CSBConfiguration csbConfiguration,
			final AuditableDeployitEvent event) throws CSBPluginException {
		CSBLogEntry csbLogEntry = new CSBLogEntry();

		String eventType = null;
		if (event.message.startsWith(STARTED_TASK))
			eventType = "Deployment Initiated";
		else if (event.message.startsWith(ARCHIVED_TASK))
			eventType = "Deployment Completed";
		else if (event.message.startsWith(CANCELLED_TASK))
			eventType = "Deploynent Aborted";
		else
			throw new CSBPluginException("Unkown event type :" + event.message);

		csbLogEntry.setUserId(event.username);
		csbLogEntry.setProduct("XL Deploy");
		csbLogEntry.setModule("Deploy");
		csbLogEntry.setFeature(eventType);
		csbLogEntry.setTimestamp(XLUtil.getCurrentTimeStampInGMT());
		csbLogEntry.setClientName(csbConfiguration.getClientName());

		return csbLogEntry;
	}

}

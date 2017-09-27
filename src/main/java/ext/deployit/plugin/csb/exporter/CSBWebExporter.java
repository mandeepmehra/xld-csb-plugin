package ext.deployit.plugin.csb.exporter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ext.deployit.plugin.csb.domain.CSBConfiguration;
import ext.deployit.plugin.csb.domain.CSBLogEntry;
import ext.deployit.plugin.csb.exception.CSBPluginException;

public class CSBWebExporter {

	private static final Logger logger = LoggerFactory.getLogger(CSBWebExporter.class);
	private final static String CSB_SUCCESS_RESPONSE = "{\"success\":true}";
	private final static String API_ENDPOINT = "/api/v1_1/feature";

	public void logEntry(final CSBConfiguration csbConfiguration, final CSBLogEntry logEntry)
			throws CSBPluginException {
		Client webClient = null;
		try {

			webClient = ClientBuilder.newClient();

			String payload = "{\"account_id\": \"" + logEntry.getClientName() + "\",  \"user_id\": \""
					+ logEntry.getUserId() + "\",  \"product_id\": \"" + logEntry.getProduct() + "\",  \"module_id\":\""
					+ logEntry.getModule() + "\",  \"feature_id\": \"" + logEntry.getFeature()
					+ "\",  \"timestamp\": \"" + logEntry.getTimestamp() + "\"}";

			logger.debug("CSB payload : {}", payload);

			Entity<String> payloadEntity = Entity.json(payload);
			Response response = webClient.target(csbConfiguration.getUrl() + API_ENDPOINT)
					.request(MediaType.APPLICATION_JSON_TYPE)
					.header("Authorization", "Bearer " + csbConfiguration.getAuthToken()).post(payloadEntity);

			String responseBody = response.readEntity(String.class);
			logger.debug("CSB response Status : {}, Body : {}", response.getStatus(), responseBody);

			if (response.getStatus() != 200)
				throw new CSBPluginException("Invalid response code received : " + response.getStatus());
			else if (!responseBody.equalsIgnoreCase(CSB_SUCCESS_RESPONSE))
				throw new CSBPluginException("Invalid response success message : " + responseBody);

		} catch (Exception ex) {
			throw new CSBPluginException(ex.getMessage());
		} finally {
			try {
				if (webClient != null)
					webClient.close();
			} catch (Exception ex) {
				logger.error("Error in closing web client");
			}
		}
	}
}

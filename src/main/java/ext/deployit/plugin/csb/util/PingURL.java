package ext.deployit.plugin.csb.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingURL {
	private static final Logger logger = LoggerFactory.getLogger(PingURL.class);

	public static boolean pingURL(String url, int timeout) {

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			logger.debug("Response code for url {} : {}", url, responseCode);
			return (200 <= responseCode && responseCode <= 399);
		} catch (IOException exception) {
			logger.error("Could not reach url {} in defined timeout of {}", url, timeout);
			return false;
		}
	}

}
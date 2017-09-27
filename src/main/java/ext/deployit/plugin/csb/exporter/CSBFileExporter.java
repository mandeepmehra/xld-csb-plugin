package ext.deployit.plugin.csb.exporter;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ext.deployit.plugin.csb.domain.CSBLogEntry;

public class CSBFileExporter {

	private static final Logger logger = LoggerFactory.getLogger(CSBFileExporter.class);

	public void logEntry(final CSBLogEntry logEntry) {
		MDC.put("CSB_CLIENT_NAME", logEntry.getClientName());
		MDC.put("CSB_USER", logEntry.getUserId());
		MDC.put("CSB_PRODUCT", logEntry.getProduct());
		MDC.put("CSB_MODULE", logEntry.getModule());
		MDC.put("CSB_FEATURE", logEntry.getFeature());
		MDC.put("CSB_TIMESTAMP", logEntry.getTimestamp());
		logger.info("Release event logged to file");
	}
}

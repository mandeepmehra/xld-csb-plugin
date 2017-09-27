package ext.deployit.plugin.csb.eventhandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.engine.spi.event.AuditableDeployitEvent;

import ext.deployit.plugin.csb.exception.CSBPluginException;
import ext.deployit.plugin.csb.exporter.CSBEventExporter;

public class CSBDeployEventHandler implements EventHandler {

	private static final Logger logger = LoggerFactory.getLogger(CSBDeployEventHandler.class);

	private final CSBEventExporter eventExporter = new CSBEventExporter();
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

	@Override
	public void handleEvent(final AuditableDeployitEvent event) throws CSBPluginException {

		logger.info("Exporting deploy event {}", event.message);
		if (!isDeployEvent(event))
			return;

		// TODO: Start in new thread
		try {
			eventExporter.exportEvent(event);
		} catch (CSBPluginException ex) {
			logger.error("Error in exporting release", ex);
		}
	}

	public boolean isDeployEvent(final AuditableDeployitEvent event) {
		if (event.component.equals("engine") && event.message.indexOf("task") > 0
				&& !event.message.startsWith("Created task")) {
			return true;
		}
		return false;
	}

}

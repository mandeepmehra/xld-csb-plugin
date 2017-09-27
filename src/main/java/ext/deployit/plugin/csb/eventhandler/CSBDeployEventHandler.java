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

		if (!isDeployEvent(event))
			return;

		logger.info("Exporting deploy event {}", event.message);

		EXECUTOR_SERVICE.submit(new Runnable() {
			public void run() {
				try {
					eventExporter.exportEvent(event);
				} catch (CSBPluginException exception) {
					logger.error("Exception trying to export event: {}", exception);
				} catch (Exception exception) {
					logger.error("Generic Exception trying to export event: {}", event.message, exception);
				}
			}
		});

	}

	public boolean isDeployEvent(final AuditableDeployitEvent event) {
		if (event.component.equals("engine") && event.message.indexOf("task") > 0
				&& !event.message.startsWith("Created task")) {
			return true;
		}
		return false;
	}

}

package ext.deployit.plugin.csb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.engine.spi.event.AuditableDeployitEvent;
import com.xebialabs.deployit.engine.spi.event.DeployitEventListener;

import ext.deployit.plugin.csb.exception.CSBPluginException;
import ext.deployit.plugin.csb.exporter.CSBEventExporter;
import nl.javadude.t2bus.Subscribe;

@DeployitEventListener
public class XLDEventListener {

	private final CSBEventExporter exporter = new CSBEventExporter();

	private static final Logger logger = LoggerFactory.getLogger(XLDEventListener.class);

	@Subscribe
	public void deployitEvent(AuditableDeployitEvent event) {

		try {

			exporter.exportEvent(event);

		} catch (CSBPluginException ex) {
			logger.error("Error exporting event", ex);
		}
	}

}

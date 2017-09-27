package ext.deployit.plugin.csb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.engine.spi.event.AuditableDeployitEvent;
import com.xebialabs.deployit.engine.spi.event.DeployitEventListener;

import nl.javadude.t2bus.Subscribe;

@DeployitEventListener
public class XLDEventListener {

	private final EventExporter exporter = new CSBXLDEventExporter();

	private static final Logger logger = LoggerFactory.getLogger(XLDEventListener.class);

	@Subscribe
	public void deployitEvent(AuditableDeployitEvent event) {

		try {
			if (isDeployEvent(event))
				exporter.exportEvent(event);

		} catch (CSBLogException ex) {
			logger.error("Error exporting event", ex);
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

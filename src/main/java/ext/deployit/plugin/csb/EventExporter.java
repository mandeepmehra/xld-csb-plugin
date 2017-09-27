package ext.deployit.plugin.csb;

import com.xebialabs.deployit.engine.spi.event.AuditableDeployitEvent;

public interface EventExporter {
	public void exportEvent(AuditableDeployitEvent event) throws CSBLogException;
}

package ext.deployit.plugin.csb.eventhandler;

import com.xebialabs.deployit.engine.spi.event.AuditableDeployitEvent;

import ext.deployit.plugin.csb.exception.CSBPluginException;

public interface EventHandler {
	public void handleEvent(AuditableDeployitEvent event) throws CSBPluginException;
}

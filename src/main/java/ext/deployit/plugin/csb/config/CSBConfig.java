package ext.deployit.plugin.csb.config;

import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;
import com.xebialabs.deployit.plugin.api.udm.base.BaseConfigurationItem;

@SuppressWarnings("serial")
@Metadata(root = Metadata.ConfigurationItemRoot.CONFIGURATION, description = "Attributes to connect to CSB server to export events")

public class CSBConfig extends BaseConfigurationItem {

	@Property
	private String url;

	@Property
	private String customerName;

	@Property
	private String authToken;

	public String getUrl() {
		return url;
	}

	public String getAuthToken() {
		return authToken;
	}

	public String getCustomerName() {
		return customerName;
	}

}

package hudson.plugins.sfee;

import hudson.Plugin;
import hudson.security.SecurityRealm;

/**
 * @plugin
 */
public class PluginImpl extends Plugin {
	public void start() throws Exception {
		SecurityRealm.LIST.add(SFEESecurityRealm.INSTANCE);
	}
}

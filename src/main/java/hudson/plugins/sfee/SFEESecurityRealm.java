package hudson.plugins.sfee;

import hudson.model.Descriptor;
import hudson.security.SecurityRealm;

import org.acegisecurity.AuthenticationManager;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * 
 * @author huybrechts
 *
 */
public class SFEESecurityRealm extends SecurityRealm {

	private String host;

	@DataBoundConstructor
	public SFEESecurityRealm(String host) {
		this.host = host;
	}
	
	@Override
	public AuthenticationManager createAuthenticationManager() {
		return new SFEEAuthenticationManager(host);
	}

	public Descriptor<SecurityRealm> getDescriptor() {
		return INSTANCE;
	}

	public static final DescriptorImpl INSTANCE = new DescriptorImpl();

	public static final class DescriptorImpl extends Descriptor<SecurityRealm> {

		private String host;
		
		private DescriptorImpl() {
			super(SFEESecurityRealm.class);
			load();
		}

        public SFEESecurityRealm newInstance(StaplerRequest req) throws FormException {
            return req.bindParameters(SFEESecurityRealm.class,"sfee.");
        }

        public String getDisplayName() {
			return "SFEE user database";
		}

		public String getHelpFile() {
			return null;
		}

		@Override
		public boolean configure(StaplerRequest req) throws FormException {
			host = req.getParameter("sfee.host");
			save();
			return super.configure(req);
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}
	}

}

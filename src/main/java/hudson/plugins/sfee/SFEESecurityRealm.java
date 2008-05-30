package hudson.plugins.sfee;

import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.security.SecurityRealm;
import hudson.util.Scrambler;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.providers.rememberme.RememberMeAuthenticationProvider;
import org.acegisecurity.userdetails.UserDetailsService;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author huybrechts
 */
public class SFEESecurityRealm extends SecurityRealm {

	public SFEESecurityRealm() {
	}

	@Override
	public SecurityComponents createSecurityComponents() {
		ProviderManager manager = new ProviderManager();
		UserDetailsService userDetailsService = new CachingUserDetailsService(new SFEEUserDetailsService());
		RememberMeAuthenticationProvider rememberMeAuthenticationProvider = new RememberMeAuthenticationProvider();
		rememberMeAuthenticationProvider.setKey(Hudson.getInstance()
				.getSecretKey());
		manager.setProviders(Arrays.asList(
				(AuthenticationProvider) rememberMeAuthenticationProvider,
				(AuthenticationProvider) new SFEEAuthenticationManager(
						userDetailsService)));
		return new SecurityComponents(manager, userDetailsService);
	}

	public Descriptor<SecurityRealm> getDescriptor() {
		return DESCRIPTOR;
	}

	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static final class DescriptorImpl extends Descriptor<SecurityRealm> {

		private Map<String, String> passwords = new ConcurrentHashMap<String, String>();

		private DescriptorImpl() {
			super(SFEESecurityRealm.class);
			load();
		}

		public SFEESecurityRealm newInstance(StaplerRequest req)
				throws FormException {
			return new SFEESecurityRealm();
		}

		public String getDisplayName() {
			return "SFEE User Database";
		}

		public String getPassword(String user) {
			String result = passwords.get(user);
			return result != null ? Scrambler.descramble(result) : result;
		}

		public void setPassword(String user, String password) {
			passwords.put(user, Scrambler.scramble(password));
			save();
		}

		public String getHelpFile() {
			return null;
		}
	}

}

package hudson.plugins.sfee;

import hudson.Extension;
import org.acegisecurity.Authentication;
import org.kohsuke.args4j.Option;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.AuthenticationException;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.security.*;
import hudson.util.Scrambler;
import hudson.FilePath;
import hudson.cli.CLICommand;
import hudson.remoting.Callable;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;

import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.providers.rememberme.RememberMeAuthenticationProvider;
import org.acegisecurity.userdetails.UserDetailsService;
import org.kohsuke.stapler.StaplerRequest;

import hudson.security.CliAuthenticator;
import hudson.cli.CLICommand;

/**
 * @author huybrechts
 */
public class SFEESecurityRealm extends SecurityRealm {

	public SFEESecurityRealm() {
	}

	@Override
	public SecurityComponents createSecurityComponents() {
		ProviderManager manager = new ProviderManager();
		UserDetailsService userDetailsService = new CachingUserDetailsService(
				new SFEEUserDetailsService());
		RememberMeAuthenticationProvider rememberMeAuthenticationProvider = new RememberMeAuthenticationProvider();
		rememberMeAuthenticationProvider.setKey(Hudson.getInstance()
				.getSecretKey());
		manager.setProviders(Arrays.asList(
				(AuthenticationProvider) rememberMeAuthenticationProvider,
				(AuthenticationProvider) new SFEEAuthenticationManager(
						userDetailsService)));
		return new SecurityComponents(manager, userDetailsService);
	}
	
    @Override
    public CliAuthenticator createCliAuthenticator(final CLICommand command) {
        return new CliAuthenticator() {
            @Option(name="--username",usage="User name to authenticate yourself to Hudson")
            public String userName;

            @Option(name="--password",usage="Password for authentication. Note that passing a password in arguments is insecure.")
            public String password;

            @Option(name="--password-file",usage="File that contains the password")
            public String passwordFile;

            public Authentication authenticate() throws AuthenticationException, IOException, InterruptedException {
                if (userName==null) {
                	return Hudson.ANONYMOUS;    // no authentication parameter. run as anonymous
                }

                if (passwordFile!=null)
                    try {
                        password = new FilePath(command.channel,passwordFile).readToString().trim();
                    } catch (IOException e) {
                        throw new BadCredentialsException("Failed to read "+passwordFile,e);
                    }
                if (password==null)
                    password = command.channel.call(new InteractivelyAskForPassword());

                if (password==null)
                    throw new BadCredentialsException("No password specified");

                return getSecurityComponents().manager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
            }
        };
    }


	@Override
	public Descriptor<SecurityRealm> getDescriptor() {
		return DESCRIPTOR;
	}

	@Extension
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

		@Override
		public String getHelpFile() {
			return null;
		}
	}

    /**
     * Asks for the password.
     */
    private static class InteractivelyAskForPassword implements Callable<String,IOException> {
        public String call() throws IOException {
            Console console = System.console();
            if (console == null)    return null;    // no terminal

            char[] w = console.readPassword("Password:");
            if (w==null)    return null;
            return new String(w);
        }

        private static final long serialVersionUID = 1L;
    }
}

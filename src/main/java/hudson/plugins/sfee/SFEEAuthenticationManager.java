package hudson.plugins.sfee;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.dao.AbstractUserDetailsAuthenticationProvider;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;

public class SFEEAuthenticationManager extends
		AbstractUserDetailsAuthenticationProvider {

	private UserDetailsService userDetailsService;

	public SFEEAuthenticationManager(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
	}

	@Override
	protected UserDetails retrieveUser(String username,
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {

		String password = (String) authentication.getCredentials();

		SFEESecurityRealm.DESCRIPTOR.setPassword(username, password);

		return userDetailsService.loadUserByUsername(username);

	}

	@Override
	protected Authentication createSuccessAuthentication(Object principal,
			Authentication authentication, UserDetails user) {
		return super.createSuccessAuthentication(principal, authentication,
				user);
	}
	
}

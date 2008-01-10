package hudson.plugins.sfee;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

/**
 * @author huybrechts
 */
public class SFEEAuthentication extends UsernamePasswordAuthenticationToken {

	private String sessionId;

	public SFEEAuthentication(Object principal, Object credentials,
			GrantedAuthority[] authorities, String sessionId) {
		super(principal, credentials, authorities);
		this.sessionId = sessionId;
	}

}

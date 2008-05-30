package hudson.plugins.sfee;

import hudson.plugins.sfee.webservice.ProjectSoapRow;
import hudson.plugins.sfee.webservice.UserSoapDO;

import java.util.HashSet;
import java.util.Set;

import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

public class SFEEUserDetailsService implements UserDetailsService {

	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		SourceForgeSite site = SourceForgeSite.DESCRIPTOR.getSite();
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		authorities.add(new GrantedAuthorityImpl("authenticated"));
		
		String password = SFEESecurityRealm.DESCRIPTOR.getPassword(username);
		if (password == null) {
			throw new UsernameNotFoundException("Password not known for this user - please login");
		}
		
		try {
			String sessionId = site.createSession(username, password);
			
			UserSoapDO userDetails = site.getUserDetails(username);
			if (userDetails.isSuperUser()) {
				authorities.add(new GrantedAuthorityImpl("admin"));
			}
			ProjectSoapRow[] projects = site.getProjects(sessionId);
			for (ProjectSoapRow project : projects) {
				authorities.add(new GrantedAuthorityImpl(project.getId()));
			}

			GrantedAuthority[] authoritiesArray = (GrantedAuthority[]) authorities
					.toArray(new GrantedAuthority[authorities
					.size()]);
			
			return new User(username, password, true, true, true, true, authoritiesArray);
		} catch (BadCredentialsException e) {
			throw e;
		} catch (Exception e) {
			throw new DataRetrievalFailureException("SFEE error", e);
		}
	}

}

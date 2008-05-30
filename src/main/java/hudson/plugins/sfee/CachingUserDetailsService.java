package hudson.plugins.sfee;

import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

public class CachingUserDetailsService implements UserDetailsService, Computable<String,UserDetails> {

	private Memoizer<String, UserDetails> memoizer;
	private UserDetailsService service;
	
	public CachingUserDetailsService(UserDetailsService service) {
		this.service = service;

		memoizer = new Memoizer<String, UserDetails>(this);
	}

	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		try {
			return memoizer.compute(username);
		} catch (InterruptedException e) {
			throw new DataRetrievalFailureException("Interrupted", e);
		}
	}

	public UserDetails compute(String arg) throws InterruptedException {
		return service.loadUserByUsername(arg);
	}
	
	
	
}

package hudson.plugins.sfee;

import hudson.model.User;
import hudson.plugins.sfee.webservice.UserSoapRow;
import hudson.tasks.MailAddressResolver;
import hudson.tasks.UserNameResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Resolves email addresses from SFEE IDs.
 * 
 * @TODO sane caching
 *
 */
public class SFEEMailAddressResolver {
	
	private static Logger log = Logger.getLogger(SFEEMailAddressResolver.class.getName());

	private static String UNKNOWN = "unknown";
	
	private final Map<String, String> emails = new HashMap<String, String>();
	private final Map<String, String> names = new HashMap<String, String>();
	
	private final MailAddressResolver mailAddressResolver = new MailAddressResolver() {
		@Override
		public synchronized String findMailAddressFor(User u) {
			String result = emails.get(u.getId());
			if (result == null) {
				update();
				result = emails.get(u.getId());
				if (result == null) {
					emails.put(u.getId(), UNKNOWN);
				}
			}
			if (UNKNOWN.equals(result)) {
				return null;
			} else {
				return result;
			}
		}
	};
	
	private final UserNameResolver userNameResolver = new UserNameResolver() {
		@Override
		public synchronized String findNameFor(User u) {
			String result = names.get(u.getId());
			if (result == null) {
				update();
				result = names.get(u.getId());
				if (result == null) {
					names.put(u.getId(), UNKNOWN);
				}
			}
			if (UNKNOWN.equals(result)) {
				return null;
			} else {
				return result;
			}
		}
	};
	
	public SFEEMailAddressResolver() {
		update();
	}

	private void update() {
		try {
			SourceForgeSite site = SourceForgeSite.DESCRIPTOR.getSite();
			if (site != null) {
				UserSoapRow[] users = site.getUsers();
				for (UserSoapRow user: users) {
					emails.put(user.getUserName(), user.getEmail());
					names.put(user.getUserName(), user.getFullName());
				}
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Error getting users", e);
		}
	}

	public MailAddressResolver getMailAddressResolver() {
		return mailAddressResolver;
	}

	public UserNameResolver getUserNameResolver() {
		return userNameResolver;
	}
	
}

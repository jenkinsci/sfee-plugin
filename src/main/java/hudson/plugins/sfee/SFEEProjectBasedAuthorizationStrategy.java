package hudson.plugins.sfee;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.security.ACL;
import hudson.security.AuthorizationStrategy;
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy;
import hudson.security.SparseACL;
import net.sf.json.JSONObject;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.acls.sid.GrantedAuthoritySid;
import org.kohsuke.stapler.StaplerRequest;

/**
 * {@link AuthorizationStrategy} that grants control based on owned permissions.
 */
public class SFEEProjectBasedAuthorizationStrategy extends
		FullControlOnceLoggedInAuthorizationStrategy {

	public static final SparseACL UNSECURED_PROJECT_ACL = new SparseACL(null);

	static {
		UNSECURED_PROJECT_ACL.add(ACL.ANONYMOUS, Hudson.ADMINISTER, false);
		UNSECURED_PROJECT_ACL.add(ACL.EVERYONE, Hudson.ADMINISTER, true);
	}

	@Override
	public ACL getRootACL() {
		return UNSECURED_PROJECT_ACL;
	}

	public GrantedAuthority createAuthority(Job<?, ?> project) {
		SourceForgeProject p = project.getProperty(SourceForgeProject.class);
		if (p != null) {
			return new GrantedAuthorityImpl(p.getProjectId());
		} else {
			return null;
		}
	}

	@Override
	public ACL getACL(Job<?, ?> project) {
		GrantedAuthority auth = createAuthority(project);
		if (auth != null) {
			SparseACL acl = new SparseACL(getRootACL());
			acl.add(new GrantedAuthoritySid(auth), Hudson.ADMINISTER,
					true);
			return acl;
		} else {
			return UNSECURED_PROJECT_ACL;
		}
	}

	@Override
	public Descriptor<AuthorizationStrategy> getDescriptor() {
		return DESCRIPTOR;
	}

	//@Extension
	public static final Descriptor<AuthorizationStrategy> DESCRIPTOR = new Descriptor<AuthorizationStrategy>(
			FullControlOnceLoggedInAuthorizationStrategy.class) {
		public String getDisplayName() {
			return "SFEE Project Based Access Control";
		}

		@Override
		public AuthorizationStrategy newInstance(StaplerRequest req,
				JSONObject formData) throws FormException {
			return new SFEEProjectBasedAuthorizationStrategy();
		}

		@Override
		public String getHelpFile() {
			return "/help/security/full-control-once-logged-in.html";
		}
	};
}

package hudson.plugins.sfee;

import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.security.AuthorizationStrategy;
import hudson.security.Permission;
import hudson.security.ProjectBasedAuthorizationStrategy;
import hudson.security.SparseACL;
import net.sf.json.JSONObject;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.acls.sid.GrantedAuthoritySid;
import org.kohsuke.stapler.StaplerRequest;

/**
 * {@link AuthorizationStrategy} that grants control based on owned permissions.
 */
public class SFEEProjectBasedAuthorizationStrategy extends ProjectBasedAuthorizationStrategy {
	
    public static final SparseACL UNSECURED_PROJECT_ACL = new SparseACL(ROOT_ACL);

    static {
    	UNSECURED_PROJECT_ACL.add(ACL.ANONYMOUS, Permission.FULL_CONTROL, false);
    	UNSECURED_PROJECT_ACL.add(ACL.EVERYONE, Permission.FULL_CONTROL, true);
    }
	

	public GrantedAuthority createAuthority(AbstractProject<?,?> project) {
    	SourceForgeProject p = project.getProperty(SourceForgeProject.class);
    	if (p != null) {
    		return new GrantedAuthorityImpl(p.getProjectId());
    	} else {
    		return null;
    	}
	}

	@Override
	public ACL getACL(AbstractProject<?, ?> project) {
		GrantedAuthority auth = createAuthority(project);
		if (auth != null) {
			SparseACL acl = new SparseACL(getRootACL());
			acl.add(new GrantedAuthoritySid(auth), Permission.FULL_CONTROL, true);
			return acl;
		} else {
			return UNSECURED_PROJECT_ACL;
		}
	}
    
    public Descriptor<AuthorizationStrategy> getDescriptor() {
        return DESCRIPTOR;
    }

    public static final Descriptor<AuthorizationStrategy> DESCRIPTOR = new Descriptor<AuthorizationStrategy>(ProjectBasedAuthorizationStrategy.class) {
        public String getDisplayName() {
            return "SFEE Project Based Access Control";
        }

        public AuthorizationStrategy newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new SFEEProjectBasedAuthorizationStrategy();
        }

        public String getHelpFile() {
            return "/help/security/full-control-once-logged-in.html";
        }
    };
}

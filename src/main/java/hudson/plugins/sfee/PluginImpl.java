package hudson.plugins.sfee;

import hudson.Plugin;
import hudson.model.Hudson;
import hudson.model.Run;
import hudson.plugins.sfee.webservice.PackageSoapRow;
import hudson.security.Permission;
import hudson.util.ListBoxModel;

import java.io.IOException;

import javax.servlet.ServletException;

import org.codehaus.plexus.util.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * @plugin
 */
public class PluginImpl extends Plugin {

	public static Permission PUBLISH;
	
	static {
		PUBLISH = new Permission(Run.PERMISSIONS, Messages
				.Run_Permissions_Publish(), Messages
				._Run_PublishPermission_Description(), Hudson.ADMINISTER);
	}
		
	public void doGetReleasePackages(StaplerRequest req, StaplerResponse rsp,
			@QueryParameter("projectId") String projectId) throws IOException,
			ServletException {
		// when the item is not found, the user should be getting an error from
		// elsewhere.
		ListBoxModel r = new ListBoxModel();
		if (!StringUtils.isEmpty(projectId) && !projectId.equals("<none>")) {
			SourceForgeSite site = SourceForgeSite.DESCRIPTOR.getSite();
			PackageSoapRow[] releasePackages = site
					.getReleasePackages(projectId);
			for (PackageSoapRow row : releasePackages) {
				r.add(new ListBoxModel.Option(row.getTitle(), row.getId()));
			}
		}
		r.writeTo(req, rsp);
	}
}

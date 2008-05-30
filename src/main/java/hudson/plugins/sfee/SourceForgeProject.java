package hudson.plugins.sfee;

import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Project;
import hudson.plugins.sfee.webservice.FolderSoapRow;
import hudson.plugins.sfee.webservice.InvalidSessionFault;
import hudson.plugins.sfee.webservice.NoSuchObjectFault;
import hudson.plugins.sfee.webservice.PackageSoapRow;
import hudson.plugins.sfee.webservice.PermissionDeniedFault;
import hudson.plugins.sfee.webservice.ProjectSoapRow;
import hudson.plugins.sfee.webservice.SystemFault;
import hudson.plugins.sfee.webservice.TrackerSoapRow;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import net.sf.json.JSONObject;

import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class SourceForgeProject extends JobProperty<AbstractProject<?, ?>> {

	private final String projectId;
	private final String trackerId;
	private final String releasePackageId;

	public static final String NONE = "(none)";

	public String getTrackerId() {
		return trackerId;
	}

	@DataBoundConstructor
	public SourceForgeProject(String projectId, String releasePackageId,
			String trackerId) {
		this.projectId = projectId;
		this.trackerId = trackerId;
		this.releasePackageId = releasePackageId;
	}

	@Override
	public JobPropertyDescriptor getDescriptor() {
		return DescriptorImpl.INSTANCE;
	}

	public static SourceForgeProject getProperty(AbstractProject<?, ?> project) {
		return project.getProperty(SourceForgeProject.class);
	}

	public static List<Project<?, ?>> getProjects(String projectId) {
		List<Project<?, ?>> result = new ArrayList<Project<?, ?>>();
		for (Project<?, ?> project : Hudson.getInstance().getProjects()) {
			SourceForgeProject p = getProperty(project);
			if (p != null && projectId.equals(p.getProjectId())) {
				result.add(project);
			}
		}
		return result;
	}

	public static final class DescriptorImpl extends JobPropertyDescriptor {

		@Override
		public SourceForgeProject newInstance(StaplerRequest req,
				JSONObject formData) throws FormException {
			SourceForgeProject result = req.bindJSON(SourceForgeProject.class,
					formData);
			if (NONE.equals(result.getProjectId())) {
				return null;
			} else {
				if (!Hudson.getInstance().getACL().hasPermission(Hudson.ADMINISTER)) {
					Authentication auth = Hudson.getAuthentication();
					GrantedAuthority[] authorities = auth.getAuthorities();
					boolean found = false;
					for (GrantedAuthority authority : authorities) {
						found |= authority.getAuthority().equals(
								result.getProjectId());
					}
					if (!found) {
						throw new AccessDeniedException(
						"Cannot change associated SFEE project. You would not have access!");
					}
				}
				return result;
			}
		}

		protected DescriptorImpl() {
			super(SourceForgeProject.class);
		}

		@Override
		public boolean isApplicable(Class<? extends Job> jobType) {
			return AbstractProject.class.isAssignableFrom(jobType);
		}

		@Override
		public String getDisplayName() {
			return "SFEE Project";
		}

		public Collection<ListBoxModel.Option> getPossibleProjectNames() {
			SourceForgeSite site = SourceForgeSite.DESCRIPTOR.getSite();
			if (site == null) {
				return Collections.emptyList();
			}
			ProjectSoapRow[] projects = site
					.getProjects();
			Collection<ListBoxModel.Option> result = new TreeSet<ListBoxModel.Option>(
					new Comparator<ListBoxModel.Option>() {
						public int compare(Option o1, Option o2) {
							return o1.name.toUpperCase().compareTo(
									o2.name.toUpperCase());
						}
					});
			result.add(new ListBoxModel.Option(NONE, NONE));
			for (ProjectSoapRow project : projects) {
				result.add(new ListBoxModel.Option(project.getTitle(), project
						.getId()));
			}
			return result;
		}

		public Collection<ListBoxModel.Option> getPossibleReleasePackageIds(
				String projectId) throws NoSuchObjectFault,
				InvalidSessionFault, SystemFault, PermissionDeniedFault,
				RemoteException {
			if (projectId == null) {
				return Collections.emptyList();
			}
			SourceForgeSite site = SourceForgeSite.DESCRIPTOR.getSite();
			PackageSoapRow[] releasePackages = site
					.getReleasePackages(projectId);
			Collection<ListBoxModel.Option> result = new TreeSet<ListBoxModel.Option>(
					new Comparator<ListBoxModel.Option>() {
						public int compare(Option o1, Option o2) {
							return o1.name.toUpperCase().compareTo(
									o2.name.toUpperCase());
						}
					});
			for (PackageSoapRow row : releasePackages) {
				result
						.add(new ListBoxModel.Option(row.getTitle(), row
								.getId()));
			}
			return getTitles(releasePackages);
		}

		public static final DescriptorImpl INSTANCE = new DescriptorImpl();

	}

	public String getProjectId() {
		return projectId;
	}

	public TrackerSoapRow[] getTrackers() throws NoSuchObjectFault,
			InvalidSessionFault, SystemFault, PermissionDeniedFault,
			RemoteException {
		SourceForgeSite site = SourceForgeSite.DESCRIPTOR.getSite();
		TrackerSoapRow[] trackers = site.getTrackers(projectId);
		return trackers;
	}

	private static Collection<ListBoxModel.Option> getTitles(
			FolderSoapRow[] folders) {
		Collection<ListBoxModel.Option> result = new HashSet<ListBoxModel.Option>();
		for (FolderSoapRow psr : folders) {
			result.add(new ListBoxModel.Option(psr.getTitle(), psr.getId()));
		}
		return result;
	}

	public String getReleasePackageId() {
		return releasePackageId;
	}

}

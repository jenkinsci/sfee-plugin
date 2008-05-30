package hudson.plugins.sfee;

import hudson.model.BuildBadgeAction;
import hudson.model.ProminentProjectAction;

public class SFEEReleaseCompletedTask implements ProminentProjectAction, BuildBadgeAction {

	private final SFEEReleaseTask releaseTask;

	public SFEEReleaseCompletedTask(SFEEReleaseTask<?> releaseTask) {
		this.releaseTask = releaseTask;
	}
	
	public String getDisplayName() {
		String result = releaseTask.isUploadFiles() ? "Uploaded to SourceForge" : "Created in SourceForge";
		return result;
	}

	public String getIconFileName() {
		return null;
	}

	public String getUrlName() {
		return null;
	}
	
	public String getFileReleaseUrl() {
		return releaseTask.getFileReleaseUrl();
	}

	public SFEEReleaseTask getReleaseTask() {
		return releaseTask;
	}

}

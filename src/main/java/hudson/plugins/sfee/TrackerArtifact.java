package hudson.plugins.sfee;

import hudson.plugins.sfee.webservice.ArtifactDetailSoapRow;

public class TrackerArtifact {

	private final String id, projectId, title, description;

	public TrackerArtifact(String id, String description,
			String projectId, String title) {
		this.id = id;
		this.description = description;
		this.projectId = projectId;
		this.title = title;
	}
	
	public TrackerArtifact(ArtifactDetailSoapRow row) {
		this(row.getId(), row.getProjectId(), row.getDescription(), row.getTitle());
	}

	public String getId() {
		return id;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}
	
	public String getURL() {
		return SourceForgeSite.DESCRIPTOR.getSite().getURL(id);
	}
}

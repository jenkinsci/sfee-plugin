package hudson.plugins.sfee;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;

public class Messages {

	public Messages() {
	}

	public static Localizable _Run_PublishPermission_Description() {
		return new Localizable(holder, "Run.PublishPermission.Description",
				new Object[0]);
	}

	private static final ResourceBundleHolder holder = new ResourceBundleHolder(
			Messages.class);

	public static String Run_Permissions_Publish() {
		return holder.format("Run.Permissions.Publish", new Object[0]);
	}
}

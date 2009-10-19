package hudson.plugins.sfee;

import hudson.Extension;
import hudson.MarkupText;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;

import java.util.regex.Pattern;

@Extension
public class LinkAnnotator extends ChangeLogAnnotator {
	@Override
	public void annotate(AbstractBuild<?, ?> build, Entry change,
			MarkupText text) {
		String site = getServer();
		if (site == null) return;  // Plugin not configured
		String url = "http://" + getServer() + "/sf/go/";

		for (LinkMarkup markup : MARKUPS)
			markup.process(text, url);
	}

	static final class LinkMarkup {
		private final Pattern pattern;
		private final String href;

		LinkMarkup(String pattern, String href) {
			pattern = NUM_PATTERN.matcher(pattern).replaceAll("(\\\\d+)");
			// \\\\d becomes \\d when in the expanded text -----^
			pattern = ANYWORD_PATTERN.matcher(pattern).replaceAll(
					"((?:\\\\w|[._-])+)");
			this.pattern = Pattern.compile(pattern);
			this.href = href;
		}

		void process(MarkupText text, String url) {
			for (SubText st : text.findTokens(pattern)) {
				st.surroundWith("<a href='" + url + href + "'>", "</a>");
			}
		}

		private static final Pattern NUM_PATTERN = Pattern.compile("NUM");
		private static final Pattern ANYWORD_PATTERN = Pattern.compile("ANYWORD");
	}

	private String getServer() {
		SourceForgeSite site = SourceForgeSite.DESCRIPTOR.getSite();
		return site != null ? site.getSite() : null;
	}

	static final LinkMarkup[] MARKUPS = new LinkMarkup[] { new LinkMarkup(
			"(artf|task|rel)NUM", "$1$2"), };
}

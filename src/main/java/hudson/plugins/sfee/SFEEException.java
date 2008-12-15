package hudson.plugins.sfee;

public class SFEEException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4311386486278131378L;

	public SFEEException(Exception nested) {
		super(nested);
	}

	public SFEEException(String message, Exception nested) {
		super(message, nested);
	}
}

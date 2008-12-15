package hudson.plugins.sfee;

public class SFEEException extends RuntimeException {

	public SFEEException(Exception nested) {
		super(nested);
	}

	public SFEEException(String message, Exception nested) {
		super(message, nested);
	}
}

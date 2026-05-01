package documentReader.exceptions;

/**
 * Thrown when a file format is not supported by any available DocumentReader.
 */
public class UnsupportedFileException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnsupportedFileException(String message) {
		super(message);
	}
}

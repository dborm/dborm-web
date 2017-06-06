/**
 * 
 */
package com.tbc.paas.mql.domain;

/**
 * @author Ztian
 * 
 */
public class MqlParseException extends RuntimeException {

	private static final long serialVersionUID = 825629846934895833L;

	/**
	 * 
	 */
	public MqlParseException() {
		super();
	}

	/**
	 * @param message
	 */
	public MqlParseException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public MqlParseException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MqlParseException(String message, Throwable cause) {
		super(message, cause);
	}
}

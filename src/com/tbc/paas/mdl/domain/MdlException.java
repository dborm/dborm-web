/**
 * 
 */
package com.tbc.paas.mdl.domain;

/**
 * @author Ztian
 * 
 */
public class MdlException extends RuntimeException {

	private static final long serialVersionUID = 825629846934895833L;

	/**
	 * 
	 */
	public MdlException() {
		super();
	}

	/**
	 * @param message
	 */
	public MdlException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public MdlException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MdlException(String message, Throwable cause) {
		super(message, cause);
	}
}

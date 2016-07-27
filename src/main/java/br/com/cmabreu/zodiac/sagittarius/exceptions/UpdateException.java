package br.com.cmabreu.zodiac.sagittarius.exceptions;

public class UpdateException extends PersistenceException {
	private static final long serialVersionUID = 1L;

	public UpdateException( String message ) {
		super(message);
	}
	
}

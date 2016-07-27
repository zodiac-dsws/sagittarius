package br.com.cmabreu.zodiac.sagittarius.exceptions;

public class NotFoundException extends PersistenceException {
	private static final long serialVersionUID = 1L;

	public NotFoundException(String message){
		super(message);
	}
	
}

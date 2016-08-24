package br.com.cmabreu.zodiac.sagittarius.repository;

import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.Transaction;

import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.exceptions.DatabaseConnectException;
import br.com.cmabreu.zodiac.sagittarius.infra.ConnFactory;


public class BasicRepository {
	protected Session session;
	private Transaction tx = null;

	private String sessionId;

	public BasicRepository() throws DatabaseConnectException {

		try {
			session = ConnFactory.getSession();
			if ( session != null ) {
				tx = session.beginTransaction();
			} else {
				throw new DatabaseConnectException( "Cannot open Database Session" );
			}
		} catch (Exception e ) {
			e.printStackTrace();
			error( e.getMessage() );
			throw new DatabaseConnectException( e.getMessage() );
		}
        UUID uuid = UUID.randomUUID();
        sessionId = uuid.toString().toUpperCase().substring(0,8);
		debug(" --- open  session " + sessionId + " ---" );
	}
	

	public void newTransaction() {
		if ( !session.isOpen() ) {
			debug("new transaction for session " + sessionId );
			session = ConnFactory.getSession();
			if ( session != null ) {
				tx = session.beginTransaction();
			} else {
				debug( "Cannot open Database Session" );
			}
		} else {
			debug("will not open a new transaction. session "+sessionId+" is already open");
		}
	}
	
	public boolean isOpen() {
		return session.isOpen();
	}
	
	public void closeSession() {
		debug(" --- close session " + sessionId + " ---" );
		if( isOpen() ) {
			session.close();
		} else {
			debug(" --- session "+sessionId+" is already closed ---" );
		}
	}
	
	public void commit() {
		debug("commit session " + sessionId );
		tx.commit(); 
	}
	
	public void rollBack() {
		debug("rollback session " + sessionId);
		tx.rollback();
	}
	
	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}	

	private void warn( String s ) {
		Logger.getInstance().warn(this.getClass().getName(), s );
	}	

	private void error( String s ) {
		Logger.getInstance().error(this.getClass().getName(), s );
	}	
}

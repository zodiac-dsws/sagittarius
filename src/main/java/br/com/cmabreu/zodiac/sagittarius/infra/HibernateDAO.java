package br.com.cmabreu.zodiac.sagittarius.infra;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.hibernate.transform.Transformers;

import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.exceptions.DeleteException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.InsertException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.NotFoundException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.UpdateException;



public class HibernateDAO<T> implements IDao<T>  {
	private Class<T> classe;
	private Session session;
	private String sqlDLL;
	private boolean globalWithCommit;
	private long startTime;    
	private long estimatedTime;	
	
	public HibernateDAO(Session session, Class<T> classe) {
		debug("open DAO for entity " + classe.getSimpleName() );
		this.session = session;
		this.classe = classe;
		startTime = System.nanoTime();
	}
	
	private void postClose() {
		//
	}

	public int insertDO(T objeto) throws InsertException {
		debug("insert");
		int res = -1;
		try { 
			res = (Integer)session.save(objeto);
		} catch (HibernateException e) {
			postClose();
			throw new InsertException ( e.getMessage() );
		}
		if ( res == -1 ) {
			throw new InsertException("Unknown error by insert in " + this.classe.getSimpleName() );
		}
		estimatedTime = System.nanoTime() - startTime;
		debug("DAO finished in " + estimatedTime / 1000000000.0 + " seconds");
		
		postClose();
		return res;		
	}


	public int getCount(String tableName, String criteria) throws Exception {
		debug("get count " + tableName + " " + criteria );
        Query query = session.createSQLQuery( "select count(*) from " + tableName + " " + criteria );
        Integer retorno = ( (BigInteger)query.uniqueResult() ).intValue();
		estimatedTime = System.nanoTime() - startTime;
		debug("DAO finished in " + estimatedTime / 1000000000.0 + " seconds");
		
		postClose();
        return retorno;
	}

	
	public List<?> genericAccess(String hql) throws Exception {
		debug("access");
        Query query = session.createSQLQuery(hql);
        //Each row is a list of properties in the query
        List<?> rows = query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
		estimatedTime = System.nanoTime() - startTime;
		debug("DAO finished in " + estimatedTime / 1000000000.0 + " seconds");
		
		postClose();
        return rows;
	}


	public void executeQuery(String hql, boolean withCommit) throws Exception {
		debug("query");
		sqlDLL = hql;
		globalWithCommit = withCommit;
		session.doWork(new Work() {
		    @Override
		    public void execute(Connection connection) throws SQLException {
		    	Statement st = connection.createStatement();
		    	String sql = sqlDLL;
		    	st.execute(sql);
		    	if( globalWithCommit ) {
			    	try {
			    		connection.commit();
			    	} catch ( Exception ignored ) {
			    		error( ignored.getMessage() );
			    	} finally {
				    	try { st.close(); } catch ( Exception e ) { error( e.getMessage() );}
			    		connection.close();
			    	}
		    	}
				debug("DAO finished in " + estimatedTime / 1000000000.0 + " seconds");
				
				postClose();
		    }
		});
		
	}
	
	
	public void deleteDO(T objeto) throws DeleteException {
		debug("delete");
		try {
			session.delete(objeto);
		} catch (HibernateException e) {
			postClose();
			throw new DeleteException( e.getMessage() );
		} 				
		estimatedTime = System.nanoTime() - startTime;
		debug("DAO finished in " + estimatedTime / 1000000000.0 + " seconds");
		
		postClose();
	}
	

	public void updateDO(T objeto) throws UpdateException {
		debug("update");
		try {
			session.saveOrUpdate(objeto);
		} catch (HibernateException e) {
			postClose();
			throw new UpdateException( e.getMessage() );
		} 			
		estimatedTime = System.nanoTime() - startTime;
		debug("DAO finished in " + estimatedTime / 1000000000.0 + " seconds");
		
		postClose();
	}


	@SuppressWarnings("unchecked")
	public List<T> getList(String criteria) throws NotFoundException {
		debug("list");
		try {
			List<T> retorno;
			retorno = (List<T>)session.createSQLQuery(criteria).addEntity(this.classe).list(); 
			if ( retorno.size() == 0 ) {
				debug("empty list");
				postClose();
				throw new NotFoundException("No records found for entity " + this.classe);
			}
			estimatedTime = System.nanoTime() - startTime;
			debug("DAO finished in " + estimatedTime / 1000000000.0 + " seconds");
			
			postClose();
			return retorno;
		} catch (HibernateException e) {
			throw new NotFoundException( e.getMessage() );
		} 		
	}


	@SuppressWarnings("unchecked")
	public T getDO(int id) throws NotFoundException {
		debug("retrieve");
		Object objeto = session.get(classe, id);
		if ( objeto == null ){
			postClose();
			throw new  NotFoundException(classe.getSimpleName() + ": ID " + id + " not found.");
		}
		estimatedTime = System.nanoTime() - startTime;
		debug("DAO finished in " + estimatedTime / 1000000000.0 + " seconds");
		
		postClose();
		return (T)objeto;		
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

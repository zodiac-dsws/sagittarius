package br.com.cmabreu.zodiac.sagittarius.repository;

import java.util.List;

import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.entity.Instance;
import br.com.cmabreu.zodiac.sagittarius.exceptions.DatabaseConnectException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.DeleteException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.InsertException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.NotFoundException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.UpdateException;
import br.com.cmabreu.zodiac.sagittarius.infra.DaoFactory;
import br.com.cmabreu.zodiac.sagittarius.infra.IDao;



public class InstanceRepository extends BasicRepository {

	public InstanceRepository() throws DatabaseConnectException {
		super();
		debug("init");
	}

	
	public List<Instance> recoverFromCrash() throws Exception {
		debug("recovering common instances" );
		DaoFactory<Instance> df = new DaoFactory<Instance>();
		IDao<Instance> fm = df.getDao(this.session, Instance.class);
		List<Instance> pipes = null;
		try {
			pipes = fm.getList("select * from instances where status = 'PAUSED' or status = 'RUNNING' order by id_instance");
		} catch (NotFoundException e) {
			closeSession();
			throw e;
		}
		closeSession();
		debug("done: " + pipes.size() + " instances.");
		return pipes;
	}
	
	
	/*
		select ex.tagexec, frags.serial, frags.total_instances, inst.serial, inst.status from experiments ex 
			join fragments frags on ex.id_experiment = frags.id_experiment 
			join instances inst on inst.id_fragment = frags.id_fragment
		where ex.id_experiment = 270 and frags.status = 'RUNNING' and inst.status = 'PIPELINED' 
		order by inst.id_instance limit 10	
	*/
	public List<Instance> getHead( int howMany, int idExperiment ) throws Exception {
		debug("get first " + howMany + " records for runnig fragments in experiment " + idExperiment );
		DaoFactory<Instance> df = new DaoFactory<Instance>();
		IDao<Instance> fm = df.getDao(this.session, Instance.class);
		List<Instance> pipes = null;
		try {
			
			String newQuery = " experiments ex " + 
					"join fragments frags on ex.id_experiment = frags.id_experiment " + 
					"join instances inst on inst.id_fragment = frags.id_fragment " + 
					"where ex.id_experiment = " + idExperiment + " and frags.status = 'RUNNING' and inst.status = 'PIPELINED' " + 
					"order by inst.id_instance limit " + howMany; 
			
			String query = "select inst.* from" + newQuery;
			
			debug( query );
			
			pipes = fm.getList( query );
			
			String update ="update instances set start_date_time = now(), status = 'RUNNING' where id_instance in (select id_instance from" + 
						newQuery + ")";
			
			debug( update );
			
			fm.executeQuery( update, true );
		} catch (NotFoundException e) {
			closeSession();
			throw e;
		}
		closeSession();
		debug("done: " + pipes.size() + " instances.");
		return pipes;
	}

	/*
	public List<Instance> getHeadJoin( int howMany, int idFragment ) throws Exception {
		debug("get first " + howMany + " JOIN records for fragment " + idFragment );
		debug("get first " + howMany + " records" );
		DaoFactory<Instance> df = new DaoFactory<Instance>();
		IDao<Instance> fm = df.getDao(this.session, Instance.class);
		List<Instance> pipes = null;
		try {
			String selectQuery = "select * from instances where status = 'PIPELINED' and type = 'SELECT' and id_fragment = " + idFragment
					+ " order by id_instance limit " + howMany;
			
			pipes = fm.getList(selectQuery);

			String update ="update instances set status = 'RUNNING' where id_instance in (select id_instance from instances where status = 'PIPELINED' and type = 'SELECT' and id_fragment = " + idFragment
					+ " order by id_instance limit " + howMany + ")"; 
			fm.executeQuery( update, true );
			
		} catch (NotFoundException  e) {
			closeSession();
			throw e;
		}
		closeSession();
		debug("done: " + pipes.size() + " instances.");
		return pipes;		
		
	}
	*/
	
	public List<Instance> getPipelinedList( int idFragment ) throws NotFoundException {
		debug("get list" );
		DaoFactory<Instance> df = new DaoFactory<Instance>();
		IDao<Instance> fm = df.getDao(this.session, Instance.class);
		List<Instance> pipes = null;
		try {
			pipes = fm.getList("select * from instances where status = 'PIPELINED' and id_fragment = " + idFragment);
		} catch (Exception e) {
			closeSession();
			throw e;
		}
		closeSession();
		debug("done: " + pipes.size() + " instances.");
		return pipes;
	}
	
	public List<Instance> getList( int idFragment ) throws NotFoundException {
		debug("get list" );
		DaoFactory<Instance> df = new DaoFactory<Instance>();
		IDao<Instance> fm = df.getDao(this.session, Instance.class);
		List<Instance> pipes = null;
		try {
			pipes = fm.getList("select * from instances where id_fragment = " + idFragment);
		} catch (Exception e) {
			closeSession();
			throw e;
		}
		closeSession();
		debug("done: " + pipes.size() + " instances.");
		return pipes;
	}
	
	public Instance insertInstance(Instance instance) throws InsertException {
		debug("insert");
		DaoFactory<Instance> df = new DaoFactory<Instance>();
		IDao<Instance> fm = df.getDao(this.session, Instance.class);
		try {
			fm.insertDO(instance);
			commit();
		} catch (Exception e) {
			error( e.getMessage() );
			rollBack();
			closeSession();
			throw new InsertException(e.getMessage());
		}
		closeSession();
		debug("done");
		return instance;
	}

	
	public void insertInstanceList( List<Instance> pipes ) throws InsertException {
		debug("insert list");
		DaoFactory<Instance> df = new DaoFactory<Instance>();
		IDao<Instance> fm = df.getDao(this.session, Instance.class);
		try {
			for ( Instance instance : pipes ) {
				fm.insertDO(instance);
			}
			commit();
		} catch (Exception e) {
			error( e.getMessage() );
			rollBack();
			closeSession();
			throw new InsertException(e.getMessage());
		}
		closeSession();
		debug("done");
	}
	
	public Instance getInstance(String serial) throws NotFoundException {
		debug("retrieving instance by serial " + serial + "..." );
		DaoFactory<Instance> df = new DaoFactory<Instance>();
		IDao<Instance> fm = df.getDao(this.session, Instance.class);
		Instance instance = null;
		try {
			instance = fm.getList("select * from instances where serial = '" + serial + "'").get(0);
		} catch ( NotFoundException e ) {
			closeSession();		
			throw e;
		} 
		debug("done");
		closeSession();
		return instance;
	}
	
	public void deleteInstance(Instance instance) throws DeleteException {
		debug("delete instance." );
		DaoFactory<Instance> df = new DaoFactory<Instance>();
		IDao<Instance> fm = df.getDao(this.session, Instance.class);
		try {
			fm.deleteDO(instance);
			commit();
		} catch (Exception e) {
			error( e.getMessage() );
			rollBack();
			closeSession();
			throw new DeleteException( e.getMessage() );			
		}
		debug("done.");
		closeSession();
	}
	
	public Instance getInstance(int idInstance) throws NotFoundException {
		debug("retrieve");
		DaoFactory<Instance> df = new DaoFactory<Instance>();
		IDao<Instance> fm = df.getDao(this.session, Instance.class);
		Instance instance = null;
		try {
			instance = fm.getDO(idInstance);
		} catch ( Exception e ) {
			closeSession();
			throw e;
		}
		closeSession();
		debug("done");
		return instance;
	}
	
	public void updateInstance( Instance instance ) throws UpdateException {
		debug("update");
		DaoFactory<Instance> df = new DaoFactory<Instance>();
		IDao<Instance> fm = df.getDao(this.session, Instance.class);
		try {
			instance.evaluateTime();
			fm.updateDO(instance);
			commit();
		} catch (Exception e) {
			e.printStackTrace();
			
			error( e.getMessage() );
			rollBack();
			closeSession();
			throw new UpdateException( e.getMessage() );
		}
		closeSession();
		debug("done");
	}

	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}	

	private void error( String s ) {
		Logger.getInstance().error(this.getClass().getName(), s );
	}		
}

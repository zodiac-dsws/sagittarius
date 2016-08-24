package br.com.cmabreu.zodiac.sagittarius.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.entity.Experiment;
import br.com.cmabreu.zodiac.sagittarius.entity.User;
import br.com.cmabreu.zodiac.sagittarius.exceptions.DatabaseConnectException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.DeleteException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.InsertException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.NotFoundException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.UpdateException;
import br.com.cmabreu.zodiac.sagittarius.infra.DaoFactory;
import br.com.cmabreu.zodiac.sagittarius.infra.IDao;

public class ExperimentRepository extends BasicRepository {

	public ExperimentRepository() throws DatabaseConnectException {
		super();
		debug("init");
	}


	public Set<Experiment> getList() throws NotFoundException {
		return getList( null );
	}
	
	public Set<Experiment> getList( User user ) throws NotFoundException {
		if ( user == null ) {
			debug("get list for no user" );
		} else {
			debug("get list for user " + user.getLoginName() );
		}
		DaoFactory<Experiment> df = new DaoFactory<Experiment>();
		IDao<Experiment> fm = df.getDao(this.session, Experiment.class);
		Set<Experiment> experiments = null;
		try {
			if ( user == null ) {
				experiments = new HashSet<Experiment>( fm.getList("select * from experiments") );
			} else {
				experiments = new HashSet<Experiment>( fm.getList("select * from experiments where id_user = " + user.getIdUser() ) );
			}
		} catch ( Exception e ) {
			closeSession();
			throw e;
		}
		closeSession();
		debug("done: " + experiments.size() + " experiments.");
		
		for ( Experiment experiment : experiments ) {
			experiment.updateMetrics();
		}
		
		return experiments;
	}


	public List<Experiment> getRunning() throws NotFoundException {
		debug("retrieve pendent" );
		DaoFactory<Experiment> df = new DaoFactory<Experiment>();
		IDao<Experiment> fm = df.getDao(this.session, Experiment.class);
		List<Experiment> running = null;
		try {
			running = fm.getList("select * from experiments where status = 'RUNNING' or status = 'PAUSED' ");
		} catch ( Exception e ) {
			closeSession();		
			throw e;
		} 
		closeSession();		
		debug("done");
		for ( Experiment experiment : running ) {
			experiment.updateMetrics();
		}
		return running;
	}
	
	
	public void updateExperiment( Experiment experiment ) throws UpdateException {
		debug("update");
		DaoFactory<Experiment> df = new DaoFactory<Experiment>();
		IDao<Experiment> fm = df.getDao(this.session, Experiment.class);
		try {
			fm.updateDO(experiment);
			commit();
		} catch (UpdateException e) {
			error( e.getMessage() );
			rollBack();
			closeSession();
			throw e;
		}
		closeSession();
		debug("done");
	}
	
	public Experiment insertExperiment(Experiment experiment) throws InsertException {
		debug("insert");
		DaoFactory<Experiment> df = new DaoFactory<Experiment>();
		IDao<Experiment> fm = df.getDao(this.session, Experiment.class);
		try {
			fm.insertDO(experiment);
			commit();
		} catch (InsertException e) {
			rollBack();
			closeSession();
			error( e.getMessage() );
			throw e;
		}
		closeSession();
		debug("done");
		return experiment;
	}
	
	
	public Experiment getExperiment(String tag) throws NotFoundException {
		debug("retrieving experiment by TAG " + tag + "..." );
		DaoFactory<Experiment> df = new DaoFactory<Experiment>();
		IDao<Experiment> fm = df.getDao(this.session, Experiment.class);
		Experiment experiment = null;
		try {
			experiment = fm.getList("select * from experiments where tagExec = '" + tag + "'").get(0);
		} catch ( Exception e ) {
			closeSession();		
			throw e;
		} 
		debug("done");
		closeSession();
		return experiment;
	}
	
	
	public Experiment getExperiment(int idExperiment) throws NotFoundException {
		debug("retrieving experiment " + idExperiment + "...");
		DaoFactory<Experiment> df = new DaoFactory<Experiment>();
		IDao<Experiment> fm = df.getDao(this.session, Experiment.class);
		Experiment experiment = null;
		try {
			experiment = fm.getDO(idExperiment);
		} catch ( Exception e ) {
			closeSession();		
			throw e;
		} 
		closeSession();		
		debug("done: " + experiment.getTagExec() );
		return experiment;
	}
	

	public void deleteExperiment(Experiment experiment) throws DeleteException {
		debug("delete" );
		DaoFactory<Experiment> df = new DaoFactory<Experiment>();
		IDao<Experiment> fm = df.getDao(this.session, Experiment.class);
		try {
			fm.deleteDO(experiment);
			commit();
		} catch (DeleteException e) {
			rollBack();
			closeSession();
			error( e.getMessage() );
			throw e;			
		}
		debug("done");
		closeSession();
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

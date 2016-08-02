package br.com.cmabreu.zodiac.sagittarius.services;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.com.cmabreu.zodiac.sagittarius.entity.Experiment;
import br.com.cmabreu.zodiac.sagittarius.entity.User;
import br.com.cmabreu.zodiac.sagittarius.exceptions.DatabaseConnectException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.InsertException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.NotFoundException;
import br.com.cmabreu.zodiac.sagittarius.repository.ExperimentRepository;


public class ExperimentService {
	private ExperimentRepository rep;
	private Logger logger = LogManager.getLogger( this.getClass().getName() );

	public ExperimentService() throws DatabaseConnectException {
		this.rep = new ExperimentRepository();
	}

	public void close() {
		rep.closeSession();
	}

	public void newTransaction() {
		rep.newTransaction();
	}
	
	private Experiment insertExperiment(Experiment experiment) throws InsertException {
		Experiment expRet = rep.insertExperiment( experiment );
		return expRet ;
	}	
	
	public Experiment generateExperiment( Experiment source, User owner ) throws InsertException {
		Experiment ex = new Experiment();
		try {
			ex.setWorkflow( source.getWorkflow() );
			ex.setActivitiesSpecs( source.getActivitiesSpecs() );
			ex.setImagePreviewData( source.getImagePreviewData() );
			ex.setOwner( owner );
			ex = insertExperiment(ex);
		} catch ( Exception e ) {
			throw new InsertException( e.getMessage() );
		}
		return ex;
	}
	
	public List<Experiment> getRunning() throws NotFoundException {
		logger.debug("retrieve running experiments");
		List<Experiment> running = rep.getRunning();
		try {
			FragmentService fs = new FragmentService();
			for ( Experiment exp : running ) {
				exp.setFragments( fs.getList( exp.getIdExperiment() ) );
			}
		} catch (DatabaseConnectException e) {
			throw new NotFoundException( e.getMessage() );
		}
		logger.debug("done");
		
		return running;
	}

	public Set<Experiment> getList() throws NotFoundException {
		logger.debug("get list");
		Set<Experiment> preList = rep.getList();
		return preList;	
	}

	public Set<Experiment> getList( User user ) throws NotFoundException {
		logger.debug("get list : user " + user.getLoginName() );
		Set<Experiment> preList = rep.getList( user );
		return preList;	
	}

}

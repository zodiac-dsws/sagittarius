package br.com.cmabreu.zodiac.sagittarius.services;

import java.util.ArrayList;
import java.util.List;

import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.entity.Experiment;
import br.com.cmabreu.zodiac.sagittarius.entity.Fragment;
import br.com.cmabreu.zodiac.sagittarius.entity.Instance;
import br.com.cmabreu.zodiac.sagittarius.exceptions.DatabaseConnectException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.InsertException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.NotFoundException;
import br.com.cmabreu.zodiac.sagittarius.exceptions.UpdateException;
import br.com.cmabreu.zodiac.sagittarius.repository.InstanceRepository;
import br.com.cmabreu.zodiac.sagittarius.types.FragmentStatus;
import br.com.cmabreu.zodiac.sagittarius.types.InstanceStatus;



public class InstanceService {
	private InstanceRepository rep;
	
	public InstanceService() throws DatabaseConnectException {
		this.rep = new InstanceRepository();
	}
	
	public void newTransaction() {
		if( !rep.isOpen() ) {
			rep.newTransaction();
		}
	}

	public void close() {
		rep.closeSession();
	}

	public void finishInstance( Instance instance ) throws UpdateException {
		Instance oldInstance;
		try {
			oldInstance = rep.getInstance( instance.getSerial() );
		} catch (NotFoundException e) {
			throw new UpdateException( e.getMessage() );
		}
		
		oldInstance.setStatus(  InstanceStatus.FINISHED );
		oldInstance.setStartDateTime( instance.getStartDateTime() );
		oldInstance.setFinishDateTime( instance.getFinishDateTime() );
		oldInstance.setExecutedBy( instance.getExecutedBy() );
		oldInstance.setCoresUsed( instance.getCoresUsed() );
		oldInstance.setRealFinishTimeMillis( instance.getRealFinishTimeMillis() );
		oldInstance.setRealStartTimeMillis( instance.getRealStartTimeMillis() );

		rep.newTransaction();
		rep.updateInstance(oldInstance);
	}	

	public int insertInstance(Instance instance) throws InsertException {
		rep.insertInstance( instance );
		return 0;
	}
	
	public void insertInstanceList( List<Instance> pipes ) throws InsertException {
		rep.insertInstanceList( pipes );
	}
	
	public Instance getInstance( String serial ) throws NotFoundException {
		return rep.getInstance(serial);
	}

	public Instance getInstance( int idInstance ) throws NotFoundException {
		return rep.getInstance( idInstance );
	}
	
	
	public List<Instance> getList( int idFragment ) throws NotFoundException {
		List<Instance> pipes = rep.getList( idFragment );
		return pipes;
	}

	public List<Instance> getPipelinedList( int idFragment ) throws NotFoundException {
		List<Instance> pipes = rep.getPipelinedList( idFragment );
		return pipes;
	}
	
	public List<Instance> getHead( int howMany, Experiment experiment ) throws Exception {
		int idExperiment = experiment.getIdExperiment();
		int runningFrags = 0;
		// How many frags running?
		for ( Fragment frag : experiment.getFragments() ) {
			if ( frag.getStatus() == FragmentStatus.RUNNING ) runningFrags++;
		}
		// Partition total to get by the running fragments
		int fragPartition = (int)Math.ceil( howMany / (float) runningFrags );
		List<Instance> pipes = new ArrayList<Instance>();
		
		FragmentService fragmentService = new FragmentService();
		
		// For each running get a piece of fragment
		for ( Fragment frag : experiment.getFragments() ) {
			if ( frag.getStatus() == FragmentStatus.RUNNING ) {
				int count = 0;
				int newValue = 0;
				try {
					rep.newTransaction();
					count = getPipelinedList( frag.getIdFragment() ).size();
					rep.newTransaction();
					List<Instance> temp = rep.getHead( fragPartition, idExperiment, frag.getIdFragment() );
					newValue = temp.size();
					pipes.addAll( temp );
				} catch ( NotFoundException nfe ) {
					debug("No Instances found for Fragment " + frag.getSerial() );
				}
				
				// Update Fragment Remaining Instances in Database
				try {
					rep.newTransaction();
					int newRemainingInstances = count - newValue;
					if ( newRemainingInstances < 0 ) newRemainingInstances = 0;
					if( newRemainingInstances != frag.getRemainingInstances() ) {
						fragmentService.newTransaction();
						frag.setRemainingInstances( newRemainingInstances );
						fragmentService.updateFragment( frag );
					}
					
				} catch (Exception e) {
					//e.printStackTrace();
				}
				
				
				
			}
		}
		// Return all howMany (well, a little more because rounding a float).
		return pipes;
	}
	
	public List<Instance> recoverFromCrash( ) throws Exception {
		List<Instance> pipes = rep.recoverFromCrash();
		return pipes;
	}
	
	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}	
	
}

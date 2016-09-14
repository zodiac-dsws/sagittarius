package br.com.cmabreu.zodiac.sagittarius.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import br.com.cmabreu.zodiac.sagittarius.core.instances.InstanceListContainer;
import br.com.cmabreu.zodiac.sagittarius.entity.Experiment;
import br.com.cmabreu.zodiac.sagittarius.entity.Fragment;
import br.com.cmabreu.zodiac.sagittarius.entity.Instance;
import br.com.cmabreu.zodiac.sagittarius.services.InstanceService;
import br.com.cmabreu.zodiac.sagittarius.types.ExperimentStatus;


public class InstanceBuffer {
	private int bufferSize;
	private Queue<Instance> instanceInputBuffer;
	private Queue<Instance> instanceOutputBuffer;
	private int bufferCurrentLoad = 0;

	public boolean isEmpty() {
		return ( getInstanceInputBufferSize() == 0 ); 
	}
	
	public boolean canLoadMore() {
		return ( getInstanceInputBufferSize() < ( bufferSize / 3 ) );
	}
	
	public int merge( InstanceListContainer listContainer ) {
		try {
			if ( listContainer.size() > 0 ) {
				debug("container have " + listContainer.size() + " instance list. Merging...");
				instanceInputBuffer.addAll( listContainer.merge() );
			} else {
				// None was found
			}
			debug("buffer current capacity utilization: " + instanceInputBuffer.size() + " of " + bufferSize + "(" + 
					getBufferCurrentLoad() + "%)" );
		} catch ( Exception e ) {
			//e.printStackTrace();
		}
		return instanceInputBuffer.size() ;
	}

	public int getBufferCurrentLoad() {
		float x1 = Float.valueOf( getInstanceInputBufferSize() ) ;
		float y1 = Float.valueOf( bufferSize );
		bufferCurrentLoad = Math.round( x1 / y1 * 100 );
		return bufferCurrentLoad;
	}
	
	public int getInstanceInputBufferSize() {
		return instanceInputBuffer.size();
	}
	
	public boolean isQueued( Fragment frag ) {
		for ( Instance instance : instanceOutputBuffer ) {
			if ( instance.getIdFragment() == frag.getIdFragment() ) return true;
		}
		return false;
	}
	
	
	public synchronized Instance getNextInstance( String macAddress,  List<Experiment> runningExperiments ) {
		Instance next = instanceInputBuffer.poll();
		if ( next != null ) {
			debug("serving instance " + next.getSerial() + " to " + macAddress );
			if ( hasOwner(next, runningExperiments) ) {
				instanceOutputBuffer.add( next );
			} else {
				return getNextInstance( macAddress, runningExperiments );
			}
		} else {
			debug("empty output buffer.");
		}
		return next;
	}
	
	public void setBufferSize( int bufferSize ) {
		this.bufferSize = bufferSize;
	}
	
	public int getBufferSize() {
		return bufferSize;
	}

	public InstanceBuffer() {
		this.instanceInputBuffer = new LinkedList<Instance>();
		this.instanceOutputBuffer = new LinkedList<Instance>();
		this.bufferSize = 300;
	}
	
	public synchronized void removeFromOutputBuffer( Instance instance ) {
		for ( Instance pipe : getInstanceOutputBuffer() ) {
			if ( pipe.getSerial().equals( instance.getSerial() ) ) {
				instanceOutputBuffer.remove( pipe );
				break;
			}
		}
	}
	
	public Instance getIntanceFromOutputBuffer( String instanceSerial ) {
		for ( Instance pipe : getInstanceOutputBuffer() ) {
			if ( pipe.getSerial().equals( instanceSerial ) ) {
				return pipe;
			}
		}
		return null;
	}
	
	public synchronized void reloadAfterCrash( List<Experiment> runningExperiments ) {
		debug("after crash reloading " + runningExperiments.size() + " experiments.");
		try {
			InstanceService instanceService = new InstanceService();
			instanceInputBuffer.addAll( instanceService.recoverFromCrash() );
			debug( getInstanceInputBufferSize() + " instances recovered.");
		} catch ( Exception e) {
			debug("no instances to recover");
		} 
		debug("after crash reload done.");
	}
	
	public synchronized void returnToBuffer( Instance instance ) {
		debug("instance " + instance.getSerial() + " moved back to the input buffer" );
		if ( instanceOutputBuffer.remove( instance ) ) {
			instanceInputBuffer.add( instance );
		}
	}
	
	/**
	 *	Discard instances in buffer that have no experiments (deleted) 
	 */
	private synchronized boolean hasOwner( Instance instance, List<Experiment> runningExperiments ) {
		for ( Experiment exp : runningExperiments ) {
			for( Fragment frag : exp.getFragments() ) {
				if ( instance.getIdFragment() == frag.getIdFragment() ) {
					return true;
				}
			}
		}
		warn("owner of instance " + instance.getSerial() + " not found ("+instance.getIdFragment()+"). Will discard this instance.");
		return false;
	}
	
	public Queue<Instance> getInstanceInputBuffer() {
		return new LinkedList<Instance>( instanceInputBuffer );
	}

	public Queue<Instance> getInstanceOutputBuffer() {
		return new LinkedList<Instance>( instanceOutputBuffer );
	}

	public List<Instance> loadBuffer( Experiment experiment, int runningExperimentCount ) throws Exception {
		int instanceInputBufferSize = getInstanceInputBufferSize();
		debug("check buffer because it is at size " + instanceInputBufferSize );
		int sliceSize = ( bufferSize - instanceInputBufferSize ) / runningExperimentCount + 1;		
		
		debug("loading Instances for experiment " + experiment.getTagExec() + ". Slice size: " + sliceSize );
		List<Instance> preBuffer = null;
		if ( experiment.getStatus() != ExperimentStatus.PAUSED ) {
			debug("Experiment " + experiment.getTagExec() + " will try to read " + sliceSize + " Instances from database to the buffer." );
			InstanceService ps = new InstanceService();
			preBuffer = ps.getHead( sliceSize, experiment.getIdExperiment() );
			debug("Available Instances found: " + preBuffer.size() );
		} else {
			debug("experiment " + experiment.getTagExec() + " is paused. will ignore..." );
		}
		return preBuffer;
	}
	
	
	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}	

	private void warn( String s ) {
		Logger.getInstance().warn(this.getClass().getName(), s );
	}	

	public int getInstanceOutputBufferSize() {
		return instanceOutputBuffer.size();
	}	
}

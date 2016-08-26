package br.com.cmabreu.zodiac.sagittarius.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.hadoop.yarn.webapp.NotFoundException;

import br.com.cmabreu.zodiac.sagittarius.core.instances.InstanceList;
import br.com.cmabreu.zodiac.sagittarius.core.instances.InstanceListContainer;
import br.com.cmabreu.zodiac.sagittarius.entity.Experiment;
import br.com.cmabreu.zodiac.sagittarius.entity.Fragment;
import br.com.cmabreu.zodiac.sagittarius.entity.Instance;
import br.com.cmabreu.zodiac.sagittarius.services.InstanceService;
import br.com.cmabreu.zodiac.sagittarius.types.ActivityType;
import br.com.cmabreu.zodiac.sagittarius.types.ExperimentStatus;
import br.com.cmabreu.zodiac.sagittarius.types.FragmentStatus;


public class InstanceBuffer {
	private int bufferSize;
	private Queue<Instance> instanceInputBuffer;
	private Queue<Instance> instanceJoinInputBuffer;
	private Queue<Instance> instanceOutputBuffer;
	private List<Experiment> runningExperiments;
	
	public List<Experiment> getRunningExperiments() {
		return new ArrayList<Experiment>( runningExperiments );
	}
	
	public boolean isEmpty() {
		return ( getInstanceJoinInputBufferSize() == 0 ) && ( getInstanceInputBufferSize() == 0 ); 
	}
	
	private synchronized void processAndInclude( List<Instance> preBuffer ) {
		debug("collecting instances from database to buffers...");
		for( Instance instance : preBuffer ) {
			if( instance.getType().isJoin() ) {
				debug("JB > " + instance.getSerial() + " " + instance.getType() );
				instanceJoinInputBuffer.add(instance);
			} else {
				debug("CB > " + instance.getSerial() + " " + instance.getType() );
				instanceInputBuffer.add(instance);
			}
		}
		debug("done.");
	}
	
	public synchronized int loadBuffers() throws Exception {
		int runningExperimentCount = getRunningExperiments().size(); 
		if ( runningExperimentCount == 0 ) return 0;
		int sliceSize;

		if ( getInstanceInputBufferSize() < ( bufferSize / 3 ) ) {
			InstanceListContainer listContainer = new InstanceListContainer();
			debug("check COMMON buffer because buffer is at " + getInstanceInputBufferSize() );
			sliceSize = ( bufferSize - getInstanceInputBufferSize() ) / runningExperimentCount + 1;
			for ( Experiment experiment : getRunningExperiments() ) {
				
				if ( experiment.getStatus() != ExperimentStatus.PAUSED ) {
					debug("loading Common Instances for experiment " + experiment.getTagExec() + ". Slice size: " + sliceSize );
					List<Instance> common = loadCommonBuffer( sliceSize, experiment );
					if ( common != null ) {
						debug("found " + common.size() + " instances. Adding to container...");
						listContainer.addList( new InstanceList(common, experiment.getTagExec()) );
					}
				} else {
					debug("experiment " + experiment.getTagExec() + " is paused. will ignore..." );
				}
				
			}
			try {
				if ( listContainer.size() > 0 ) {
					debug("container have " + listContainer.size() + " instance list. Merging...");
					instanceInputBuffer.addAll( listContainer.merge() );
				} else {
					//
				}
				debug("common buffer size: " + instanceInputBuffer.size() );
			} catch ( Exception e ) {
				e.printStackTrace();
			}
			
		}
	
		if ( getInstanceJoinInputBufferSize() < ( bufferSize / 5 ) ) {
			InstanceListContainer listContainerS = new InstanceListContainer();
			debug("check SELECT buffer...");
			sliceSize = ( bufferSize - getInstanceJoinInputBufferSize() ) / runningExperimentCount + 1;
			for ( Experiment experiment : getRunningExperiments() ) {
				debug(" > " + experiment.getTagExec() + " " + experiment.getStatus() );
				if ( experiment.getStatus() != ExperimentStatus.PAUSED ) {
					debug("loading SELECT Instances for experiment " + experiment.getTagExec() );
					List<Instance> select = loadJoinBuffer( sliceSize, experiment); 
					if ( select != null ) {
						listContainerS.addList( new InstanceList(select, experiment.getTagExec()) );
					} 
				} else {
					debug("experiment " + experiment.getTagExec() + " is paused. will ignore..." );
				}
			}
			if ( listContainerS.size() > 0 ) {
				debug("SQL container have " + listContainerS.size() + " instance list. Merging...");
				instanceJoinInputBuffer.addAll( listContainerS.merge() );
			}
			debug("SELECT buffer size: " + instanceJoinInputBuffer.size() );
		}
		
		return ( instanceInputBuffer.size() + instanceJoinInputBuffer.size() );
	}
	
	
	public int getInstanceInputBufferSize() {
		return instanceInputBuffer.size();
	}
	
	public int getInstanceJoinInputBufferSize() {
		return instanceJoinInputBuffer.size();
	}
	
	private synchronized Instance getNextInstance( String macAddress) {
		System.out.println("Blihhhh...");
		Instance instance = getNextInstance( getRunningExperiments(), macAddress );
		if ( instance != null ) {
			debug("serving instance " + instance.getSerial() + " to " + macAddress );
		} else {
			debug("null instance");
		}
		return instance;
	}
	
	public synchronized Instance getNextInstance( List<Experiment> runningExperiments, String macAddress ) {
		this.runningExperiments = runningExperiments;
		Instance next = instanceInputBuffer.poll();
		if ( next != null ) {
			if ( next.getType() == ActivityType.SELECT ) {
				error("SELECT type Instance in common buffer!");
			} else {
				if ( hasOwner(next) ) {
					instanceOutputBuffer.add( next );
				} else {
					return getNextInstance( macAddress );
				}
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
		this.instanceJoinInputBuffer = new LinkedList<Instance>();
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
	
	public synchronized boolean experimentIsStillQueued( Experiment exp ) {
		for( Fragment frag : exp.getFragments() ) {
			for( Instance pipe : getInstanceOutputBuffer()  ) {
				if( pipe.getIdFragment() == frag.getIdFragment() ) {
					return true;
				}
			}
			
			for( Instance pipe : instanceInputBuffer  ) {
				if( pipe.getIdFragment() == frag.getIdFragment() ) {
					return true;
				}
			}
			for( Instance pipe : instanceJoinInputBuffer  ) {
				if( pipe.getIdFragment() == frag.getIdFragment() ) {
					return true;
				}
			}
		}
		return false;
	}
	
	public synchronized void reloadAfterCrash( List<Experiment> runningExperiments ) {
		this.runningExperiments = runningExperiments;
		debug("after crash reloading " + runningExperiments.size() + " experiments.");
		try {
			try {
				InstanceService instanceService = new InstanceService();
				processAndInclude( instanceService.recoverFromCrash() );
				debug( getInstanceInputBufferSize() + " common instances recovered.");
				debug( getInstanceJoinInputBufferSize() + " JOIN instances recovered.");
			} catch ( NotFoundException e ) {
				debug("no instances to recover");
			}
			
		} catch ( Exception e) {
			error( e.getMessage() );
		} 
		debug("after crash reload done.");
	}
	
	public synchronized Instance getNextJoinInstance( String macAddress ) {
		Instance next = instanceJoinInputBuffer.poll();
		if ( next != null ) {
			debug("serving SELECT instance " + next.getSerial() + " to " + macAddress );
			instanceOutputBuffer.add(next);
		}
		return next;
	}

	public synchronized void returnToBuffer( Instance instance ) {
		debug("instance refund: " + instance.getSerial() );
		if ( instanceOutputBuffer.remove( instance ) ) {
			if ( instance.getType().isJoin() ) {
				instanceJoinInputBuffer.add( instance );
				debug(" > to the join buffer" );
			} else {
				instanceInputBuffer.add( instance );
				debug(" > to the common buffer" );
			}
		}
	}
	
	
	/**
	 *	Discard instances in buffer that have no experiments (deleted) 
	 */
	private synchronized boolean hasOwner( Instance instance ) {
		for ( Experiment exp : getRunningExperiments() ) {
			for( Fragment frag : exp.getFragments() ) {
				if ( instance.getIdFragment() == frag.getIdFragment() ) {
					return true;
				}
			}
		}
		warn("owner of instance " + instance.getSerial() + " not found. Will discard this instance.");
		return false;
	}
	
	public Queue<Instance> getInstanceInputBuffer() {
		return new LinkedList<Instance>( instanceInputBuffer );
	}

	public Queue<Instance> getInstanceOutputBuffer() {
		return new LinkedList<Instance>( instanceOutputBuffer );
	}

	public Queue<Instance> getInstanceJoinInputBuffer() {
		return new LinkedList<Instance>( instanceJoinInputBuffer );
	}
	
	private synchronized Fragment getRunningFragment( Experiment experiment ) {
		for ( Fragment frag : experiment.getFragments() ) {
			if ( frag.getStatus() == FragmentStatus.RUNNING ) {
				return frag;
			}
		}
		return null;
	}
	
	private List<Instance> loadJoinBuffer( int count, Experiment experiment ) {
		List<Instance> preBuffer = null;
		debug("loading SELECT buffer. current size: " + instanceJoinInputBuffer.size());
		try {
			Fragment running = getRunningFragment( experiment );
			if ( running == null ) {
				debug("no SELECT fragments running");
			} else {
				debug("running SELECT fragment found: " + running.getSerial() );
				try {
					InstanceService ps = new InstanceService();
					preBuffer = ps.getHeadJoin( count, running.getIdFragment() );
					debug("found " + preBuffer.size() + " Instances for experiment " + experiment.getTagExec() + " Fragment " +
					 running.getSerial() );
				} catch (NotFoundException e) {
					debug("no more SELECT instances found in database for experiment " + experiment.getTagExec() +
							" Fragment " + running.getSerial() );
				}
			}
		} catch (Exception e) {
			error( e.getMessage() );
		}
		return preBuffer;
	}

	private List<Instance> loadCommonBuffer( int count, Experiment experiment ) {
		List<Instance> preBuffer = null;
		debug("loading common buffer...");
		try {
			Fragment running = getRunningFragment( experiment );
			if ( running == null ) {
				debug("no fragments running");
			} else {
				debug("running fragment found: " + running.getSerial() );
				InstanceService ps = new InstanceService();
				preBuffer = ps.getHead( count, running.getIdFragment() );
			}
		} catch (NotFoundException e) {
			debug("no running instances found for experiment " + experiment.getTagExec() );
		} catch ( Exception e) {
			error( e.getMessage() );
		} 
		return preBuffer;
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

	public int getInstanceOutputBufferSize() {
		return instanceOutputBuffer.size();
	}	
}

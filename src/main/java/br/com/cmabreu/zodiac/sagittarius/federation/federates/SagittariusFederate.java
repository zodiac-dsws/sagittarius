package br.com.cmabreu.zodiac.sagittarius.federation.federates;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.cmabreu.zodiac.sagittarius.core.InstanceBuffer;
import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.core.instances.InstanceList;
import br.com.cmabreu.zodiac.sagittarius.core.instances.InstanceListContainer;
import br.com.cmabreu.zodiac.sagittarius.entity.Experiment;
import br.com.cmabreu.zodiac.sagittarius.entity.Fragment;
import br.com.cmabreu.zodiac.sagittarius.entity.Instance;
import br.com.cmabreu.zodiac.sagittarius.federation.Environment;
import br.com.cmabreu.zodiac.sagittarius.federation.RTIAmbassadorProvider;
import br.com.cmabreu.zodiac.sagittarius.federation.classes.CoreClass;
import br.com.cmabreu.zodiac.sagittarius.federation.classes.SagittariusClass;
import br.com.cmabreu.zodiac.sagittarius.federation.classes.ScorpioClass;
import br.com.cmabreu.zodiac.sagittarius.federation.objects.CoreObject;
import br.com.cmabreu.zodiac.sagittarius.misc.PathFinder;
import br.com.cmabreu.zodiac.sagittarius.misc.ZipUtil;
import br.com.cmabreu.zodiac.sagittarius.services.InstanceService;
import br.com.cmabreu.zodiac.sagittarius.types.ExperimentStatus;
import br.com.cmabreu.zodiac.sagittarius.types.InstanceStatus;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;

public class SagittariusFederate {
	private static SagittariusFederate instance;
	private String rootPath;
	private SagittariusClass sagittariusClass;
	private ScorpioClass scorpioClass;
	private CoreClass coreClass;
	
	// ==== OLD SAGITARII ========================
	private InstanceBuffer instanceBuffer;
	private List<Experiment> runningExperiments;
	
	public int loadBuffers() throws Exception {
		int runningExperimentCount = getRunningExperiments().size(); 
		if ( runningExperimentCount == 0 ) return 0;
		InstanceListContainer listContainer = new InstanceListContainer();
		
		if ( instanceBuffer.canLoadMore() ) {
			for ( Experiment experiment : getRunningExperiments() ) {
				try {
					List<Instance> common = instanceBuffer.loadBuffer( experiment, runningExperimentCount);
					if ( common != null ) {
						debug("found " + common.size() + " instances. Adding to container...");
						listContainer.addList( new InstanceList( common, experiment.getTagExec() ) );
					}
				} catch ( Exception e ) {
					
					// ***************** No running instances found for this experiment (or error) *******************
					// MUST FINISH THE RUNNIG FRAGMENTS AND TRY TO START ANOTHER OR FINISH EXPERIMENT 
					// ***********************************************************************************************
					
					System.out.println("No more instances here: ");
					for ( Fragment frag : experiment.getFragments() ) {
						System.out.println( " > " + frag.getSerial() + " : " + frag.getStatus() + " " + instanceBuffer.isQueued(frag) );
					}
					
				}
			}
		}
		int buffer = instanceBuffer.merge( listContainer );
		
		System.out.println("Buffer current load: " + instanceBuffer.getBufferCurrentLoad() );
		
		return buffer;
	}
	
	public void setMaxInputBufferCapacity(int maxInputBufferCapacity) {
		instanceBuffer.setBufferSize( maxInputBufferCapacity );
	}
	
	public int getMaxInputBufferCapacity() {
		return instanceBuffer.getBufferSize();
	}
	
	public synchronized List<Experiment> getRunningExperiments() {
		return new ArrayList<Experiment>( runningExperiments );
	}
	
	public void setRunningExperiments(List<Experiment> runningExperiments) {
		this.runningExperiments = runningExperiments;
	}
	
	public synchronized void addRunningExperiment( Experiment experiment ) throws Exception {
		boolean found = false;
		for ( Experiment exp : runningExperiments ) {
			if ( exp.getTagExec().equalsIgnoreCase( experiment.getTagExec() ) ) {
				found = true;
			}
		}
		if ( !found && ( experiment.getStatus() == ExperimentStatus.RUNNING ) ) {
			runningExperiments.add( experiment );
			//updateFragments();
		}
	}	
	
	public boolean isRunning() {
		return ( runningExperiments.size() > 0 );
	}	
	
	public void reloadAfterCrash() {
		instanceBuffer.reloadAfterCrash( runningExperiments );
	}	
	
	public synchronized void finishInstance( String instanceSerial, CoreObject core ) throws Exception {
		Instance instance = instanceBuffer.getIntanceFromOutputBuffer( instanceSerial );
		if ( instance != null ) {
			instance.setFinishDateTime( Calendar.getInstance().getTime() );
			instance.setExecutedBy( core.getOwnerNode() );
			finishInstance( instance );
		} else {
			warn("Instance " + instanceSerial + " is not in output buffer. Ignoring...");
		}
	}
	
	private void finishInstance( Instance instance ) {
		debug("instance " + instance.getSerial() + " was finished by " + instance.getExecutedBy() + ". execution time: " + instance.getElapsedTime() );
		try {
			// Set as finished (database)
			InstanceService instanceService = new InstanceService();
			instanceService.finishInstance( instance );
			// Remove from output buffer if any
			instanceBuffer.removeFromOutputBuffer( instance );
		} catch ( Exception e ) {
			error( e.getMessage() );
			e.printStackTrace();
		}
	}
	
	public synchronized Instance getNextInstance(String macAddress) {
		return instanceBuffer.getNextInstance( macAddress, getRunningExperiments() );
	}

	
	public synchronized void returnToBuffer( Instance instance ) {
		instance.triedAgain();
		instanceBuffer.returnToBuffer(instance);
	}
	
	// ============================== END OLD SAGITARII STUFF ================================================

	public static SagittariusFederate getInstance() throws Exception {
		if ( instance == null ) {
			instance = new SagittariusFederate();
		}
		return instance;
	}
	
	public void finishFederationExecution() throws Exception {
		debug( "Will try to finish Federation execution" );
		RTIambassador rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();

		rtiamb.resignFederationExecution( ResignAction.DELETE_OBJECTS );
		try	{
			rtiamb.destroyFederationExecution( "Zodiac" );
			debug( "Destroyed Federation" );
		} catch( FederationExecutionDoesNotExist dne ) {
			debug( "No need to destroy federation, it doesn't exist" );
		} catch( FederatesCurrentlyJoined fcj ){
			debug( "Didn't destroy federation, federates still joined" );
		}		
	}
	
	public InstanceBuffer getInstanceBuffer() {
		return instanceBuffer;
	}
	
	private SagittariusFederate( ) throws Exception {
		instanceBuffer = new InstanceBuffer();
		runningExperiments = new ArrayList<Experiment>();
		rootPath = PathFinder.getInstance().getPath();
	}
	
	private void startFederate() {
		debug("Starting Zodiac Sagittarius Instance Controller");
		try {

			Map<String, String> newenv = new HashMap<String, String>();
			newenv.put("RTI_HOME", "");
			//newenv.put("RTI_RID_FILE", rootPath + "/rti.RID" );
			Environment.setEnv( newenv );
			
			RTIambassador rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
			
			try	{
				URL[] modules = new URL[]{
					(new File( rootPath + "/foms/HLAstandardMIM.xml" ) ).toURI().toURL()
				};
				rtiamb.createFederationExecution("Zodiac", modules );
			} catch( FederationExecutionAlreadyExists exists ) {
				debug("Federation already exists. Bypassing...");
			}
			
			try {
				join();
			} catch ( Exception e ) {
				error("Error when joing the Federation: " + e.getMessage() );
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	
	public ObjectInstanceHandle getSagittariusObjectHandle() {
		return getSagittariusClass().getSagittariusObjectHandle();
	}
	
	public void reflectAttributeUpdate( ObjectInstanceHandle theObject,  AttributeHandleValueMap theAttributes ) {
		
		try {
			getCoreClass().reflectAttributeValues( theAttributes, theObject );
			getScorpioClass().reflectAttributeValues( theAttributes, theObject );
		} catch ( Exception e ) {
			e.printStackTrace(); 
		}		
		
	}
	

	public void startServer() throws Exception {
		startFederate();
		if ( sagittariusClass == null ) {
			
			sagittariusClass = new SagittariusClass();
			// Publish server attributes
			sagittariusClass.publish();
			// Create a new Server Object
			sagittariusClass.createNew();

			// Subscribe to Scorpio Updates
			scorpioClass = new ScorpioClass();
			scorpioClass.subscribe();
			
			// Subscribe to Cores updates
			coreClass = new CoreClass();
			coreClass.subscribe();
			coreClass.publishCurrentInstance();

			debug("done.");
			
		} else {
			warn("server is already running an instance");
		}
	}
	
	public SagittariusClass getSagittariusClass() {
		return sagittariusClass;
	}
	
	public ScorpioClass getScorpioClass() {
		return scorpioClass;
	}
	
	public CoreClass getCoreClass() {
		return coreClass;
	}
	
	private void join() throws Exception {
		RTIambassador rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();

		debug("joing Federation Execution ...");
		URL[] joinModules = new URL[]{
			(new File(rootPath +  "/foms/zodiac.xml")).toURI().toURL(),	
		    (new File(rootPath +  "/foms/sagittarius.xml")).toURI().toURL(),
		    (new File(rootPath +  "/foms/core.xml")).toURI().toURL(),
		    (new File(rootPath +  "/foms/scorpio.xml")).toURI().toURL()
		};
		rtiamb.joinFederationExecution( "Sagittarius", "SagittariusType", "Zodiac", joinModules );           
	}
	
	public void attributeOwnershipAcquisitionNotification( ObjectInstanceHandle theObject, AttributeHandleSet securedAttributes ) {
		debug("I now own the Current Instance attibute. Sending new instance...");
		sendInstance( theObject );
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

	public void releaseAttributeOwnership(ObjectInstanceHandle theObject, AttributeHandleSet candidateAttributes) {
		debug("Release Attribute Ownership Request");
		try {
			RTIambassador rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
			rtiamb.attributeOwnershipDivestitureIfWanted( theObject, candidateAttributes );
			debug("Released.");
		} catch ( Exception e ) {
			error("Error: " + e.getMessage() );
		}		
	}
	
	private String fillInstanceID( Instance instance ) {
		String content = instance.getContent();
		try {
			content = instance.getContent().replace("%ID_PIP%", String.valueOf( instance.getIdInstance() ) );
		} catch ( Exception e ) {
			error("Error setting Instance ID to instance content tag.");
		}
		return content.replace("##TAG_ID_INSTANCE##", String.valueOf( instance.getIdInstance() ) );
	}	
	
	private String encode( Instance instance ) {
		instance.setStartDateTime( Calendar.getInstance().getTime() );
		instance.setStatus( InstanceStatus.WAITING );
		String content = fillInstanceID ( instance );
		instance.setContent( content );
		byte[] respCompressed = ZipUtil.compress( content );
		String respHex = ZipUtil.toHexString( respCompressed );
		debug( " > instance "+ instance.getSerial() + " compressed and control tags replaced." );	
		return respHex;
	}
	
	
	private synchronized void sendInstance( ObjectInstanceHandle theObject ) {
		try {
			for ( CoreObject core : coreClass.getCores()  ) {
				if ( core.isMe( theObject ) ) {
					Instance instance = getNextInstance( core.getOwnerNode() );
					if ( instance != null ) {
						debug("sending Instance " + instance.getSerial() + " to Core " + core.getSerial() + "@" + core.getOwnerNode() );
						core.setCurrentInstance( encode( instance ) );
						try {
							coreClass.updateAttributeValuesObject( core );
						} catch ( Exception e ) {
							warn("Returning Instance " + instance.getSerial() + " to buffer because of " + e.getMessage() );
							instanceBuffer.returnToBuffer(instance);
						}
					} 
					break;
				}
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}	

	public void checkCores() {
		try {
			for ( CoreObject core : coreClass.getCores()  ) {
				if (  core.getCurrentInstance().equals("*")  && !core.isWorking() ) {
					sendInstance( core.getHandle() );
				}
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
	}	
	
	
}

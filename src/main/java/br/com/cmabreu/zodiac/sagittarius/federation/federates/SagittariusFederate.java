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
import br.com.cmabreu.zodiac.sagittarius.federation.classes.ExperimentFinishedInteractionClass;
import br.com.cmabreu.zodiac.sagittarius.federation.classes.ExperimentStartedInteractionClass;
import br.com.cmabreu.zodiac.sagittarius.federation.classes.GeminiClass;
import br.com.cmabreu.zodiac.sagittarius.federation.classes.GenerateInstancesInteractionClass;
import br.com.cmabreu.zodiac.sagittarius.federation.classes.InstanceCreationErrorInteractionClass;
import br.com.cmabreu.zodiac.sagittarius.federation.classes.InstancesCreatedInteractionClass;
import br.com.cmabreu.zodiac.sagittarius.federation.classes.SagittariusClass;
import br.com.cmabreu.zodiac.sagittarius.federation.classes.ScorpioClass;
import br.com.cmabreu.zodiac.sagittarius.federation.objects.CoreObject;
import br.com.cmabreu.zodiac.sagittarius.misc.PathFinder;
import br.com.cmabreu.zodiac.sagittarius.misc.ZipUtil;
import br.com.cmabreu.zodiac.sagittarius.services.ExperimentService;
import br.com.cmabreu.zodiac.sagittarius.services.FragmentService;
import br.com.cmabreu.zodiac.sagittarius.services.InstanceService;
import br.com.cmabreu.zodiac.sagittarius.types.ExperimentStatus;
import br.com.cmabreu.zodiac.sagittarius.types.FragmentStatus;
import br.com.cmabreu.zodiac.sagittarius.types.InstanceStatus;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandleValueMap;
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
	private GeminiClass geminiClass;
	private GenerateInstancesInteractionClass generateInstancesInteractionClass;
	private ExperimentStartedInteractionClass experimentStartedInteractionClass;
	private ExperimentFinishedInteractionClass experimentFinishedInteractionClass;
	private InstancesCreatedInteractionClass instancesCreatedInteractionClass;
	private InstanceCreationErrorInteractionClass instanceCreationErrorInteractionClass;	
	
	// ==== OLD SAGITARII ========================
	private InstanceBuffer instanceBuffer;
	private List<Experiment> runningExperiments;
	
	public void experimentStarted(ParameterHandleValueMap theParameters) {
		String experimentSerial = experimentStartedInteractionClass.getExperimentSerial( theParameters );
		debug("Gemini started Experiment " + experimentSerial + ". Will add it to the buffer..." );
		
		try {
			ExperimentService es = new ExperimentService();
			Experiment exp = es.getExperiment(experimentSerial);
			addRunningExperiment( exp );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}	

	public synchronized void addRunningExperiment( Experiment experiment ) throws Exception {
		for ( Experiment exp : getRunningExperiments() ) {
			if ( exp.getTagExec().equalsIgnoreCase( experiment.getTagExec() ) ) {
				error("Experiment " + experiment.getTagExec() + " is already in the buffer. Aborting...");
				return;
			}
		}
		
		if ( experiment.getStatus() == ExperimentStatus.RUNNING ) {
			debug("Experiment " + experiment.getTagExec() + " is running and not in the buffer. Loading Fragments...");
			FragmentService fs = new FragmentService();
			experiment.setFragments( fs.getList( experiment.getIdExperiment() ) );
			runningExperiments.add( experiment );
			debug("Experiment " + experiment.getTagExec() + " is now ready to process.");
		}
	}	
	
	public void finishExperiment( ParameterHandleValueMap theParameters ) throws Exception {
		String experimentSerial = experimentFinishedInteractionClass.getExperimentSerial( theParameters );
		debug("Received notification Experiment " + experimentSerial + " is finished.");
		
		for ( Experiment experiment : getRunningExperiments() ) {
			if ( experiment.getTagExec().equalsIgnoreCase( experimentSerial ) ) {
				debug("Removing Experiment " + experiment.getTagExec() + " from buffer.");
				runningExperiments.remove( experiment );
				debug("Experiment " + experiment.getTagExec() + " finished. Experiments running: " + runningExperiments.size() );
				return;
			}
		}
		
		debug("Experiment " + experimentSerial + " is not in the buffer.");
		/*
		if ( experiment.getStatus() != ExperimentStatus.FINISHED ) {
			ExperimentService es = new ExperimentService();
			experiment.setStatus( ExperimentStatus.FINISHED );
			experiment.setFinishDateTime( Calendar.getInstance().getTime() );
			es.newTransaction();
			es.updateExperiment(experiment);
		}
		*/
	}
	
	private void startNextActivities( Experiment exp ) {
		try {
			generateInstancesInteractionClass.send( exp.getTagExec() );
		} catch ( Exception e) {
			e.printStackTrace();
		}
	}

	private void finishFragment( Fragment frag ) {
		debug("Fragment " + frag.getSerial() + " will finish...");
		try {
			frag.setStatus( FragmentStatus.FINISHED );
			FragmentService fragmentService = new FragmentService();
			fragmentService.updateFragment(frag);
			debug("Fragment " + frag.getSerial() + " finished.");
		} catch ( Exception e ) {
			error("Error setting Fragment " + frag.getSerial() + " to finished: " + e.getMessage() );
			
		}
	}
	
	// New gemini found. If this is the first one then request again to create instances.
	public void requestCreateInstancesAgain() {
		if ( getRunningExperiments().size() == 0 ) return;
		
		debug("Asking again to generate instances for Experiments: ");
		for ( Experiment experiment : getRunningExperiments() ) {
			boolean haveMore = false;
			boolean isQueued = false;
			boolean allStopped = true;
			
			for ( Fragment frag : experiment.getFragments() ) {
				if ( frag.getStatus() == FragmentStatus.READY  ) haveMore = true;
				if ( instanceBuffer.isQueued( frag ) ) isQueued = true;
				if ( frag.getStatus() == FragmentStatus.RUNNING  ) allStopped = false;
			}
			
			if ( haveMore && allStopped && !isQueued ) {
				debug(" > " + experiment.getTagExec() );
				startNextActivities( experiment );
			}
			
		}
	}
	
	private void requestMoreInstancesAndFinishFragment( Experiment experiment ) throws Exception {
		debug("No Instances in database for experiment " + experiment.getTagExec() + "..." );
		boolean canLoadMore = true; 
		
		// Now do the other checks
		for ( Fragment frag : experiment.getFragments() ) {
			boolean isQueued = instanceBuffer.isQueued( frag );
			System.out.println( "  > " + frag.getSerial() + " : " + frag.getStatus() + " " + isQueued );
			if ( ( frag.getStatus() == FragmentStatus.RUNNING ) && ( !isQueued ) ) {
				finishFragment( frag );
			}
			
			// frag.Status may not be RUNNING here because of finishFragment above.
			if ( frag.getStatus() == FragmentStatus.RUNNING ) canLoadMore = false;
		}

		if ( canLoadMore ) startNextActivities( experiment );
	}
	
	public int loadBuffers() throws Exception {
		int runningExperimentCount = getRunningExperiments().size(); 
		if ( runningExperimentCount == 0 ) return 0;
		InstanceListContainer listContainer = new InstanceListContainer();
		
		if ( instanceBuffer.canLoadMore() ) {
			for ( Experiment experiment : getRunningExperiments() ) {
				try {
					List<Instance> tempBuffer = instanceBuffer.loadBuffer( experiment, runningExperimentCount );
					if ( tempBuffer != null ) {
						debug("Experiment " + experiment.getTagExec() + " have " + tempBuffer.size() + " instances running. Adding to merge buffer...");
						listContainer.addList( new InstanceList( tempBuffer, experiment.getTagExec() ) );
					}
				} catch ( Exception e ) {
					requestMoreInstancesAndFinishFragment( experiment );
				}
			}
			updateFragments();
		} else {
			// 
		}
		int buffer = 0;
		
		if ( listContainer.size() > 0 ) {
			debug("Merge buffer size: " + listContainer.size() + ". Adding to the Instance buffer.");
			buffer = instanceBuffer.merge( listContainer );
		} else {
			debug("Merge buffer is empty. No Instances to process for any ("+runningExperimentCount+") running Experiment.");
		}
	
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
	
	private synchronized void updateFragments() throws Exception {
		debug("Updating Fragments...");
		InstanceService instanceService = new InstanceService();
		FragmentService fragmentService = new FragmentService();
		for ( Experiment exp : runningExperiments ) {
			debug(" " + exp.getTagExec() + ": ");
			for ( Fragment frag : exp.getFragments() ) {
				int oldRemainingInstances = frag.getRemainingInstances();
				int count = 0;
				try {
					instanceService.newTransaction();
					count = instanceService.getPipelinedList( frag.getIdFragment() ).size();
				} catch ( Exception e) {
					count = 0;
				}
				debug("  > " + frag.getSerial() + " " + count );
				if( oldRemainingInstances != count ) {
					fragmentService.newTransaction();
					frag.setRemainingInstances( count );
					fragmentService.updateFragment( frag );
				}
			}	
		}
		debug("done updating fragments.");
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
			instance.setCoresUsed( coreClass.getCores().size() );
			finishInstance( instance );
		} else {
			warn("Instance " + instanceSerial + " is not in output buffer. Ignoring...");
		}
	}
	
	private void finishInstance( Instance instance ) {
		debug("instance " + instance.getSerial() + " was finished by " + instance.getExecutedBy() + ". execution time: " + instance.getElapsedTime() );
		try {
			// Remove from output buffer if any
			instanceBuffer.removeFromOutputBuffer( instance );
			// Set as finished (database)
			InstanceService instanceService = new InstanceService();
			instanceService.finishInstance( instance );
		} catch ( Exception e ) {
			error( e.getMessage() );
			e.printStackTrace();
		}
	}
	
	public synchronized Instance getNextInstance(String macAddress) {
		if ( getRunningExperiments().size() == 0 ) {
			error("Node "+macAddress+" is requesting Instance but have no Experiments running");
			return null;
		}
		return instanceBuffer.getNextInstance( macAddress, getRunningExperiments() );
	}

	
	public synchronized void returnToBuffer( Instance instance ) {
		instance.triedAgain();
		instanceBuffer.returnToBuffer(instance);
	}
	
	// ============================== END OLD SAGITARII STUFF ================================================

	public GenerateInstancesInteractionClass getGenerateInstancesInteractionClass() {
		return generateInstancesInteractionClass;
	}
	
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
			
			// Allow to send "Generate Instances" commands to Gemini
			generateInstancesInteractionClass = new GenerateInstancesInteractionClass();
			generateInstancesInteractionClass.publish();				

			// Listen to new Running Experiments
			experimentStartedInteractionClass = new ExperimentStartedInteractionClass();
			experimentStartedInteractionClass.subscribe();	
			
			// Listen when a Experiment is finished.
			experimentFinishedInteractionClass = new ExperimentFinishedInteractionClass();
			experimentFinishedInteractionClass.subscribe();
			
			// Listen when create instances
			instancesCreatedInteractionClass = new InstancesCreatedInteractionClass();
			instancesCreatedInteractionClass.subscribe();
			
			// Listen when fail to create instances
			instanceCreationErrorInteractionClass = new InstanceCreationErrorInteractionClass();
			instanceCreationErrorInteractionClass.subscribe();			
			
			// Listen to know about Gemini online
			geminiClass = new GeminiClass();
			geminiClass.subscribe();

			debug("done starting Federate.");
			
		} else {
			warn("Sagittarius is already running an Instance");
		}
	}
	
	public SagittariusClass getSagittariusClass() {
		return sagittariusClass;
	}
	
	public ScorpioClass getScorpioClass() {
		return scorpioClass;
	}
	
	public GeminiClass getGeminiClass() {
		return geminiClass;
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
			(new File(rootPath +  "/foms/gemini.xml")).toURI().toURL(),	
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
							warn("Returning Instance " + instance.getSerial() + " to buffer because " + e.getMessage() );
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

	public boolean isExperimentStartedInteraction(InteractionClassHandle interactionClassHandle) {
		return experimentStartedInteractionClass.getInteractionClassHandle().equals( interactionClassHandle );
	}

	public boolean isInstancesCreatedInteraction(InteractionClassHandle interactionClassHandle) {
		return instancesCreatedInteractionClass.getInteractionClassHandle().equals( interactionClassHandle );
	}

	public boolean isInstanceCreationErrorInteraction(InteractionClassHandle interactionClassHandle) {
		return instanceCreationErrorInteractionClass.getInteractionClassHandle().equals( interactionClassHandle );
	}

	public boolean isExperimentFinishedInteraction(InteractionClassHandle interactionClassHandle) {
		return experimentFinishedInteractionClass.getInteractionClassHandle().equals( interactionClassHandle );
	}

}

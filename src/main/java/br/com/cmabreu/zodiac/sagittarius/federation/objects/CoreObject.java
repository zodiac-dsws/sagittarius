package br.com.cmabreu.zodiac.sagittarius.federation.objects;

import java.util.Calendar;

import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.entity.Instance;
import br.com.cmabreu.zodiac.sagittarius.federation.federates.SagittariusFederate;
import br.com.cmabreu.zodiac.sagittarius.misc.ZipUtil;
import br.com.cmabreu.zodiac.sagittarius.types.InstanceStatus;
import hla.rti1516e.ObjectInstanceHandle;

public class CoreObject {
	private ObjectInstanceHandle instance;
	private boolean working = false;
	private String serial = "";
	private String experimentSerial = "*";
	private String instanceSerial = "*";
	private String fragmentSerial = "*";
	private String activitySerial = "*";
	private String executor = "*";
	private String executorType = "*";
	private String ownerNode = "";
	private String currentInstance = "*";
	
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
	
	public boolean getNextInstance() throws Exception {
		boolean result = false;
		Instance instance = SagittariusFederate.getInstance().getNextInstance( getOwnerNode() );
		if ( instance != null ) {
			currentInstance = encode( instance );
			result = true;
		}
		
		return result;
		
		/*
			if ( taskType.equals("nunki")) {
				instance = getNextJoinInstance( core.getOwnerNode() );
			}
		*/
		
	}
	
	// =====================================================================================
	// COMMON POJO
	// =====================================================================================
	public String getCurrentInstance() {
		return currentInstance;
	}
	
	public void setCurrentInstance(String currentInstance) {
		this.currentInstance = currentInstance;
	}
	
	public String getExecutor() {
		return executor;
	}
	
	public void setExecutor(String executor) {
		this.executor = executor;
	}
	
	public String getExecutorType() {
		return executorType;
	}
	
	public void setExecutorType(String executorType) {
		this.executorType = executorType;
	}
	
	public void setOwnerNode(String ownerNode) {
		this.ownerNode = ownerNode;
	}
	
	public String getOwnerNode() {
		return ownerNode;
	}
	
	public CoreObject( ObjectInstanceHandle instance ) {
		this.instance = instance;
	}
	
	public boolean isWorking() {
		return working;
	}

	public boolean isMe( ObjectInstanceHandle obj ) {
		return obj.equals( instance );
	}
	
	public String getSerial() {
		return serial;
	}

	public ObjectInstanceHandle getHandle() {
		return instance;
	}
	
	public void setSerial(String serial) {
		this.serial = serial;
	}
	
	public void setWorking(boolean working) {
		this.working = working;
	}

	public String getExperimentSerial() {
		return experimentSerial;
	}

	public void setExperimentSerial(String experimentSerial) {
		this.experimentSerial = experimentSerial;
	}

	public String getInstanceSerial() {
		return instanceSerial;
	}

	public void setInstanceSerial(String instanceSerial) {
		this.instanceSerial = instanceSerial;
	}

	public String getFragmentSerial() {
		return fragmentSerial;
	}

	public void setFragmentSerial(String fragmentSerial) {
		this.fragmentSerial = fragmentSerial;
	}

	public String getActivitySerial() {
		return activitySerial;
	}

	public void setActivitySerial(String activitySerial) {
		this.activitySerial = activitySerial;
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

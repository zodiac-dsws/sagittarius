package br.com.cmabreu.zodiac.sagittarius.federation.objects;

import br.com.cmabreu.zodiac.sagittarius.federation.classes.CoreStatus;
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
	private int result;
	private CoreStatus status = CoreStatus.NOT_OWNED;
	
	public int getResult() {
		return result;
	}
	
	public void setResult(int result) {
		this.result = result;
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

	public void setStatus(CoreStatus status) {
		this.status = status;
	}
	
	public CoreStatus getStatus() {
		return status;
	}
	
}

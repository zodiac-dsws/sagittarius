package br.com.cmabreu.zodiac.sagittarius.federation.classes;

import java.util.ArrayList;
import java.util.List;

import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.federation.EncoderDecoder;
import br.com.cmabreu.zodiac.sagittarius.federation.RTIAmbassadorProvider;
import br.com.cmabreu.zodiac.sagittarius.federation.federates.SagittariusFederate;
import br.com.cmabreu.zodiac.sagittarius.federation.objects.CoreObject;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.RTIexception;

public class CoreClass {
	private RTIambassador rtiamb;
	private ObjectClassHandle classHandle;
	private AttributeHandle isWorkingHandle;
	private AttributeHandle serialNumberHandle;
	private AttributeHandle ownerNodeHandle;
	
	private AttributeHandle experimentSerialHandle;
	private AttributeHandle fragmentSerialHandle;
	private AttributeHandle instanceSerialHandle;
	private AttributeHandle activitySerialHandle;
	private AttributeHandle executorHandle;
	private AttributeHandle executorTypeHandle;
	private AttributeHandle currentInstanceHandle;
	private AttributeHandle executionResultHandle;
	
	private AttributeHandleSet attributes;
	private List<CoreObject> cores;
	private EncoderDecoder encodec;
	
	public void finishInstanceAndRequestAttributeOwnerShip( ObjectInstanceHandle theObject ) throws Exception {
		try {
			
			CoreObject core = getCoreByHandle( theObject );
			String instanceSerial = core.getInstanceSerial();
			
			if ( !instanceSerial.equals("*") ) {
				debug("Finishing Instance " + instanceSerial + " with result " + core.getResult() );
				SagittariusFederate.getInstance().finishInstance( instanceSerial, core );
				core.setInstanceSerial("*");
			}

			RTIambassador rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
			AttributeHandleSet ahs = rtiamb.getAttributeHandleSetFactory().create();
			ahs.add( currentInstanceHandle );
			rtiamb.attributeOwnershipAcquisition( theObject, ahs, null );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}		
	
	public CoreObject getCoreByHandle(ObjectInstanceHandle theObject) {
		for ( CoreObject core : getCores() ) {
			if ( core.getHandle().equals( theObject ) ) return core;
		}
		return null;
	}

	public List<CoreObject> getCores() {
		return new ArrayList<CoreObject>(cores);
	}

	public CoreObject getCore( String coreSerial ) {
		for ( CoreObject core : getCores()  ) {
			if ( core.getSerial().equals( coreSerial ) ) return core; 
		}
		return null;
	}
	

	public void updateAttributeValuesObject( CoreObject core ) throws RTIexception {
		debug("Updating attributes of Core " + core.getSerial() + "@" + core.getOwnerNode() );
		HLAunicodeString currentInstanceHandleValue = encodec.createHLAunicodeString( core.getCurrentInstance() );
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(1);
		attributes.put( currentInstanceHandle, currentInstanceHandleValue.toByteArray() );
		rtiamb.updateAttributeValues( core.getHandle(), attributes, "Core Attributes".getBytes() );
	}	
	
	public void remove( ObjectInstanceHandle objHandle ) {
		for ( CoreObject core : getCores()  ) {
			if ( core.isMe( objHandle ) ) {
				debug( "Core " + core.getSerial() + " is offline." );
				cores.remove( core );
				return;
			}
		}		
	}

	public ObjectInstanceHandle createNew( ObjectInstanceHandle coreObjectHandle ) throws RTIexception {
		CoreObject core = new CoreObject( coreObjectHandle );
		cores.add( core );
		rtiamb.requestAttributeValueUpdate(coreObjectHandle, attributes, "Request Update".getBytes() );
		return coreObjectHandle;
	}
	
	public void updateWorkingDataCore( CoreObject core ) throws Exception {
		HLAunicodeString currentInstanceHandleValue = encodec.createHLAunicodeString( core.getCurrentInstance() );
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(1);
		attributes.put( currentInstanceHandle, currentInstanceHandleValue.toByteArray() );
		rtiamb.updateAttributeValues( core.getHandle(), attributes, "Core Working Data".getBytes() );
	}	
	
	public CoreObject reflectAttributeValues( AttributeHandleValueMap theAttributes, ObjectInstanceHandle theObject ) {
		// Find the Object instance
		for ( CoreObject core : getCores() ) {
			if( core.isMe( theObject) ) {
				// Update its attributes
				for( AttributeHandle attributeHandle : theAttributes.keySet() )	{
					if( attributeHandle.equals( isWorkingHandle) ) {
						core.setWorking(  encodec.toBoolean( theAttributes.get(attributeHandle) ) );
					}
					else if( attributeHandle.equals( experimentSerialHandle ) ) {
						core.setExperimentSerial( encodec.toString( theAttributes.get(attributeHandle)) );
					}
					else if( attributeHandle.equals( fragmentSerialHandle ) ) {
						core.setFragmentSerial( encodec.toString( theAttributes.get(attributeHandle)) );
					}
					else if( attributeHandle.equals( instanceSerialHandle ) ) {
						core.setInstanceSerial( encodec.toString( theAttributes.get(attributeHandle)) );
					}
					else if( attributeHandle.equals( activitySerialHandle ) ) {
						core.setActivitySerial( encodec.toString( theAttributes.get(attributeHandle)) );
					}
					else if( attributeHandle.equals( serialNumberHandle ) ) {
						core.setSerial( encodec.toString( theAttributes.get(attributeHandle)) );
					}
					else if( attributeHandle.equals( ownerNodeHandle ) ) {
						core.setOwnerNode( encodec.toString( theAttributes.get(attributeHandle)) );
					}

					else if( attributeHandle.equals( executionResultHandle ) ) {
						core.setResult( encodec.toInteger32( theAttributes.get(attributeHandle)) );
					}
					
					else if( attributeHandle.equals( executorHandle ) ) {
						core.setExecutor( encodec.toString( theAttributes.get(attributeHandle)) );
					}
					else if( attributeHandle.equals( executorTypeHandle ) ) {
						core.setExecutorType( encodec.toString( theAttributes.get(attributeHandle)) );
					}
					else if( attributeHandle.equals( currentInstanceHandle ) ) {
						core.setCurrentInstance( encodec.toString( theAttributes.get(attributeHandle)) );
					}
				}
				
				try {
					if ( core.getCurrentInstance().equals("*") ) {
						debug( "Core " + core.getSerial() + "@" + core.getOwnerNode() + " reporting. Requesting ownership..." );
						finishInstanceAndRequestAttributeOwnerShip( theObject );
					} else {
						// debug("Current Instance was changed to " + core.getCurrentInstance() );
					}
				} catch ( Exception e ) {
					e.printStackTrace();
				}
				
				
				return core;
			}
		}
		return null;
	}
	

	
	public boolean objectExists( ObjectInstanceHandle objHandle ) {
		for ( CoreObject object : getCores()  ) {
			if ( object.isMe( objHandle ) ) {
				return true;
			}
		}
		return false;
	}
	
	public ObjectClassHandle getClassHandle() {
		return classHandle;
	}
	
	public boolean isSameOf( ObjectClassHandle theObjectClass ) {
		return theObjectClass.equals( classHandle );
	}
	
	public CoreClass() throws Exception {
		rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
		this.classHandle = rtiamb.getObjectClassHandle( "HLAobjectRoot.Core" );
		
		this.isWorkingHandle = rtiamb.getAttributeHandle( classHandle, "IsWorking" );
		this.serialNumberHandle = rtiamb.getAttributeHandle( classHandle, "SerialNumber" );
		this.ownerNodeHandle = rtiamb.getAttributeHandle( classHandle, "OwnerNode" );

		this.experimentSerialHandle = rtiamb.getAttributeHandle( classHandle, "ExperimentSerial" );
		this.fragmentSerialHandle = rtiamb.getAttributeHandle( classHandle, "FragmentSerial" );
		this.instanceSerialHandle = rtiamb.getAttributeHandle( classHandle, "InstanceSerial" );
		this.activitySerialHandle = rtiamb.getAttributeHandle( classHandle, "ActivitySerial" );	
		this.executorHandle = rtiamb.getAttributeHandle( classHandle, "Executor" );
		this.executorTypeHandle = rtiamb.getAttributeHandle( classHandle, "ExecutorType" );
		this.currentInstanceHandle = rtiamb.getAttributeHandle( classHandle, "CurrentInstance" );	
		this.executionResultHandle = rtiamb.getAttributeHandle( classHandle, "LastExecutionResult" );

		
		this.attributes = rtiamb.getAttributeHandleSetFactory().create();
		
		attributes.add( isWorkingHandle );
		attributes.add( serialNumberHandle );
		attributes.add( ownerNodeHandle );
		attributes.add( experimentSerialHandle );
		attributes.add( fragmentSerialHandle );
		attributes.add( instanceSerialHandle );
		attributes.add( activitySerialHandle );
		attributes.add( executorHandle );
		attributes.add( executorTypeHandle );
		attributes.add( currentInstanceHandle );
		attributes.add( executionResultHandle );
		
		cores = new ArrayList<CoreObject>();
		encodec = new EncoderDecoder();
	}
	
	public void publishCurrentInstance() throws RTIexception {
		debug("publish Core Current Instance Attribute");
		AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
		attributes.add( currentInstanceHandle );
		rtiamb.publishObjectClassAttributes( classHandle, attributes );
	}
	
	/*
	public void publish() throws RTIexception {
		debug("publish Core Attributes");
		rtiamb.publishObjectClassAttributes( classHandle, attributes );
	}
	*/
	
	public void subscribe() throws RTIexception {
		debug("subscribe");
		rtiamb.subscribeObjectClassAttributes( classHandle, attributes );		
	}

	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}	

	
}

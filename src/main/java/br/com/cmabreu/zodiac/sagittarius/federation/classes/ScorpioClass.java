package br.com.cmabreu.zodiac.sagittarius.federation.classes;

import java.util.ArrayList;
import java.util.List;

import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.federation.EncoderDecoder;
import br.com.cmabreu.zodiac.sagittarius.federation.RTIAmbassadorProvider;
import br.com.cmabreu.zodiac.sagittarius.federation.objects.ScorpioObject;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.exceptions.RTIexception;

public class ScorpioClass {
	private ObjectClassHandle classHandle;
	private RTIambassador rtiamb;
	
	private AttributeHandle macAddressHandle;
	private AttributeHandle soNameHandle;
	private AttributeHandle machineNameHandle;
	private AttributeHandle cpuLoadHandle;
	private AttributeHandle availableProcessorsHandle;
	private AttributeHandle totalMemoryHandle;
	private AttributeHandle freeMemoryHandle;
	private AttributeHandle ipAddressHandle;
	
	private AttributeHandleSet attributes;
	
	private EncoderDecoder encodec;	
	private List<ScorpioObject> nodes;

	public List<ScorpioObject> getNodes() {
		return new ArrayList<ScorpioObject>( nodes );
	}
	
	public boolean objectExists( ObjectInstanceHandle objHandle ) {
		for ( ScorpioObject scorpio : getNodes()  ) {
			if ( scorpio.isMe( objHandle ) ) {
				return true;
			}
		}
		return false;
	}

	public void remove( ObjectInstanceHandle objHandle ) {
		for ( ScorpioObject node : getNodes()  ) {
			if ( node.isMe(objHandle ) ) {
				debug( "Node " + node.getMacAddress() + " is offline." );
				nodes.remove( node );
				return;
			}
		}		
	}
	
	public ScorpioObject createNew( ObjectInstanceHandle objectHandle ) throws Exception {
		debug("discovered new Scorpio node");
		ScorpioObject node = new ScorpioObject( objectHandle );
		nodes.add( node );
		return node;
	}
	
	public ObjectClassHandle getClassHandle() {
		return classHandle;
	}
	
	public boolean isSameOf( ObjectClassHandle theObjectClass ) {
		return theObjectClass.equals( classHandle );
	}
	
	public ScorpioClass( ) {
		try {
			nodes = new ArrayList<ScorpioObject>();
			
			rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
			this.classHandle = rtiamb.getObjectClassHandle( "HLAobjectRoot.Scorpio" );
			
			this.macAddressHandle = rtiamb.getAttributeHandle( classHandle, "MACAddress" );
			this.soNameHandle = rtiamb.getAttributeHandle( classHandle, "SOName" );
			this.machineNameHandle = rtiamb.getAttributeHandle( classHandle, "MachineName" );
			this.cpuLoadHandle = rtiamb.getAttributeHandle( classHandle, "CpuLoad" );
			this.availableProcessorsHandle = rtiamb.getAttributeHandle( classHandle, "AvailableProcessors" );
			this.totalMemoryHandle = rtiamb.getAttributeHandle( classHandle, "TotalMemory" );
			this.freeMemoryHandle = rtiamb.getAttributeHandle( classHandle, "FreeMemory" );
			this.ipAddressHandle = rtiamb.getAttributeHandle( classHandle, "IPAddress" );
			
			this.attributes = rtiamb.getAttributeHandleSetFactory().create();
			attributes.add( macAddressHandle );
			attributes.add( soNameHandle );
			attributes.add( machineNameHandle );
			attributes.add( cpuLoadHandle );
			attributes.add( availableProcessorsHandle );
			attributes.add( totalMemoryHandle );
			attributes.add( freeMemoryHandle );
			attributes.add( ipAddressHandle );
			
			encodec = new EncoderDecoder();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	
	public void publish() throws RTIexception {
		debug("publish");
		rtiamb.publishObjectClassAttributes( classHandle, attributes );
	}
	
	public void subscribe() throws RTIexception {
		debug("subscribe");
		rtiamb.subscribeObjectClassAttributes( classHandle, attributes );		
	}

	public ScorpioObject reflectAttributeValues( AttributeHandleValueMap theAttributes, ObjectInstanceHandle theObject ) {
		// Find the Object instance
		for ( ScorpioObject node : getNodes() ) {
			
			if( node.getHandle().equals( theObject) ) {
				// Update its attributes
				for( AttributeHandle attributeHandle : theAttributes.keySet() )	{	
					
					if( attributeHandle.equals( macAddressHandle) ) {
						node.setMacAddress( encodec.toString( theAttributes.get(attributeHandle) ) );
					} else
					if( attributeHandle.equals( soNameHandle) ) {
						node.setSoName( encodec.toString( theAttributes.get(attributeHandle) ) );
					} else 
					if( attributeHandle.equals( machineNameHandle) ) {
						node.setMachineName( encodec.toString( theAttributes.get(attributeHandle) ) );
					} else
					if( attributeHandle.equals( cpuLoadHandle) ) {
						node.setCpuLoad( encodec.toFloat64( theAttributes.get(attributeHandle) ) );
					} else  
					if( attributeHandle.equals( availableProcessorsHandle) ) {
						node.setAvailableProcessors( encodec.toInteger32( theAttributes.get(attributeHandle) ) );
					} else 
					if( attributeHandle.equals( totalMemoryHandle) ) {
						node.setTotalMemory( encodec.toInteger64( theAttributes.get(attributeHandle) ) );
					} else 
					if( attributeHandle.equals( freeMemoryHandle) ) {
						node.setFreeMemory( encodec.toInteger64( theAttributes.get(attributeHandle) ) );
					} else
					if( attributeHandle.equals( ipAddressHandle) ) {
						node.setIpAddress( encodec.toString( theAttributes.get(attributeHandle) ) );
					}  
				}
				return node;
			}
		}
		return null;
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

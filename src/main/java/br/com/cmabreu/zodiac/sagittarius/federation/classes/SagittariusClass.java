package br.com.cmabreu.zodiac.sagittarius.federation.classes;

import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.federation.EncoderDecoder;
import br.com.cmabreu.zodiac.sagittarius.federation.RTIAmbassadorProvider;
import br.com.cmabreu.zodiac.sagittarius.federation.federates.SagittariusFederate;
import br.com.cmabreu.zodiac.sagittarius.federation.objects.SagittariusObject;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.RTIexception;

public class SagittariusClass {
	private static ObjectClassHandle classHandle;
	private AttributeHandle macAddressAttributeHandle;
	
	private AttributeHandle instanceInputBufferHandle;
	private AttributeHandle instanceOutputBufferHandle;
	private AttributeHandle runningExperimentsHandle;
	private AttributeHandle bufferCurrentLoadHandle;
	
	private EncoderDecoder encodec;
	private AttributeHandleSet attributes;
	private RTIambassador rtiamb;

	private SagittariusObject sagittarius;
	
	public ObjectInstanceHandle getSagittariusObjectHandle() {
		return sagittarius.getHandle();
	}
	
	public void createNew() throws RTIexception {
		debug("new HLA Sagittarius Object instance created");
		ObjectInstanceHandle handle = rtiamb.registerObjectInstance( classHandle, "Sagittarius" );
		sagittarius = new SagittariusObject( handle );
	}	
	
	public SagittariusClass() {
		try {
			debug("new server");
			rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
			classHandle = rtiamb.getObjectClassHandle( "HLAobjectRoot.Sagittarius" );
			
			attributes = rtiamb.getAttributeHandleSetFactory().create();
			
			encodec = new EncoderDecoder();
			
			macAddressAttributeHandle = rtiamb.getAttributeHandle( classHandle, "MACAddress" );
			instanceInputBufferHandle = rtiamb.getAttributeHandle( classHandle, "InstanceInputBuffer" );
			instanceOutputBufferHandle = rtiamb.getAttributeHandle( classHandle, "InstanceOutputBuffer" );
			runningExperimentsHandle = rtiamb.getAttributeHandle( classHandle, "RunningExperiments" );
			bufferCurrentLoadHandle = rtiamb.getAttributeHandle( classHandle, "BufferCurrentLoad" );
			
			attributes.add( macAddressAttributeHandle );
			attributes.add( instanceInputBufferHandle );
			attributes.add( instanceOutputBufferHandle );
			attributes.add( runningExperimentsHandle );
			attributes.add( bufferCurrentLoadHandle );
			
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public void updateAttributeValues() throws Exception {
		debug("updating attributes");
		String macAddress = sagittarius.getMacAddress();
		int instanceInputBuffer = SagittariusFederate.getInstance().getInstanceBuffer().getInstanceInputBufferSize();
		int bufferCurrentLoad = SagittariusFederate.getInstance().getInstanceBuffer().getBufferCurrentLoad();
		int instanceOutputBuffer = SagittariusFederate.getInstance().getInstanceBuffer().getInstanceOutputBufferSize();
		int runningExperiments = SagittariusFederate.getInstance().getRunningExperiments().size();
		
		ObjectInstanceHandle objectInstanceHandle = sagittarius.getHandle();
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(5);
		
		HLAunicodeString macAddressValue = encodec.createHLAunicodeString( macAddress );
		HLAinteger32BE instanceInputBufferValue = encodec.createHLAinteger32BE( instanceInputBuffer );
		HLAinteger32BE instanceOutputBufferValue = encodec.createHLAinteger32BE( instanceOutputBuffer );
		HLAinteger32BE runningExperimentsValue = encodec.createHLAinteger32BE( runningExperiments );
		HLAinteger32BE bufferCurrentLoadValue = encodec.createHLAinteger32BE( bufferCurrentLoad );
		
		attributes.put( macAddressAttributeHandle, macAddressValue.toByteArray() );
		attributes.put( instanceInputBufferHandle, instanceInputBufferValue.toByteArray() );
		attributes.put( instanceOutputBufferHandle, instanceOutputBufferValue.toByteArray() );
		attributes.put( runningExperimentsHandle, runningExperimentsValue.toByteArray() );
		attributes.put( bufferCurrentLoadHandle, bufferCurrentLoadValue.toByteArray() );
		
		rtiamb.updateAttributeValues( objectInstanceHandle, attributes, "Sagittarius Attributes".getBytes() );
	}
	
	public ObjectClassHandle getClassHandle() {
		return classHandle;
	}
	
	public boolean isSameOf( ObjectClassHandle theObjectClass ) {
		return theObjectClass.equals( classHandle );
	}
	
	public void publish() throws RTIexception {
		debug("publish");
		rtiamb.publishObjectClassAttributes( classHandle, attributes );
	}
	
	public void subscribe() throws RTIexception {
		debug("subscribe");
		rtiamb.subscribeObjectClassAttributes( classHandle, attributes );
	}	
	
	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}

	private void error( String s ) {
		Logger.getInstance().error(this.getClass().getName(), s );
	}

	public void provideAttributeValueUpdate(ObjectInstanceHandle theObject, AttributeHandleSet theAttributes) {
		try {
			updateAttributeValues();
		} catch ( Exception e ) {
			error("Error sending attributes to RTI under request.");
		}
	}		

}

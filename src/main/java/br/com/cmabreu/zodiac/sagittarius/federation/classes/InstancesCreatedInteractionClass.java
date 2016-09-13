package br.com.cmabreu.zodiac.sagittarius.federation.classes;

import br.com.cmabreu.zodiac.sagittarius.federation.EncoderDecoder;
import br.com.cmabreu.zodiac.sagittarius.federation.RTIAmbassadorProvider;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAunicodeString;

public class InstancesCreatedInteractionClass {
	private InteractionClassHandle instancesCreatedInteractionHandle;
	private ParameterHandle experimentSerialParameterHandle;
	private ParameterHandle instanceCountParameterHandle;
	private EncoderDecoder encodec;
	
	private RTIambassador rtiamb;

	public InteractionClassHandle getInteractionClassHandle() {
		return instancesCreatedInteractionHandle;
	}
	
	public InstancesCreatedInteractionClass() throws Exception {
		rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
		instancesCreatedInteractionHandle = rtiamb.getInteractionClassHandle( "HLAinteractionRoot.InstancesCreated" );
		experimentSerialParameterHandle = rtiamb.getParameterHandle( instancesCreatedInteractionHandle, "ExperimentSerial" );
		instanceCountParameterHandle = rtiamb.getParameterHandle( instancesCreatedInteractionHandle, "InstanceCount" );
		encodec = new EncoderDecoder();
	}
	
	public String getExperimentSerial( ParameterHandleValueMap parameters ) {
		String experimentSerial = encodec.toString( parameters.get( experimentSerialParameterHandle ) );
		return experimentSerial;
	}	
	
	public int getInstanceCount( ParameterHandleValueMap parameters ) {
		int instanceCount = encodec.toInteger32( parameters.get( experimentSerialParameterHandle ) );
		return instanceCount;
	}
	
	public void send( String experimentSerial, int instanceCount ) throws Exception {
		HLAunicodeString experimentSerialValue = encodec.createHLAunicodeString( experimentSerial );
		HLAinteger32BE instanceCountValue = encodec.createHLAinteger32BE( instanceCount );
		
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);
		parameters.put( experimentSerialParameterHandle, experimentSerialValue.toByteArray() );
		parameters.put( instanceCountParameterHandle, instanceCountValue.toByteArray() );
		
		rtiamb.sendInteraction( instancesCreatedInteractionHandle, parameters, "Instances Created".getBytes() );		
	}

	public void publish() throws Exception {
		rtiamb.publishInteractionClass( instancesCreatedInteractionHandle );
	}
	
	public void subscribe() throws Exception {
		rtiamb.subscribeInteractionClass( instancesCreatedInteractionHandle );		
	}
	
}

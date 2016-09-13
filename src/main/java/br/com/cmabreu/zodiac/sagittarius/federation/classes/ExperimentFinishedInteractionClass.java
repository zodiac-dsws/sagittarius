package br.com.cmabreu.zodiac.sagittarius.federation.classes;



import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.federation.EncoderDecoder;
import br.com.cmabreu.zodiac.sagittarius.federation.RTIAmbassadorProvider;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.HLAunicodeString;

public class ExperimentFinishedInteractionClass {
	private InteractionClassHandle experimentFinishedInteractionHandle;
	private ParameterHandle experimentSerialParameterHandle;
	private EncoderDecoder encodec;
	
	private RTIambassador rtiamb;

	public ExperimentFinishedInteractionClass() throws Exception {
		rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
		experimentFinishedInteractionHandle = rtiamb.getInteractionClassHandle( "HLAinteractionRoot.ExperimentFinished" );
		experimentSerialParameterHandle = rtiamb.getParameterHandle( experimentFinishedInteractionHandle, "ExperimentSerial" );
		encodec = new EncoderDecoder();
	}
	
	public String getExperimentSerial( ParameterHandleValueMap parameters ) {
		String experimentSerial = encodec.toString( parameters.get( experimentSerialParameterHandle ) );
		return experimentSerial;
	}	
	
	public void send( String experimentSerial ) throws Exception {
		HLAunicodeString experimentSerialValue = encodec.createHLAunicodeString( experimentSerial );
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);
		parameters.put( experimentSerialParameterHandle, experimentSerialValue.toByteArray() );
		String text = "Experiment "+experimentSerial+" Finished";
		rtiamb.sendInteraction( experimentFinishedInteractionHandle, parameters, text.getBytes() );		
	}

	public void publish() throws Exception {
		debug("publish");
		rtiamb.publishInteractionClass( experimentFinishedInteractionHandle );
	}
	
	public void subscribe() throws Exception {
		debug("subscribe");
		rtiamb.subscribeInteractionClass( experimentFinishedInteractionHandle );		
	}

	public Object getInteractionClassHandle() {
		return experimentFinishedInteractionHandle;
	}
	
	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}		

	
}

package br.com.cmabreu.zodiac.sagittarius.federation.classes;

import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.federation.EncoderDecoder;
import br.com.cmabreu.zodiac.sagittarius.federation.RTIAmbassadorProvider;
import br.com.cmabreu.zodiac.sagittarius.federation.objects.SagittariusObject;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.RTIexception;

public class SagittariusClass {
	private static ObjectClassHandle classHandle;
	private AttributeHandle macAddressAttributeHandle;
	private EncoderDecoder encodec;
	private AttributeHandleSet attributes;
	private RTIambassador rtiamb;

	private SagittariusObject sagittarius;
	
	public ObjectInstanceHandle getSagittariusObjectHandle() {
		return sagittarius.getHandle();
	}
	
	public void createNew() throws RTIexception {
		error("new HLA Sagittarius Object instance created");
		ObjectInstanceHandle handle = rtiamb.registerObjectInstance( classHandle, "Sagittarius" );
		sagittarius = new SagittariusObject( handle );
	}	
	
	public SagittariusClass() {
		try {
			error("new server");
			rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
	
			classHandle = rtiamb.getObjectClassHandle( "HLAobjectRoot.Sagittarius" );
			attributes = rtiamb.getAttributeHandleSetFactory().create();
			encodec = new EncoderDecoder();
			
			error("registering attributes");
			macAddressAttributeHandle = rtiamb.getAttributeHandle( classHandle, "MACAddress" );
			attributes.add( macAddressAttributeHandle );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public void updateAttributeValues() throws RTIexception {
		error("updating attributes");
		String macAddress = sagittarius.getMacAddress();
		ObjectInstanceHandle objectInstanceHandle = sagittarius.getHandle();
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(1);
		HLAunicodeString macAddressValue = encodec.createHLAunicodeString( macAddress );
		attributes.put( macAddressAttributeHandle, macAddressValue.toByteArray() );
		rtiamb.updateAttributeValues( objectInstanceHandle, attributes, "Sagittarius Attributes".getBytes() );
	}
	
	public ObjectClassHandle getClassHandle() {
		return classHandle;
	}
	
	public boolean isSameOf( ObjectClassHandle theObjectClass ) {
		return theObjectClass.equals( classHandle );
	}
	
	public void publish() throws RTIexception {
		error("publish");
		rtiamb.publishObjectClassAttributes( classHandle, attributes );
	}
	
	public void subscribe() throws RTIexception {

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

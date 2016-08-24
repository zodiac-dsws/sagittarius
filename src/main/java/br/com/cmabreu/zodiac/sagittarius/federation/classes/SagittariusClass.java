package br.com.cmabreu.zodiac.sagittarius.federation.classes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private Logger logger = LogManager.getLogger( this.getClass().getName() );

	private SagittariusObject sagittarius;
	
	public void createNew() throws RTIexception {
		logger.debug("new HLA Sagittarius Object instance created");
		ObjectInstanceHandle handle = rtiamb.registerObjectInstance( classHandle, "Sagittarius" );
		sagittarius = new SagittariusObject( handle );
	}	
	
	
	public SagittariusClass() {
		try {
			logger.debug("new server");
			rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
	
			classHandle = rtiamb.getObjectClassHandle( "HLAobjectRoot.Sagittarius" );
			attributes = rtiamb.getAttributeHandleSetFactory().create();
			encodec = new EncoderDecoder();
			
			logger.debug("registering attributes");
			macAddressAttributeHandle = rtiamb.getAttributeHandle( classHandle, "MACAddress" );
			attributes.add( macAddressAttributeHandle );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public void updateAttributeValues() throws RTIexception {
		logger.debug("updating attributes");
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
		logger.debug("publish");
		rtiamb.publishObjectClassAttributes( classHandle, attributes );
	}
	
	public void subscribe() throws RTIexception {

	}	

}

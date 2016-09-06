package br.com.cmabreu.zodiac.sagittarius.federation.classes;

import java.util.ArrayList;
import java.util.List;

import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.federation.RTIAmbassadorProvider;
import br.com.cmabreu.zodiac.sagittarius.federation.federates.SagittariusFederate;
import br.com.cmabreu.zodiac.sagittarius.federation.objects.GeminiObject;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.exceptions.RTIexception;

public class GeminiClass {
	private static ObjectClassHandle classHandle;
	private AttributeHandle macAddressAttributeHandle;
	private AttributeHandleSet attributes;
	private RTIambassador rtiamb;
	private List<GeminiObject> geminiList;
	
	public void createNew( ObjectInstanceHandle handle ) throws RTIexception {
		debug("Found new Gemini");
		GeminiObject gemini = new GeminiObject( handle );
		geminiList.add( gemini );
		
		// This is the first Gemini in Network. Check if we have Instances to create
		if ( geminiList.size() == 1 ) {
			try {
				SagittariusFederate.getInstance().requestCreateInstancesAgain();
			} catch ( Exception e ) {
				error("Error requesting Instance genesis to Gemini: " + e.getMessage() );
			}
		} else {
			debug("Already have a working Gemini. Nothing to do.");
		}
	}	
	
	public List<GeminiObject> getGeminiList() {
		return geminiList;
	}
	
	public GeminiClass() {
		try {
			rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
			classHandle = rtiamb.getObjectClassHandle( "HLAobjectRoot.Gemini" );
			attributes = rtiamb.getAttributeHandleSetFactory().create();
			macAddressAttributeHandle = rtiamb.getAttributeHandle( classHandle, "MACAddress" );
			attributes.add( macAddressAttributeHandle );
			geminiList = new ArrayList<GeminiObject>();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
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

	private void warn( String s ) {
		Logger.getInstance().warn(this.getClass().getName(), s );
	}

	public void remove(ObjectInstanceHandle theObject) {
		for ( GeminiObject go : getGeminiList() ) {
			if( go.getHandle().equals( theObject ) ) {
				warn("Gemini " + go.getMacAddress() + " is offline");
				geminiList.remove( go );
				break;
			}
		}
	}

	public boolean objectExists(ObjectInstanceHandle theObject) {
		for ( GeminiObject go : getGeminiList() ) {
			if( go.getHandle().equals( theObject ) ) {
				return true;
			}
		}
		return false;
	}

	
}

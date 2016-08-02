package br.com.cmabreu.zodiac.sagittarius.federation;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;

public class RTIAmbassadorProvider {
	private static RTIAmbassadorProvider instance;
	private RTIambassador rtiamb;
	private SagittariusAmbassador myAmb;

	private RTIAmbassadorProvider() throws Exception {
		RtiFactory factory = RtiFactoryFactory.getRtiFactory();
		rtiamb = factory.getRtiAmbassador();
		myAmb = new SagittariusAmbassador();
		rtiamb.connect(myAmb, CallbackModel.HLA_IMMEDIATE);
	}
	
	public static RTIAmbassadorProvider getInstance() throws Exception {
		if ( instance == null ) {
			instance = new RTIAmbassadorProvider();
		}
		return instance;
	}
	
	public SagittariusAmbassador getSagitariiAmbassador() {
		return myAmb;
	}
	
	public RTIambassador getRTIAmbassador() {
		return rtiamb;
	}
	
	
}

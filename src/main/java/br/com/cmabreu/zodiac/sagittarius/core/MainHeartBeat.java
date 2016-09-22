package br.com.cmabreu.zodiac.sagittarius.core;

import br.com.cmabreu.zodiac.sagittarius.federation.federates.SagittariusFederate;

public class MainHeartBeat implements Runnable {
	
    @Override
    public void run() {
    	try {
			SagittariusFederate.getInstance().loadBuffers();
			SagittariusFederate.getInstance().getSagittariusClass().updateAttributeValues();
			
			if ( SagittariusFederate.getInstance().mustCheckCores() ) {
				SagittariusFederate.getInstance().checkCores();
			}
			
    	} catch ( Exception e ) {
    		
    	}
    }
	

}

package br.com.cmabreu.zodiac.sagittarius.core;

import br.com.cmabreu.zodiac.sagittarius.federation.federates.SagittariusFederate;

public class MainHeartBeat implements Runnable {
	private int totalPending = 0;
	
    @Override
    public void run() {
    	try {
    		int newTotalPending = SagittariusFederate.getInstance().loadBuffers();
			System.out.println("Pending instances before : " + totalPending );
			System.out.println("Pending instances now    : " + newTotalPending );
			
    		if ( (totalPending == 0) && (newTotalPending > 0) ) {
    			SagittariusFederate.getInstance().checkCores();
    		}
    		
    		totalPending = newTotalPending;
    		
    	} catch ( Exception e ) {
    		
    	}
    }
	

}

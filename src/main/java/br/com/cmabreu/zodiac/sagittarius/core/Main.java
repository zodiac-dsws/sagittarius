package br.com.cmabreu.zodiac.sagittarius.core;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import br.com.cmabreu.zodiac.sagittarius.core.config.Configurator;
import br.com.cmabreu.zodiac.sagittarius.entity.Experiment;
import br.com.cmabreu.zodiac.sagittarius.exceptions.NotFoundException;
import br.com.cmabreu.zodiac.sagittarius.federation.federates.SagittariusFederate;
import br.com.cmabreu.zodiac.sagittarius.infra.ConnFactory;
import br.com.cmabreu.zodiac.sagittarius.services.ExperimentService;

public class Main {
	private ScheduledExecutorService scheduler;
	
    private void loggerDebug( String log ) {
    	System.out.println( "[DEBUG] Main " + log );
    }
    
    private void loggerError( String log ){
    	System.out.println( "[ERROR] Main " + log );
    }


    public static void main( String[] args ) {
    	System.out.println("Starting Sagittarius...");
    	new Main().initialize();
    	
    }
    
    public void initialize() {

    	try {
    		int interval = 5;
    		int maxInputBufferCapacity = 500;
    		
    		Logger.getInstance().enable();
       
			Configurator config = Configurator.getInstance("config.xml");
			config.loadMainConfig();
			
			interval = config.getPoolIntervalSeconds();

			maxInputBufferCapacity = config.getMaxInputBufferCapacity();
			
			String user = config.getUserName();
			String passwd = config.getPassword();
			String database = config.getDatabaseName();
    		
			loggerDebug("Credentials: " + user + " | " + database);
			
    		ConnFactory.setCredentials(user, passwd, database);

   		
			loggerDebug("check for interrupted work");	
			try {
				ExperimentService ws = new ExperimentService();
				List<Experiment> running = ws.getRunning();
				SagittariusFederate.getInstance().setRunningExperiments( running );
				SagittariusFederate.getInstance().reloadAfterCrash();
				loggerDebug("found " + SagittariusFederate.getInstance().getRunningExperiments().size() + " running experiments");	
			} catch ( NotFoundException e ) {
				loggerDebug("no running experiments found");	
			}			

	       	        
	        loggerDebug("Buffer cabacity " + maxInputBufferCapacity );
	        SagittariusFederate.getInstance().setMaxInputBufferCapacity( maxInputBufferCapacity );
	        SagittariusFederate.getInstance().startServer();
	        SagittariusFederate.getInstance().loadBuffers();

			scheduler = Executors.newSingleThreadScheduledExecutor();
	        MainHeartBeat as = new MainHeartBeat();
	        scheduler.scheduleAtFixedRate(as, interval, interval , TimeUnit.SECONDS);


		} catch (Exception e) { 
			System.out.println( e.getMessage() );
			loggerError( e.getMessage() );
			//e.printStackTrace(); 
		}
        
        
	}
	

}

package br.com.cmabreu.zodiac.sagittarius.federation;


import br.com.cmabreu.zodiac.sagittarius.core.Logger;
import br.com.cmabreu.zodiac.sagittarius.federation.federates.SagittariusFederate;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;


public class SagittariusAmbassador extends NullFederateAmbassador {
	
	@Override
	public void provideAttributeValueUpdate(ObjectInstanceHandle theObject,	AttributeHandleSet theAttributes, byte[] userSuppliedTag)
			throws FederateInternalError {

		try {
			SagittariusFederate.getInstance().getSagittariusClass().provideAttributeValueUpdate(theObject, theAttributes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}	
	
	@Override
	public void reflectAttributeValues( ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
	                                    byte[] tag, OrderType sentOrder, TransportationTypeHandle transport,
	                                    SupplementalReflectInfo reflectInfo ) throws FederateInternalError {
		try {
			SagittariusFederate.getInstance().reflectAttributeUpdate( theObject, theAttributes );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
	}

	
	@Override
    public void requestAttributeOwnershipRelease( ObjectInstanceHandle theObject, AttributeHandleSet candidateAttributes, byte[] userSuppliedTag) throws FederateInternalError {
		try {
			SagittariusFederate.getInstance().releaseAttributeOwnership(theObject, candidateAttributes);
		} catch ( Exception e ) {
			e.printStackTrace();
		}
    }		
	
	@Override
	public void discoverObjectInstance( ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass, String objectName ) throws FederateInternalError {
		try {

			if ( SagittariusFederate.getInstance().getGeminiClass().isSameOf( theObjectClass ) ) {
				try {
					debug("New Gemini discovered (" + objectName + ")");
					SagittariusFederate.getInstance().getGeminiClass().createNew( theObject );
					
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			
			
			if ( SagittariusFederate.getInstance().getCoreClass().isSameOf( theObjectClass ) ) {
				try {
					debug("New Core discovered (" + objectName + ")");
					SagittariusFederate.getInstance().getCoreClass().createNew( theObject );
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			
			if ( SagittariusFederate.getInstance().getScorpioClass().isSameOf( theObjectClass ) ) {
				try {
					debug("New Scorpio discovered (" + objectName + ")");
					SagittariusFederate.getInstance().getScorpioClass().createNew( theObject );
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			
		} catch ( Exception e ) {
			error( e.getMessage() );
		}
		
	}
	
	@Override
	public void attributeOwnershipAcquisitionNotification(	ObjectInstanceHandle theObject,	AttributeHandleSet securedAttributes, byte[] userSuppliedTag) throws FederateInternalError {

		try {
			SagittariusFederate.getInstance().attributeOwnershipAcquisitionNotification( theObject, securedAttributes );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}	
	
	
	@Override
	public void removeObjectInstance(ObjectInstanceHandle theObject, byte[] userSuppliedTag, OrderType sentOrdering, SupplementalRemoveInfo removeInfo)	{
		try { 
			if ( SagittariusFederate.getInstance().getScorpioClass().objectExists(theObject) ) {
				try {
					debug("Remove Scorpio object " );
					SagittariusFederate.getInstance().getScorpioClass().remove( theObject );
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			
			
			if ( SagittariusFederate.getInstance().getCoreClass().objectExists(theObject) ) {
				try {
					debug("Remove Core object " );
					SagittariusFederate.getInstance().getCoreClass().remove( theObject );
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			
			
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}	
	
	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass,	ParameterHandleValueMap theParameters,
			byte[] userSuppliedTag, OrderType sentOrdering,	TransportationTypeHandle theTransport, 
			SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
		
		String message = new String ( userSuppliedTag );
		debug( "Notification received: " + message );
		
		try {
			if ( SagittariusFederate.getInstance().isExperimentStartedInteraction( interactionClass ) ) {
				SagittariusFederate.getInstance().experimentStarted( theParameters );
			}
			
			if ( SagittariusFederate.getInstance().isInstancesCreatedInteraction( interactionClass ) ) {
				SagittariusFederate.getInstance().instancesCreated( theParameters );
			}			
			
			if ( SagittariusFederate.getInstance().isInstanceCreationErrorInteraction( interactionClass ) ) {
				SagittariusFederate.getInstance().instanceCreationError( theParameters );
			}			
			
			if ( SagittariusFederate.getInstance().isExperimentFinishedInteraction( interactionClass ) ) {
				SagittariusFederate.getInstance().finishExperiment(theParameters);
			}			
			
			
			
			
		} catch ( Exception e ) {
			error("Error receiving notification: " + e.getMessage() );
		}
	}
	
	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}	

	private void error( String s ) {
		Logger.getInstance().error(this.getClass().getName(), s );
	}		
	
}

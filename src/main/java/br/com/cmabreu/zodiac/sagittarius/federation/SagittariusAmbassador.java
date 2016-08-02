package br.com.cmabreu.zodiac.sagittarius.federation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.com.cmabreu.zodiac.sagittarius.federation.federates.SagittariusFederate;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;


public class SagittariusAmbassador extends NullFederateAmbassador {
	private Logger logger = LogManager.getLogger( this.getClass().getName() );

	@Override
	public void reflectAttributeValues( ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
	                                    byte[] tag, OrderType sentOrder, TransportationTypeHandle transport,
	                                    SupplementalReflectInfo reflectInfo ) throws FederateInternalError {
		
		reflectAttributeValues( theObject, theAttributes, tag, sentOrder, transport, null, sentOrder, reflectInfo );
			
	}

	
	@Override
	public void reflectAttributeValues( ObjectInstanceHandle theObject,  AttributeHandleValueMap theAttributes,
	                                    byte[] tag,  OrderType sentOrdering, TransportationTypeHandle theTransport,
	                                    LogicalTime time,  OrderType receivedOrdering, SupplementalReflectInfo reflectInfo ) throws FederateInternalError {
		try {
			if ( SagittariusFederate.getInstance().getCoreClass().objectExists( theObject ) ) {
				SagittariusFederate.getInstance().getCoreClass().reflectAttributeValues( theAttributes, theObject );
			} else 
			if ( SagittariusFederate.getInstance().getTeapotClass().objectExists( theObject ) ) {
				SagittariusFederate.getInstance().getTeapotClass().reflectAttributeValues( theAttributes, theObject );
			}
		} catch ( Exception e ) {
			e.printStackTrace(); 
		}
	}
	
	@Override
	public void discoverObjectInstance( ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass, String objectName ) throws FederateInternalError {
		try {
			if ( SagittariusFederate.getInstance().getCoreClass().isSameOf( theObjectClass ) ) {
				try {
					logger.debug("New Core object " + theObject + " discovered (" + objectName + ")");
					SagittariusFederate.getInstance().getCoreClass().createNew( theObject );
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			
			if ( SagittariusFederate.getInstance().getTeapotClass().isSameOf( theObjectClass ) ) {
				try {
					logger.debug("New Teapot object " + theObject + " discovered (" + objectName + ")");
					SagittariusFederate.getInstance().getTeapotClass().createNew( theObject );
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			
		} catch ( Exception e ) {
			logger.error( e.getMessage() );
		}
		
	}
	
	@Override
	public void removeObjectInstance(ObjectInstanceHandle theObject, byte[] userSuppliedTag, OrderType sentOrdering, SupplementalRemoveInfo removeInfo)	{
		try { 
			if ( SagittariusFederate.getInstance().getTeapotClass().objectExists(theObject) ) {
				try {
					logger.debug("Remove Teapot object " );
					SagittariusFederate.getInstance().getTeapotClass().remove( theObject );
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			
			
			if ( SagittariusFederate.getInstance().getCoreClass().objectExists(theObject) ) {
				try {
					logger.debug("Remove Core object " );
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
		
		//String tag = new String( userSuppliedTag );
		
		try {
			SagittariusFederate sagitarii = SagittariusFederate.getInstance(); 
			
			if ( sagitarii.getRequestTaskInteractionClass().isMe(interactionClass) ) {
				sagitarii.sendInstancesToNode( theParameters );
			}
			
			if ( sagitarii.getFinishedInstanceInteractionClass().isMe(interactionClass) ) {
				sagitarii.finishInstance(theParameters);
			}
			
		} catch ( Exception e ) {
			
		}
		
	}
	
	
	
}

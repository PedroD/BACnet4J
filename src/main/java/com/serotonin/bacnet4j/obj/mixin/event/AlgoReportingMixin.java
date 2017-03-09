package com.serotonin.bacnet4j.obj.mixin.event;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.obj.EventEnrollmentObject;
import com.serotonin.bacnet4j.obj.mixin.event.eventAlgo.EventAlgorithm;
import com.serotonin.bacnet4j.obj.mixin.event.faultAlgo.FaultAlgorithm;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.DeviceObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.FaultParameter.AbstractFaultParameter;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.enumerated.EventState;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.enumerated.Reliability;
import com.serotonin.bacnet4j.type.eventParameter.AbstractEventParameter;
import com.serotonin.bacnet4j.type.notificationParameters.NotificationParameters;

/**
 * Provides support for algorithmic reporting, particularly for the EventEnrollment object.
 *
 * @author Matthew
 */
public class AlgoReportingMixin extends EventReportingMixin {
    static final Logger LOG = LoggerFactory.getLogger(AlgoReportingMixin.class);

    private final AbstractEventParameter eventParameter;
    private final AbstractFaultParameter faultParameter;
    private final DeviceObjectPropertyReference objectPropertyReference;

    private Encodable monitoredPropertyValue;
    private Map<PropertyIdentifier, Encodable> additionalValues;

    public AlgoReportingMixin(final EventEnrollmentObject ee, final EventAlgorithm eventAlgo,
            final AbstractEventParameter eventParameter, final FaultAlgorithm faultAlgo,
            final AbstractFaultParameter faultParameter, final DeviceObjectPropertyReference objectPropertyReference) {
        super(ee, eventAlgo, faultAlgo);
        this.eventParameter = eventParameter;
        this.faultParameter = faultParameter;
        this.objectPropertyReference = objectPropertyReference;
    }

    public synchronized void updateValue(final Encodable newValue,
            final Map<PropertyIdentifier, Encodable> additionalValues) {
        final Encodable oldValue = monitoredPropertyValue;
        monitoredPropertyValue = newValue;
        this.additionalValues = additionalValues;

        // Check if the value has changed to a fault value.
        final boolean fault = executeFaultAlgo(oldValue, monitoredPropertyValue);

        if (!fault) {
            // Ensure there is no current fault.
            final Reliability reli = get(PropertyIdentifier.reliability);
            if (reli == null || reli.equals(Reliability.noFaultDetected))
                // No fault detected. Run the event algorithm
                executeEventAlgo();
        }
    }

    @Override
    protected StateTransition evaluateEventState(final BACnetObject bo, final EventAlgorithm eventAlgo) {
        return eventAlgo.evaluateAlgorithmicEventState(bo, monitoredPropertyValue, eventParameter);
    }

    @Override
    protected NotificationParameters getNotificationParameters(final EventState fromState, final EventState toState,
            final BACnetObject bo, final EventAlgorithm eventAlgo) {
        return eventAlgo.getAlgorithmicNotificationParameters(fromState, toState, monitoredPropertyValue,
                additionalValues, eventParameter);
    }

    @Override
    protected Reliability evaluateFaultState(final Encodable oldMonitoredValue, final Encodable newMonitoredValue,
            final BACnetObject bo, final FaultAlgorithm faultAlgo) {
        return faultAlgo.evaluateAlgorithmic(oldMonitoredValue, newMonitoredValue,
                bo.get(PropertyIdentifier.reliability), faultParameter);
    }

    @Override
    protected PropertyValue getEventEnrollmentMonitoredProperty(final PropertyIdentifier pid) {
        if (pid == null)
            return new PropertyValue(objectPropertyReference.getPropertyIdentifier(),
                    objectPropertyReference.getPropertyArrayIndex(), monitoredPropertyValue, null);

        final Encodable value = additionalValues.get(pid);
        if (value == null) {
            LOG.debug("Could not find property {} in additional polled properties", pid);
            return null;
        }

        return new PropertyValue(pid, value);
    }
}

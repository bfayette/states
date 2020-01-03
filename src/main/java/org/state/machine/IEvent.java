package org.state.machine;

import org.state.machine.approbation.Status.ExtendedStatus;

public interface IEvent {
	String getEventName();
	int incrementStep(ExtendedStatus status);
}

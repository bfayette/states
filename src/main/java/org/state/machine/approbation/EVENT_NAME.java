package org.state.machine.approbation;

import org.apache.commons.lang3.StringUtils;
import org.state.machine.IEvent;
import org.state.machine.approbation.Status.ExtendedStatus;

public enum EVENT_NAME implements IEvent {
	SEND,
	APPROVE,
	RECOMMEND,
	REJECT,
	REWORK,
	REOPEN,
	NONE {
		@Override
		public int incrementStep(ExtendedStatus status) {
			throw new UnsupportedOperationException();
		}
	};
	
	public String getDisplayName() {		
		return getEventName();
	}

	public String getEventName() {
		return this.name().toUpperCase();
	}

	public static EVENT_NAME findByName(String str) {
		EVENT_NAME found = EVENT_NAME.NONE;
		for (EVENT_NAME evt : EVENT_NAME.values()) {
			if (StringUtils.equalsIgnoreCase(evt.name(), str) 
					|| StringUtils.equalsIgnoreCase(evt.getDisplayName(), str)) {
				found = evt;
				break;
			}
		}
		return found;
	}

	public int incrementStep(ExtendedStatus status) {
		int nextStep = status.getSequence();
		if (REWORK.equals(this)) {
			nextStep = 0;
		} else {
			if(status.isNotRejected()) { 
				//Meaning previous status rejected do nothng
				nextStep += 1;
			}			
		}
		return Math.max(0, nextStep);
	}

	public String[] responsibleApprobationRole() {
		throw new UnsupportedOperationException();
	}
}

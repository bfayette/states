package org.state.machine.approbation;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.EnumSet;
import org.state.machine.StateMachineException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor @Getter
public enum Status implements IStatus {

	IN_PROGRESS(1, "In Progress"),
    SUBMITTED(2, "Submitted"),
    RECOMMENDED(3, "Recommended"),
    APPROVED(3, "Approved"),
    REJECTED(4, "Rejected");

    private int id;
    private String description;

    
    public boolean isNotApproved() {
		return !isApproved();
	}
    
    public boolean isApproved() {
		return Status.APPROVED.equals(this);
	}
    
    public boolean isInProgress() {
		return Status.IN_PROGRESS.equals(this);
	}
    
    public boolean isNotRejected() {
  		return !isRejected();
  	}
    
    @Override
    public boolean isActive() {
    	return EnumSet.of(APPROVED, REJECTED, RECOMMENDED, SUBMITTED, IN_PROGRESS).contains(this);
    }
    
    @Override
    public boolean isApprobationProcessInProgress() {
    	return EnumSet.of(APPROVED, REJECTED, RECOMMENDED, SUBMITTED).contains(this);
    }
      
      public boolean isRejected() {
  		return Status.REJECTED.equals(this);
  	}

    public static Status parseId(long id) {
    	Status found = null;
        for (Status status : Status.values()) {
            if (status.getId() == id) {
            	found = status;
            	break;
            }
        }
        return found;
    }
    
    @AllArgsConstructor @Data
    public static class ExtendedStatus implements Serializable, Cloneable {
		private static final long serialVersionUID = 8677867209421228557L;
		private final IStatus status;
		private final int sequence;
		private final int maxStep;
		
		public String getDescription() {
			return status.getDescription();
		}

		public boolean isCompleted() {
			return sequence >= maxStep;
		}
		
		public boolean isLastPreviousStep() {
			return sequence + 1 == maxStep;
		}

		//TODO : too specific to approbation cycle. To replace by notion of invalid state. This will be evaluated within domain object
		public ExtendedStatus newState(IStatus newValue, int newStep) {
			ExtendedStatus newStatus = new ExtendedStatus(newValue, newStep, maxStep);
			if(newStatus.isCompleted() && !(newValue.isRejected() || newValue.isApproved())) { 
				throw new StateMachineException(MessageFormat.format("Invalid state : When completed, final status must be either rejected or approved. The transition is not allowed from status {0} to {1} at sequence {2} of {3} ", getDescription(),
						newValue.getDescription(), newStep, maxStep));
			} else if(!newStatus.isCompleted() && newValue.isApproved()) {
				throw new StateMachineException(MessageFormat.format("Invalid state : When not completed, final status can't be approved. The transition is not allowed from status {0} to {1} at sequence {2} of {3} ", getDescription(),
						newValue.getDescription(), newStep, maxStep));
			}
			return newStatus;
		}

		public boolean isNotRejected() {
			return this.getStatus().isNotRejected();
		}
		
    }
}
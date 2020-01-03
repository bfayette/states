package org.state.machine;

import java.io.Serializable;
import java.util.function.Predicate;

import org.state.machine.Conditions.ICondition;
import org.state.machine.approbation.IStatus;
import org.state.machine.approbation.Status.ExtendedStatus;

import lombok.Data;

public interface ITransition extends Cloneable, Serializable, Predicate<ExtendedStatus> {

	@Data
	public class RealizedTransition implements ITransition {
		private static final long serialVersionUID = 5433334206604847053L;
		private ExtendedStatus wrappedStatus;
		private ICondition condition;
		private boolean current = true;

		public RealizedTransition(ICondition condition, ExtendedStatus wrappedStatus) {
			this.condition = condition;
			this.wrappedStatus = wrappedStatus;
		}

		public RealizedTransition copy() {
			try {
				RealizedTransition trans = (RealizedTransition) super.clone();
				return trans;
			} catch (CloneNotSupportedException cause) {
				throw new StateMachineException(cause);
			}
		}
		
		public ExtendedStatus getWrappedStatus() {
			return wrappedStatus;
		}

		public IStatus getUnwrappedStatus() {
			return wrappedStatus.getStatus();
		}

		public void setWrappedStatus(ExtendedStatus wrappedStatus) {
			this.wrappedStatus = wrappedStatus;
		}

		public boolean isLast() {
			return this.wrappedStatus != null && this.wrappedStatus.isCompleted();
		}

		public boolean isLastPreviousStep() {
			return this.wrappedStatus != null && this.wrappedStatus.isLastPreviousStep();
		}
				
		@Override
		public boolean test(ExtendedStatus state) {
			return this.wrappedStatus != null && this.wrappedStatus.equals(state);
		}
	}

}
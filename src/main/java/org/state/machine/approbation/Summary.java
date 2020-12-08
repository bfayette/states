package org.state.machine.approbation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.state.machine.Conditions.ICondition;
import org.state.machine.IEntity;
import org.state.machine.ITransition.RealizedTransition;
import org.state.machine.approbation.Status.ExtendedStatus;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @EqualsAndHashCode(of = "name")
/**
 * The states history are persited in the domain. This is not required may have some pratical use cases. 
 * Only last transition is mandatory to save.
 */
public class Summary implements IEntity {

	private static final long serialVersionUID = -7556690204979280181L;
	@Getter @Setter
	private String name;
	
	private LinkedList<RealizedTransition> transitions = new LinkedList<>();
	private List<ApprCondition> analystsThenManagersConditions;

	public Summary copy() {
		Summary summary = new Summary();
		summary.setAnalystsThenManagersConditions(analystsThenManagersConditions);
		summary.setTransitions(transitions);
		return summary;
	}

	public boolean isActive() {
		return getUnwrappedStatus().isActive();
	}

	public List<RealizedTransition> getTransitions() {
		return Collections.unmodifiableList(transitions);
	}

	public void addTransition(RealizedTransition transition) {
		for (RealizedTransition existing : transitions) {
			existing.setCurrent(false);
		}
		this.transitions.add(transition);
	}

	public ExtendedStatus getWrappedStatus() {
		RealizedTransition transition = getLastTransition();
		ExtendedStatus status;
		if (transition != null) {
			status = transition.getWrappedStatus();
		} else {
			 status = new ExtendedStatus(Status.IN_PROGRESS, 0, getMaxStep());
		}
		return status;
	}

	public IStatus getUnwrappedStatus() {
		return getWrappedStatus().getStatus();
	}

	public long getStatusStep() {
		return getWrappedStatus().getSequence();
	}

	public String getStatusDescription() {
		return getWrappedStatus().getDescription();
	}

	public int getMaxStep() {
		int nb = this.getAnalystsThenManagersConditions().size();
		return nb;
	}

	public RealizedTransition getLastTransition() {
		RealizedTransition transition = transitions.peekLast();
		return transition;
	}

	public boolean isHaveApprobationCycle() {
		return this.getMaxStep() > 1;
	}

	public ICondition getCondition() {
		RealizedTransition trs = getLastTransition();
		return trs == null ? new ApprCondition() : trs.getCondition();
	}

	public List<ApprCondition> getAnalystsThenManagersConditions() {
		return new ArrayList<ApprCondition>(analystsThenManagersConditions);
	}

	public void setAnalystsThenManagersConditions(List<ApprCondition> analystsThenManagersConditions) {
		this.analystsThenManagersConditions = new ArrayList<>(analystsThenManagersConditions);
	}
	
	public void setTransitions(List<RealizedTransition> transitions) {
		this.transitions = new LinkedList<>(transitions);
	}

}

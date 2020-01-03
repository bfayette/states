package org.state.machine;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.state.machine.Conditions.ICondition;
import org.state.machine.ITransition.RealizedTransition;
import org.state.machine.approbation.Summary;
import org.state.machine.approbation.EVENT_NAME;
import org.state.machine.approbation.Event;
import org.state.machine.approbation.IStatus;
import org.state.machine.approbation.Status;
import org.state.machine.approbation.Status.ExtendedStatus;
import org.state.machine.approbation.User;

public class StateMachine implements Serializable {

	private static final long serialVersionUID = 5118129339679938607L;
	//TODO use an object instead of a Map to hold static transitions and conditions. 
	private final Map<MultiKey<Object>, IStatus> transitions; 
	private final Map<MultiKey<Object>, ICondition> conditions;
	private final boolean evaluateCondition;

	public StateMachine(boolean evaluateCondition, Map<MultiKey<Object>, IStatus> transitions,
			Map<MultiKey<Object>, ICondition> conditions) {
		this.evaluateCondition = evaluateCondition;
		this.transitions = transitions;
		this.conditions = conditions;
	}

	public RealizedTransition nextState(Event<Summary> event) {
		ExtendedStatus nextState = findNextStatus(event);
		ICondition condition = validateStatusChangeCondition(event, nextState);
		return new RealizedTransition(condition, nextState);
	}

	private ICondition validateStatusChangeCondition(Event<Summary> event, ExtendedStatus newStatus) {
		MultiKey<Object> key1 = getConditionKey(newStatus);
		ICondition condition = this.conditions.get(key1);
		Summary summary = event.getEntity();
		ExtendedStatus wrapper = summary.getWrappedStatus();
		
		if (evaluateCondition && !condition.test(event)) {
			User user = (User) event.getArgument("user");
			throw new StateMachineException(MessageFormat.format(
					"Unauthorized transition. User {0} is not allowed to make the transition from status {1} and event {2} ", user.getUserCode(),
					wrapper.getDescription(), event.getEventId()));
		}
		
		return condition;
	}

	private MultiKey<Object> getConditionKey(ExtendedStatus newStatus) {
		MultiKey<Object> key1 = new MultiKey<Object>(newStatus.getStatus(), newStatus.getSequence());
		return key1;
	}

	private ExtendedStatus findNextStatus(Event<Summary> event) {
		Summary summary = event.getEntity();
		String eventName = StringUtils.upperCase(event.getEventId());
		User user = (User) event.getArgument("user");
		ExtendedStatus wrapper = summary.getWrappedStatus();
		MultiKey<Object> key = new MultiKey<Object>(wrapper.getStatus(), eventName);
		IStatus found = this.transitions.get(key);
		
		if (found == null) {
			throw new StateMachineException(MessageFormat.format(
					"User {0} is not allowed to make the transition from status {1} and event {2} ", user.getUserCode(),
					wrapper.getDescription(), event.getEventId()));
		}
		
		EVENT_NAME evtName = EVENT_NAME.findByName(eventName);
		int newStep = evtName.incrementStep(wrapper);
		ExtendedStatus newWrapperStatus = wrapper.newState(found, newStep);
		return newWrapperStatus;
	}
	
	public final static class Builder {
		private final boolean evaluateCondition;
		private Map<MultiKey<Object>, IStatus> transitions = new LinkedHashMap<>();
		private Map<MultiKey<Object>, ICondition> conditions = new LinkedHashMap<>();
		
		public Builder(boolean evaluateCondition) {
			this.evaluateCondition = evaluateCondition;
		}
		
		public Builder transition(IStatus state, EVENT_NAME event, Status nextState) {
			this.transitions.put(new MultiKey<Object>(state, StringUtils.upperCase(event.name())), nextState);
			return this;
		}
		
		public Builder withTransition(IStatus state, EVENT_NAME event, Status nextState) {
			this.transitions.put(new MultiKey<Object>(state, StringUtils.upperCase(event.name())), nextState);
			return this;
		}
		public Builder withTransitions(Map<MultiKey<Object>, IStatus> transitions) {
			this.transitions = transitions;
			return this;
		}
		
		public Builder withCondition(IStatus status, int seq, ICondition condition) {
			this.conditions.put(new MultiKey<Object>(status, seq),	condition);
			return this;
		}
		
		public Builder withConditions(Map<MultiKey<Object>, ICondition> conditions) {
			this.conditions = conditions;
			return this;
		}
		
		public StateMachine build() {
			Validate.isTrue(transitions.size() > 0, "Please define one or more transitions");
			Validate.isTrue(conditions.size() > 0, "Please define one or more conditions");
			StateMachine stateMachine = new StateMachine(evaluateCondition, transitions, conditions);
			return stateMachine;
		}
	}

	public StateMachine enableEvaluateConditions(boolean evaluateConditions) {
		return new StateMachine(evaluateConditions, transitions, conditions);
	}

	public static Builder builder(boolean evaluateCondition) {
		return new Builder(evaluateCondition);
	}	
}

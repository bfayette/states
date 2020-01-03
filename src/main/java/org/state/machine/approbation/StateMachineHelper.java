package org.state.machine.approbation;

import static org.state.machine.approbation.Status.APPROVED;
import static org.state.machine.approbation.Status.IN_PROGRESS;
import static org.state.machine.approbation.Status.RECOMMENDED;
import static org.state.machine.approbation.Status.REJECTED;
import static org.state.machine.approbation.Status.SUBMITTED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.state.machine.Conditions.ICondition;
import org.state.machine.IEvent;
import org.state.machine.ITransition.RealizedTransition;
import org.state.machine.StateMachine;
import org.state.machine.StateMachine.Builder;
import org.state.machine.StateMachineException;

public final class StateMachineHelper {

	public final static List<IEvent> nextEvents(StateMachine stateMachine, Summary summary, User user,
			IEvent... events) {
		StateMachine newStateMachine = stateMachine.enableEvaluateConditions(true); // get a copy
		List<IEvent> posEvts = new ArrayList<IEvent>();
		List<? extends IEvent> tryEvts = events.length == 0 ? Arrays.asList(EVENT_NAME.values())
				: Arrays.asList(events);
		for (IEvent evtId : tryEvts) {
			try {
				Summary copy = summary.copy();
				nextTransition(newStateMachine, copy, evtId, user, user);
				posEvts.add(evtId);
			} catch (StateMachineException ignore) {
				
			}
		}
		return posEvts;
	}
	
	private final static RealizedTransition nextTransition(StateMachine stateMachine, final Event<Summary> event) {
		final RealizedTransition transition = stateMachine.nextState(event);
		Summary summary = event.getEntity();
		summary.addTransition(transition);
		return transition;
	}

	private final static RealizedTransition nextTransition(StateMachine stateMachine, Summary summary,	IEvent eventEnum, User user, User approbator) {
		final Event<Summary> event = createEvent(summary, eventEnum, user, approbator);
		return nextTransition(stateMachine, event);
	}

	public static Event<Summary> createEvent(Summary summary, IEvent eventEnum, User user,
			User approbator) {
		Event<Summary> event = new Event<Summary>(eventEnum.getEventName(), summary);
		event.setArgument("user", user);
		event.setArgument("approbator", approbator);
		event.setEventId(eventEnum.getEventName());
		event.setEntity(summary);
		return event;
	}

	public static StateMachine create(List<ApprCondition> analystsThenManagersConditions) {
		Builder builder = StateMachineHelper.oneSubmissionOneApprovalZeroOrManyRecommendationsConditions(true,
				analystsThenManagersConditions);
		StateMachine stateMachine = StateMachineHelper
				.oneSubmissionOneApprovalZeroOrManyRecommendationsTransitions(builder).build();
		return stateMachine;
	}

	public static StateMachine create(List<ApprCondition> analystsThenManagersConditions, ICondition cond0) {
		Builder builder = StateMachineHelper.oneSubmissionOneApprovalZeroOrManyRecommendationsConditions(true,
				analystsThenManagersConditions, cond0);
		StateMachine stateMachine = StateMachineHelper.oneSubmissionOneApprovalZeroOrManyRecommendationsTransitions(builder).build();
		return stateMachine;
	}

	private static ApprCondition createAnalystConditions() {
		ApprCondition cond = new ApprCondition();
		cond.setRole(Role.ANALYST);
		cond.setSequence(1);
		return cond;
	}

	public static List<ApprCondition> create1AnalystThenManagerConditions(long targetId,
			List<ApprCondition> managersConditions) {
		List<ApprCondition> analystsThenManagersConditions = new ArrayList<>();
		int sequence = 1;
		for (ApprCondition iApprobationCondition : managersConditions) {
			iApprobationCondition.setSequence(++sequence);
			analystsThenManagersConditions.add(iApprobationCondition);
		}
		analystsThenManagersConditions.add(0, createAnalystConditions());
		return analystsThenManagersConditions;
	}

	private static StateMachine.Builder oneSubmissionOneApprovalZeroOrManyRecommendationsConditions(
			boolean evaluateCondition, List<? extends ICondition> conditions) {
		return oneSubmissionOneApprovalZeroOrManyRecommendationsConditions(evaluateCondition, conditions, null);
	}

	private static StateMachine.Builder oneSubmissionOneApprovalZeroOrManyRecommendationsConditions(
			boolean evaluateCondition, List<? extends ICondition> conditions, ICondition condition) {
		StateMachine.Builder builder = StateMachine.builder(evaluateCondition);
		builder.withCondition(Status.SUBMITTED, 1, conditions.get(0));
		// rework from reject to inprogress
		builder.withCondition(Status.IN_PROGRESS, 0, condition);

		for (int seq = 1; seq < conditions.size() - 1; seq++) {
			builder.withCondition(Status.REJECTED, seq + 1, conditions.get(seq));
			builder.withCondition(Status.RECOMMENDED, seq + 1, conditions.get(seq));
		}

		if (conditions.size() >= 2) { // 3 or more step approval
			int lastEle = conditions.size() - 1;
			builder.withCondition(Status.APPROVED, conditions.size(), conditions.get(lastEle));
			builder.withCondition(Status.REJECTED, conditions.size(), conditions.get(lastEle));
		}
		return builder;
	}

	public static Summary getAndInitializeNextState(StateMachine stateMachine, Event<Summary> event) {
		Summary summary = event.getEntity().copy();
		RealizedTransition trans = nextTransition(stateMachine, event);
		summary.addTransition(trans);
		return summary;
	}

	public static Builder oneSubmissionOneApprovalZeroOrManyRecommendationsTransitions(Builder builder) {
		builder.transition(IN_PROGRESS, EVENT_NAME.SEND, SUBMITTED);
		builder.transition(SUBMITTED, EVENT_NAME.RECOMMEND, RECOMMENDED);
		builder.transition(SUBMITTED, EVENT_NAME.APPROVE, APPROVED);
		builder.transition(SUBMITTED, EVENT_NAME.REJECT, REJECTED);
		builder.transition(REJECTED, EVENT_NAME.REWORK, IN_PROGRESS);
		builder.transition(REJECTED, EVENT_NAME.APPROVE, APPROVED);
		builder.transition(REJECTED, EVENT_NAME.RECOMMEND, RECOMMENDED);
		builder.transition(RECOMMENDED, EVENT_NAME.REJECT, REJECTED);
		builder.transition(RECOMMENDED, EVENT_NAME.SEND, RECOMMENDED);
		builder.transition(RECOMMENDED, EVENT_NAME.APPROVE, APPROVED);
		builder.transition(APPROVED, EVENT_NAME.REJECT, REJECTED);
		return builder;
	}

}

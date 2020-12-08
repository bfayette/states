package org.state.machine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.state.machine.ITransition.RealizedTransition;
import org.state.machine.approbation.ApprCondition;
import org.state.machine.approbation.Summary;
import org.state.machine.approbation.EVENT_NAME;
import org.state.machine.approbation.Event;
import org.state.machine.approbation.StateMachineHelper;
import org.state.machine.approbation.Status;
import org.state.machine.approbation.Status.ExtendedStatus;
import org.state.machine.approbation.User;


public class StateMachineHelperTest extends Assert {

//	@Test
//	public void testPossibleTransitionsListOfIApprobationConditionCompositeBoardsheetsUser() {
//		CompositeBoardsheets summary = create(cond(1), cond(2), cond(3));
//		assertTrue(summary.getTransitions().isEmpty());
//		List<Transition> possibleTransitions = ApprStateMachineHelper.possibleTransitions(summary.getAnalystsThenManagersConditions(), summary, new User());
//		assertEquals(possibleTransitions.size(), 3);
//		assertTrue(summary.getTransitions().isEmpty());
//	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testNextEvents_approve_reject_startwithstatus_recommened() {
		Summary summary = create(cond(1), cond(2), cond(3));
		summary.addTransition(new RealizedTransition(cond(2), new ExtendedStatus(Status.RECOMMENDED, 2, 3)));
		StateMachine stateMachine = StateMachineHelper.create(summary.getAnalystsThenManagersConditions());
		List<IEvent> evemts = StateMachineHelper.nextEvents(stateMachine , summary, new User());
		assertEquals(new HashSet(Arrays.asList(EVENT_NAME.APPROVE, EVENT_NAME.REJECT)), new HashSet(evemts));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testNextEvents_rec_reject_startwithstatus_submitted() {
		Summary summary = create(cond(1), cond(2), cond(3));
		summary.addTransition(new RealizedTransition(cond(2), new ExtendedStatus(Status.SUBMITTED, 1, 3)));
		StateMachine stateMachine = StateMachineHelper.create(summary.getAnalystsThenManagersConditions());
		List<IEvent> evemts = StateMachineHelper.nextEvents(stateMachine , summary, new User());
		assertEquals(new HashSet(Arrays.asList(EVENT_NAME.RECOMMEND, EVENT_NAME.REJECT)), new HashSet(evemts));
	}
	
	@Test
	public void testNextEvents_send() {
		Summary summary = create(cond(1), cond(2), cond(3));
		assertTrue(summary.getTransitions().isEmpty());
		StateMachine stateMachine = StateMachineHelper.create(summary.getAnalystsThenManagersConditions());
		List<IEvent> evemts = StateMachineHelper.nextEvents(stateMachine , summary, new User());
		assertEquals(EVENT_NAME.SEND, evemts.get(0));
		assertTrue(summary.getTransitions().isEmpty());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testNextEvents_rework_approve_events() {
		Summary summary = create(cond(1), cond(2), cond(3));
		summary.addTransition(new RealizedTransition(cond(2), new ExtendedStatus(Status.REJECTED, 3, 3)));
		StateMachine stateMachine = StateMachineHelper.create(summary.getAnalystsThenManagersConditions(), cond(0));
		List<IEvent> evemts = StateMachineHelper.nextEvents(stateMachine , summary, new User());
		assertEquals(new HashSet(Arrays.asList(EVENT_NAME.REWORK, EVENT_NAME.APPROVE)), new HashSet(evemts));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testNextEvents_rework_Recommend_events() {
		Summary summary = create(cond(1), cond(2), cond(3));
		summary.addTransition(new RealizedTransition(cond(2), new ExtendedStatus(Status.REJECTED, 2, 3)));
		StateMachine stateMachine = StateMachineHelper.create(summary.getAnalystsThenManagersConditions(), cond(0));
		List<IEvent> evemts = StateMachineHelper.nextEvents(stateMachine , summary, new User());
		assertEquals(new HashSet(Arrays.asList(EVENT_NAME.RECOMMEND, EVENT_NAME.REWORK)), new HashSet(evemts));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testNextEvents_1_Analyst_1_manager() {
		Summary summary = create(cond(1), cond(2));
		summary.addTransition(new RealizedTransition(cond(2), new ExtendedStatus(Status.SUBMITTED, 1, 2)));
		StateMachine stateMachine = StateMachineHelper.create(summary.getAnalystsThenManagersConditions());
		List<IEvent> evemts = StateMachineHelper.nextEvents(stateMachine , summary, new User());
		evemts = StateMachineHelper.nextEvents(stateMachine , summary, new User());
		assertEquals(new HashSet(Arrays.asList(EVENT_NAME.APPROVE, EVENT_NAME.REJECT)), new HashSet(evemts));
	}
	
	private Summary create(ApprCondition... conds) {
		Summary summary = new Summary();		
		summary.setAnalystsThenManagersConditions(Arrays.asList(conds));
		return summary;
	}

	private ApprCondition cond(final int seq) {
		ApprCondition condition = new ApprCondition() {
			private static final long serialVersionUID = 2259022565991190033L;
			
			@Override
			public int getSequence() {
				return seq;
			}
			@Override
			public boolean test(Event<?> evtObj) {
				return true;
			}
		};
		return condition;
	}

}

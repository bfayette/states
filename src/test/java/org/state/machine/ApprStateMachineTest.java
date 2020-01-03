package org.state.machine;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.state.machine.Conditions.ICondition;
import org.state.machine.approbation.ApprCondition;
import org.state.machine.approbation.Summary;
import org.state.machine.approbation.EVENT_NAME;
import org.state.machine.approbation.Event;
import org.state.machine.approbation.StateMachineHelper;
import org.state.machine.approbation.Status;
import org.state.machine.approbation.User;

@RunWith(MockitoJUnitRunner.class)
public class ApprStateMachineTest extends Assert {
	
	@Mock(answer=Answers.RETURNS_MOCKS)
	private User analyst, manager;
	
	@Mock(answer=Answers.RETURNS_MOCKS)
	private ICondition analystAppr, managerAppr;
		
	
	@Test
	public void testBaseScenario_Or_Happy_Case() {
		Summary summary = create(cond(1), cond(2), cond(3));
		StateMachine machine = StateMachineHelper.create(summary.getAnalystsThenManagersConditions());
		
		assertEquals(Status.IN_PROGRESS, summary.getUnwrappedStatus());
		Summary submitted = StateMachineHelper.getAndInitializeNextState(machine, createEvent(EVENT_NAME.SEND, analyst, summary));		
		assertEquals(Status.SUBMITTED, submitted.getUnwrappedStatus());
		asserBssNotSame(summary, submitted);
		
		Summary recommended = StateMachineHelper.getAndInitializeNextState(machine, createEvent(EVENT_NAME.RECOMMEND, analyst, submitted));
		assertEquals(Status.RECOMMENDED, recommended.getUnwrappedStatus());
		asserBssNotSame(summary, recommended);
		
		Summary approved = StateMachineHelper.getAndInitializeNextState(machine, createEvent(EVENT_NAME.APPROVE, manager, recommended));
		assertEquals(Status.APPROVED, approved.getUnwrappedStatus());
		asserBssNotSame(summary, approved);
		
		try {
			summary = StateMachineHelper.getAndInitializeNextState(machine, createEvent(EVENT_NAME.APPROVE, analyst, approved));
			fail("no more events are accepted");
		} catch (IllegalStateException expected) {}
		
		assertTrue(approved.getWrappedStatus().isCompleted());
	}
	
	@Test
	public void testScenarioRejected() {
		Summary summary = create(cond(1), cond(2), cond(3));
		StateMachine machine = StateMachineHelper.create(summary.getAnalystsThenManagersConditions(), cond(0));
		
		Summary submitted = StateMachineHelper.getAndInitializeNextState(machine, createEvent(EVENT_NAME.SEND, analyst, summary));			
		Summary recommended = StateMachineHelper.getAndInitializeNextState(machine, createEvent(EVENT_NAME.RECOMMEND, analyst,submitted));	
		Summary rejected = StateMachineHelper.getAndInitializeNextState(machine, createEvent(EVENT_NAME.REJECT, manager,recommended));	
		Summary inProgress = StateMachineHelper.getAndInitializeNextState(machine, createEvent(EVENT_NAME.REWORK, analyst, rejected));	
		assertFalse(summary.getWrappedStatus().isCompleted());
		assertEquals(Status.IN_PROGRESS, inProgress.getUnwrappedStatus());
	}
	
	@Test(expected=IllegalStateException.class)
	public void test_two_many_recommend_events() {
		Summary summary = create(cond(1), cond(2), cond(3));
		StateMachine machine = StateMachineHelper.create(summary.getAnalystsThenManagersConditions());
		
		Summary submitted = StateMachineHelper.getAndInitializeNextState(machine, createEvent(EVENT_NAME.SEND, analyst,summary));			
		Summary recommended = StateMachineHelper.getAndInitializeNextState(machine, createEvent(EVENT_NAME.RECOMMEND, analyst,submitted));	
		StateMachineHelper.getAndInitializeNextState(machine, createEvent(EVENT_NAME.RECOMMEND, manager, recommended));		
	}
	
	@Test(expected=IllegalStateException.class)
	public void test_call_approve_sooner() {
		Summary summary = create(cond(1), cond(2), cond(3));
		StateMachine machine = StateMachineHelper.create(summary.getAnalystsThenManagersConditions());
		Summary submitted = StateMachineHelper.getAndInitializeNextState(machine, createEvent(EVENT_NAME.SEND, analyst, summary));			
		StateMachineHelper.getAndInitializeNextState(machine, createEvent(EVENT_NAME.APPROVE, manager, submitted));
	}

	private void asserBssNotSame(Summary summary0, Summary summary1) {
		assertNotSame(summary0, summary1);
		assertEquals(summary0.getName(), summary1.getName());
		assertEquals(summary0.getMaxStep(), summary1.getMaxStep());
	}

	private Event<Summary> createEvent(EVENT_NAME evtName, User user, Summary bean) {
		Event<Summary> temp = new Event<Summary>(evtName.name().toUpperCase(), bean);
		temp.setArgument("user", user);
		return temp;
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

package org.state.machine;

import java.util.function.Predicate;

import org.state.machine.approbation.Event;

public final class Conditions {

	public interface ICondition extends Predicate<Event<?>> {
	}

	enum ObjectCondition implements ICondition {
		ALWAYS_TRUE {
			public boolean test(Event<?> o) {
				return true;
			}
		},
		ALWAYS_FALSE {
			public boolean test(Event<?> o) {
				return true;
			}
		}
	}
}
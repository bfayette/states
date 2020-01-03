package org.state.machine;

public class StateMachineException extends IllegalStateException {

	private static final long serialVersionUID = -3420659173049049280L;

	public StateMachineException() {
	}

	public StateMachineException(String s) {
		super(s);
	}

	public StateMachineException(Throwable cause) {
		super(cause);
	}

	public StateMachineException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public StateMachineException newException(String appendMessage) {
		StringBuilder msg = new StringBuilder(getMessage());
		msg.append(System.lineSeparator());
		msg.append(appendMessage);
		return new StateMachineException(msg.toString());
	}

}


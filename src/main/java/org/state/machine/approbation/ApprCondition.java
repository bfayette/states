package org.state.machine.approbation;

import org.apache.commons.lang3.StringUtils;
import org.state.machine.Conditions.ICondition;
import org.state.machine.StateMachineException;

import lombok.Data;

@Data
public class ApprCondition implements ICondition {
    
	private static final long serialVersionUID = 1168326659308233357L;
    private Role role;
    private int sequence;   
    private boolean current; 
    
	public ApprCondition copyWithNewSequence(int newSequence) {
		ApprCondition cond;
		try {
			cond = (ApprCondition) clone();
			cond.setSequence(newSequence);
			return cond;
		} catch (CloneNotSupportedException cause) {
			throw new StateMachineException(cause);
		}
	}
	
	public boolean  isAnalyst() {
		return getRole().isAnalyst();
	}
	
	public String getRoleCode() {
		return StringUtils.defaultIfEmpty(getRole().getCode(), "na") ;
	}
	
	@Override
	public boolean test(Event<?> event) {
		User approbator = (User) event.getArgument("user");
		return approbator.isInRoleOnBehalf(getRole()) || approbator.isInRole(getRole());
	}
}

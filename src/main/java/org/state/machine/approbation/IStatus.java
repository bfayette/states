package org.state.machine.approbation;

public interface IStatus {
	
	public String getDescription();

	public int getId();
	
	public boolean isNotApproved() ;
    
    public boolean isApproved();
    
    public boolean isActive();
    
    public boolean isRejected();
    
    public boolean isNotRejected();

	public boolean isInProgress();

	public boolean isApprobationProcessInProgress();
}

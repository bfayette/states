package org.state.machine.approbation;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class Role implements Serializable {
	
	private static final long serialVersionUID = -4260019391685213739L;
    public static final Role ANALYST = new Role("ANALYST");

    private String code;
	
	public boolean isAnalyst() {
		return ANALYST.equals(this);
	}

}

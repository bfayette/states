package org.state.machine.approbation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class User implements Serializable {
	private static final long serialVersionUID = -8197067240294852312L;
	private String userCode;
    private String email;

    private List<Role> roles = new ArrayList<>();
    private List<Role> onBehalfRoles = new ArrayList<>();
    
    

    public boolean isInRole(Role role) {
        return (this.roles != null & this.roles.contains(role));
    }

    public boolean isInRoleOnBehalf(Role role) {
        return (this.onBehalfRoles != null & this.onBehalfRoles.contains(role));
    }


}

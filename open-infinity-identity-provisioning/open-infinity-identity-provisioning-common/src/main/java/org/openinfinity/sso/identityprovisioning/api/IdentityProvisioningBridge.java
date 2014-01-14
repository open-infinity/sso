package org.openinfinity.sso.identityprovisioning.api;

import org.openinfinity.core.security.principal.Identity;

public interface IdentityProvisioningBridge {

	void provision(Identity user);

}
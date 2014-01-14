package org.openinfinity.sso.identityprovisioning.api;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.openinfinity.core.security.principal.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Aspect
public class IdentityProvisioningAspect {

	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IdentityProvisioningAspect.class);
	
	@Autowired
	IdentityProvisioningBridge identityProvisioningBridge;

	@Pointcut("@annotation(org.openinfinity.sso.identityprovisioning.api.IdentityProvisioning)")
	public void anyIdentityProvisioningMethod() {}
	
	@Before(value="anyIdentityProvisioningMethod() && @annotation(identityProvisioning)")
	public void provision(IdentityProvisioning userProvisioning) {
		long startTime = System.currentTimeMillis();
		System.out.println("################################### stdaasdfasdfadsfarting");
		LOGGER.debug("Starting provisioning of the Identity.");
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		if (authentication instanceof Identity) {
			Identity identity = (Identity) authentication;
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Injecting Identity to provisioning bridge.");
			identityProvisioningBridge.provision(identity);
			 
		}
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Identity provisioning finalized in " + (System.currentTimeMillis()-startTime) + " ms."); 
	}	

}
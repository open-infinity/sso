/*
 * Copyright (c) 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * Identity provisioning aspect for injecting identity to the underlying system by using IdentityProvisioningBridge API.
 * 
 * @author Ilkka Leinonen
 * @version 1.0.0
 * @since 1.0.0
 */
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
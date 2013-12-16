/*
 * Copyright (c) 2013 the original author or authors.
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
package org.openinfinity.sso.security.spring;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.openinfinity.sso.security.context.grid.IdentityContext;
import org.openinfinity.sso.security.util.GlobalVariables;
import org.openinfinity.sso.security.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Filter for binding <code>org.openinfinity.core.security.principal.Identity</code> to <code>org.springframework.security.core.context.SecurityContext</code>.
 * <code>org.openinfinity.core.security.principal.Identity</code> object will be loaded from the in-memory datagrid by session id provided by identity provider. 
 *  
 * @author Ilkka Leinonen
 * @version 1.0.0
 * @since 1.0.0
 */
public class InjectableSecurityContextFilterBean extends GenericFilterBean {

	/**
	 * Logger for the class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(InjectableSecurityContextFilterBean.class);
	
	/**
	 * Session identifier defines the cookie name.
	 */
	private static String SESSION_IDENTIFIER =  PropertiesUtil.loadValue(GlobalVariables.ATTRIBUTE_BASED_SESSION_KEY);
	
	/**
	 * Filters every request and response objects and verifies that <code>org.springframework.security.core.context.SecurityContext</code> is available. 
	 * If not <code>org.openinfinity.core.security.principal.Identity</code> which implements the <code>org.springframework.security.core.Authentication</code> interface will be used for storing information to local <code>org.springframework.security.core.context.SecurityContext</code>.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		LOGGER.debug("Initializing InjectableSecurityContextFilterBean for Spring security.");
		if (SecurityContextHolder.getContext() != null 
				&& SecurityContextHolder.getContext().getAuthentication() == null) {
			String sessionId = (String) request.getAttribute(SESSION_IDENTIFIER);
			if (sessionId != null) {
				LOGGER.debug("Session identifier [" + sessionId + "] found from the request.");
				eraseSecurityContext(sessionId);
				injectIdentityBasedSecurityContext(sessionId);
			}
		}
		LOGGER.debug("Finalized injecting authentication in InjectableSecurityContextFilterBean.");
		chain.doFilter(request, response);
	}

	private void eraseSecurityContext(String sessionId) {
		SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(null);
		SecurityContextHolder.clearContext();
		LOGGER.debug("SecurityContextHolder erased.");
	}
	
	private void injectIdentityBasedSecurityContext(String sessionId) {
		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		SecurityContextHolder.setContext(securityContext);
		Authentication authentication = IdentityContext.loadIdentity(sessionId);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		LOGGER.info("SecurityContext created for active session based on IdentityContext for user: " + authentication.getName());
	}
	
}
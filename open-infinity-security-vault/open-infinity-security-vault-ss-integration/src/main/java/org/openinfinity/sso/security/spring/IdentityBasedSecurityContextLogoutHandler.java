/*
 * Copyright (c) 2013-2015 the original author or authors.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openinfinity.sso.security.context.grid.IdentityContext;
import org.openinfinity.sso.security.util.GlobalVariables;
import org.openinfinity.sso.security.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

/**
 * Removes Identity from the identity context (in-memory datagrid).
 * 
 * @author Ilkka Leinonen
 * @version 1.0.0
 * @since 1.0.0
 */
public class IdentityBasedSecurityContextLogoutHandler extends SecurityContextLogoutHandler {

	/**
	 * Logger for the class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IdentityBasedSecurityContextLogoutHandler.class);
	
	/**
	 * Session identifier defines the cookie name.
	 */
	private static String SESSION_IDENTIFIER =  PropertiesUtil.loadValue(GlobalVariables.ATTRIBUTE_BASED_SESSION_KEY);	
	
	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		LOGGER.debug("Initializing IdentityBasedSecurityContextLogoutHandler for Spring security.");
		if (SecurityContextHolder.getContext() != null 
				&& SecurityContextHolder.getContext().getAuthentication() != null) {
			String sessionId = (String) request.getAttribute(SESSION_IDENTIFIER);
			if (sessionId != null) {
				LOGGER.debug("Erasing session with identifier [" + sessionId + "] from the identity context.");
				IdentityContext.clear(sessionId);
				request.getSession().invalidate();
				super.logout(request, response, authentication);
			}
		}
		LOGGER.debug("Finalized injecting authentication in IdentityBasedSecurityContextLogoutHandler.");
	}	

}
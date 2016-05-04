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
package org.openinfinity.sso.valve;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.openinfinity.core.security.principal.Identity;
import org.openinfinity.sso.security.context.grid.IdentityContext;
import org.openinfinity.sso.security.util.GlobalVariables;
import org.openinfinity.sso.security.util.PropertiesUtil;
import org.openinfinity.sso.valve.mapper.RequestToAttributeMapper;
import org.openinfinity.sso.valve.mapper.RequestToIdentityMapper;

/**
 * Security vault intercepts every request and looks for a session id. 
 * If session identifier is found federated <code>org.openinfinity.core.security.principal.Identity</code> object will be created based on the username, tenant id, and roles provided by identity provider.
 * <code>org.openinfinity.core.security.principal.Identity</code> object will be stored for every in-memory datagrid member node.
 *  
 *  
 * @author Ilkka Leinonen
 * @version 1.0.0
 * @since 1.0.0
 */
public class AttributeBasedSecurityVaultValve extends ValveBase {

	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(AttributeBasedSecurityVaultValve.class.getName());

	/**
	 * Represents the unique identifier of the session cookie name.
	 */
	private static String SESSION_IDENTIFIER = null;
	
	static {
		if (SESSION_IDENTIFIER == null) {
			PropertiesUtil.init();
			SESSION_IDENTIFIER = PropertiesUtil.loadValue(GlobalVariables.ATTRIBUTE_BASED_SESSION_KEY);
		}
	}
	
	/**
	 * Intercepts every request and looks for a session id. 
	 * If session identifier is found federated <code>org.openinfinity.core.security.principal.Identity</code> object will be created based on the username, tenant id, and roles provided by identity provider.
	 * <code>org.openinfinity.core.security.principal.Identity</code> object will be stored for every in-memory datagrid member node.
	 */
	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		String sessionId = (String) request.getAttribute(SESSION_IDENTIFIER);
		LOGGER.info("Request intercepted by security valve. The session id is [" + sessionId + "]");
		if (sessionId != null && IdentityContext.loadIdentity(sessionId) == null) {
			LOGGER.info("Identity provider session id found from the request as [" + sessionId + "]");
			RequestToIdentityMapper requestToAttributeMapper = new RequestToAttributeMapper();
			Identity identity = requestToAttributeMapper.map(request);
			String principalName = (
					identity != null && 
					identity.getUserPrincipal() != null && 
					identity.getUserPrincipal().getName() != null) ? 
							identity.getUserPrincipal().getName() : "user name missing.";
			LOGGER.info("Identity session found from the request for [" + principalName + "]");
			IdentityContext.storeIdentity(sessionId, identity);
			if (identity.getUserPrincipal() != null) {
				request.setUserPrincipal(identity.getUserPrincipal());
			}
			LOGGER.info("Security context updated for [" + principalName + "]");
		}
		getNext().invoke(request, response);
	}

}
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

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.openinfinity.core.security.principal.Identity;
import org.openinfinity.sso.security.util.GlobalVariables;
import org.openinfinity.sso.security.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

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
	private static String ATTRIBUTE_BASED_SESSION_IDENTIFIER =  PropertiesUtil.loadValue(GlobalVariables.ATTRIBUTE_BASED_SESSION_KEY);

	/**
	 * Session identifier defines the cookie name.
	 */
	private static String HEADER_BASED_SESSION_IDENTIFIER = PropertiesUtil.loadValue(GlobalVariables.HEADER_BASED_SESSION_KEY);


	private static final String IDENTITY_IDENTIFIER = "IDENTITY_CONTEXT_MAP";
	private static ConcurrentMap<String, Identity> IN_MEMORY_DATAGRID = null;

	/**
	 * Filters every request and response objects and verifies that <code>org.springframework.security.core.context.SecurityContext</code> is available.
	 * If not <code>org.openinfinity.core.security.principal.Identity</code> which implements the <code>org.springframework.security.core.Authentication</code> interface will be used for storing information to local <code>org.springframework.security.core.context.SecurityContext</code>.
	 */

	/*
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		LOGGER.debug("Initializing InjectableSecurityContextFilterBean for Spring security.");
		if (SecurityContextHolder.getContext() != null
				&& SecurityContextHolder.getContext().getAuthentication() == null) {
			String sessionId = (String) request.getAttribute(ATTRIBUTE_BASED_SESSION_IDENTIFIER);
			if (sessionId == null){
				sessionId = (String) request.getHeader(ATTRIBUTE_BASED_SESSION_IDENTIFIER);
			}
			if (sessionId != null) {
				LOGGER.debug("Session identifier [" + sessionId + "] found from the request.");
				eraseSecurityContext(sessionId);
				injectIdentityBasedSecurityContext(sessionId);
			}
		}
		LOGGER.debug("Finalized injecting authentication in InjectableSecurityContextFilterBean.");
		chain.doFilter(request, response);
	}
	*/

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		LOGGER.debug("BLAHInitializing InjectableSecurityContextFilterBean for Spring security.");
		//SecurityContextHolder.getContext().getAuthentication()
		if (SecurityContextHolder.getContext() != null
				&& SecurityContextHolder.getContext().getAuthentication() == null) {
			LOGGER.debug("Creating security context.");

			String sessionId = (String) request.getAttribute(ATTRIBUTE_BASED_SESSION_IDENTIFIER);
			if (sessionId == null){
				LOGGER.debug("Session identifier by attribute is null.");
				HttpServletRequest r = (HttpServletRequest) request;
				LOGGER.debug("HEADER_BASED_SESSION_IDENTIFIER:" + HEADER_BASED_SESSION_IDENTIFIER);

				sessionId = (String) r.getHeader(HEADER_BASED_SESSION_IDENTIFIER);
				LOGGER.debug("sessionId: " + sessionId);

			}
			if (sessionId != null) {
				LOGGER.debug("Session identifier [" + sessionId + "] found from the request.");
				eraseSecurityContext(sessionId);
				injectIdentityBasedSecurityContext(sessionId);
			}
			else{
				LOGGER.info("Session identifier is null.");
			}
		}
		LOGGER.debug("BLAHFinalized injecting authentication in InjectableSecurityContextFilterBean.");
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

		/*
		 This was returning null, which is very strange beceuse it works fine on BAS with attribute based
		session id. The funtion getAuthentication(String sessionId) is unsed instead, and this apparently works
		*/

		//Authentication authentication = IdentityContext.loadIdentity(sessionId);

		Authentication authentication = getAuthentication(sessionId);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		LOGGER.info("SecurityContext created for active session based on IdentityContext for user: " + authentication.getName());
	}
	private Identity getAuthentication(String sessionId) {
		Identity identity = null;
		try {
			ClientConfig clientConfig = new ClientConfig();
			clientConfig.getGroupConfig().setName("dev").setPassword("dev-pass");
			clientConfig.addAddress("127.0.0.1:15701");
			HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

			IN_MEMORY_DATAGRID = client.getMap(IDENTITY_IDENTIFIER);
			identity = IN_MEMORY_DATAGRID.get(sessionId);
		} catch (Exception e) {
			LOGGER.error("bad thing happened");
			e.printStackTrace();
		}
		return identity;
	}
}
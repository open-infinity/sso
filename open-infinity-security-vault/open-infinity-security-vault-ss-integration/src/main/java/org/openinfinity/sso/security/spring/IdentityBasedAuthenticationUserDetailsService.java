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

import java.security.Principal;
import java.util.Collection;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.openinfinity.core.security.principal.Identity;
import org.openinfinity.sso.security.context.grid.IdentityContext;
import org.openinfinity.sso.security.util.GlobalVariables;
import org.openinfinity.sso.security.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.Assert;

/**
 * Custom user details service providing 
 * 
 * @author Ilkka Leinonen
 * @since 1.0.0
 * @version 1.0.0
 */
public class IdentityBasedAuthenticationUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

	private static final Logger LOGGER = LoggerFactory.getLogger(IdentityBasedAuthenticationUserDetailsService.class);
	
	/**
	 * Session identifier defines the cookie name.
	 */
	private static String ATTRIBUTE_SESSION_IDENTIFIER =  PropertiesUtil.loadValue(GlobalVariables.ATTRIBUTE_BASED_SESSION_KEY);
	
	/**
	 * Session identifier defines the cookie name.
	 */
	private static String HEADER_SESSION_IDENTIFIER =  PropertiesUtil.loadValue(GlobalVariables.ATTRIBUTE_BASED_SESSION_KEY);
	
	@Autowired
	private HttpServletRequest httpServletRequest;
	
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
		LOGGER.debug("IdentityBasedAuthenticationUserDetailsService.loadUserDetails initialized.");
		String sessionIdentifier = httpServletRequest.getAttribute(ATTRIBUTE_SESSION_IDENTIFIER) != null ? 
				(String) httpServletRequest.getAttribute(ATTRIBUTE_SESSION_IDENTIFIER) : 
				(String) httpServletRequest.getAttribute(HEADER_SESSION_IDENTIFIER);
		String sessionId = (String) httpServletRequest.getAttribute(sessionIdentifier);
		Assert.isNull(sessionId, "Session id not found from the request.");
		LOGGER.debug("IdentityBasedAuthenticationUserDetailsService.loadUserDetails fetched identity with session id [" + sessionId + "]");
		final Identity identity = IdentityContext.loadIdentity(sessionId);
		LOGGER.debug("IdentityBasedAuthenticationUserDetailsService.loadUserDetails session found for identity id [" + identity.getUserPrincipal().getName() + "]");
		token.setDetails(identity);
		return new UserDetails() {

			private static final long serialVersionUID = 1404244132102359899L;

			public Collection<? extends GrantedAuthority> getAuthorities() {
				Collection<GrantedAuthority> grantedAuthorities = new TreeSet<GrantedAuthority>();
				for (Principal principal : identity.getAllPrincipalsForIdentity()) {
					GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(principal.getName());
					grantedAuthorities.add(grantedAuthority);
				}
				return grantedAuthorities;
			}

			public String getPassword() {
				return identity.getPassword();
			}

			public String getUsername() {
				return identity.getUserPrincipal().getName();
			}

			public boolean isAccountNonExpired() {
				return true;
			}

			public boolean isAccountNonLocked() {
				return true;
			}

			public boolean isCredentialsNonExpired() {
				return true;
			}

			public boolean isEnabled() {
				return true;
			}
			
		};
	}

}
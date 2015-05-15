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
package org.openinfinity.sso.valve.mapper;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.catalina.connector.Request;
import org.openinfinity.core.security.principal.Identity;
import org.openinfinity.core.security.principal.RolePrincipal;
import org.openinfinity.core.security.principal.TenantPrincipal;
import org.openinfinity.core.security.principal.UserPrincipal;
import org.openinfinity.sso.security.util.PropertiesUtil;

import static org.openinfinity.sso.security.util.GlobalVariables.*;

/**
 * Maps request attributes to <code>org.openinfinity.core.security.principal.Identity</code> object's principals. 
 *  
 * @author Ilkka Leinonen
 * @version 1.0.0
 * @since 1.0.0
 */
public class RequestToHeaderMapper implements RequestToIdentityMapper {
	
	/**
	 * Delimiter mark for splitting roles from session's attribute.
	 */
	private static final String ROLE_DELIMITER = PropertiesUtil.loadValue(HEADER_BASED_ROLE_DELIMITER);

	/**
	 * Delimiter mark for splitting user attributes from session.
	 */
	private static final String USER_ATTRIBUTE_DELIMITER = PropertiesUtil.loadValue(HEADER_BASED_USER_ATTRIBUTE_DELIMITER);
	
	/**
	 * Represents the username attribute key.
	 */
	public static final String USER_NAME = PropertiesUtil.loadValue(HEADER_BASED_USERNAME_KEY);

	/**
	 * Represents the tenant's unique identifier attribute key.
	 */
	public static final String TENANT_ID = PropertiesUtil.loadValue(HEADER_BASED_TENANT_ID_KEY);
	
	/**
	 * Represents the user roles attribute key.
	 */
	public static final String USER_ROLES = PropertiesUtil.loadValue(HEADER_BASED_ROLES_KEY);
	
	/**
	 * Represents the user attribute keys.
	 */
	public static final String USER_ATTRIBUTE_KEYS = PropertiesUtil.loadValue(HEADER_BASED_USER_ATTRIBUTES);
	
	/**
	 * Represents the prefix for the role.
	 */
	private static String ROLE_PREFIX = "ROLE_";
		
	/**
	 * Maps the request attributes to the federated <code>org.openinfinity.core.security.principal.Identity</code> object.
	 * 
	 * @param request Represents the actual request by service provider.
	 * @return <code>org.openinfinity.core.security.principal.Identity</code> Represents the federated identity object in runtime.
	 */
	public Identity map(Request request) {
		String username = (String) (request.getHeader(USER_NAME) != null ? request.getHeader(USER_NAME) : null);
		String tenantId = (String) (request.getHeader(TENANT_ID) != null ? request.getHeader(TENANT_ID) : null);
		String userRoles = ((String) (request.getHeader(USER_ROLES) != null ? request.getHeader(USER_ROLES) : null));
		String[] splittedUserRoles = userRoles!=null ? userRoles.split(ROLE_DELIMITER) : null;
		Identity identity = new Identity();
		populatePrincipals(username, tenantId, splittedUserRoles, identity);
		populateUserAttributes(request, identity);
		identity.setAuthenticated(true);
		return identity;
	}

	private void populatePrincipals(String username, String tenantId, String[] userRoles, Identity identity) {
		if (username != null) {
			UserPrincipal userPrincipal = new UserPrincipal(username);
			identity.setUserPrincipal(userPrincipal);
		}
		if (tenantId != null) {
			TenantPrincipal<?> tenantPrincipal = new TenantPrincipal<Object>(tenantId);
			identity.setTenantPrincipal(tenantPrincipal);
		}
		if (userRoles != null) {
			Collection<RolePrincipal> rolePrincipals = new ArrayList<RolePrincipal>();
			for (String role : userRoles) {
				RolePrincipal rolePrincipal = new RolePrincipal(ROLE_PREFIX + role);
				rolePrincipals.add(rolePrincipal);
			}
			identity.setRolePrincipals(rolePrincipals);
		}
	}

	private void populateUserAttributes(Request request, Identity identity) {
		String[] splittedAttributesKeys = USER_ATTRIBUTE_KEYS.split(USER_ATTRIBUTE_DELIMITER);
		for (String attributeKey : splittedAttributesKeys) {
			String attributeValue = (String) request.getHeader(attributeKey);
			if (attributeValue != null) {
				identity.addAttribute(attributeKey, attributeValue);
			}
		}
	}
	
}
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
package org.openinfinity.sso.security.util;

/**
 * Represents common global variables for the security vault. 
 *  
 * @author Ilkka Leinonen
 * @version 1.0.0
 * @since 1.0.0
 */
public interface GlobalVariables {
	
	/**
	 * Represents the configuration key for attribute based session from the properties file.
	 */
	public static final String ATTRIBUTE_BASED_SESSION_KEY = "sso.attribute.session.identifier"; 

	/**
	 * Represents the configuration key for attribute based username from the properties file.
	 */
	public static final String ATTRIBUTE_BASED_USERNAME_KEY = "sso.attribute.session.username"; 

	/**
	 * Represents the configuration key for attribute based tenant id from the properties file.
	 */
	public static final String ATTRIBUTE_BASED_TENANT_ID_KEY = "sso.attribute.session.tenant.id"; 

	/**
	 * Represents the configuration key for attribute based roles from the properties file.
	 */
	public static final String ATTRIBUTE_BASED_ROLES_KEY = "sso.attribute.session.roles"; 
	
	/**
	 * Represents the attribute based user's session attributes key fetched from the properties file.
	 */
	public static final String ATTRIBUTE_BASED_USER_ATTRIBUTES = "sso.attribute.session.attributes"; 

	/**
	 * Represents the configuration key for header based session from the properties file.
	 */
	public static final String HEADER_BASED_SESSION_KEY = "sso.header.session.identifier"; 

	/**
	 * Represents the configuration key for header based username from the properties file.
	 */
	public static final String HEADER_BASED_USERNAME_KEY = "sso.header.session.username"; 

	/**
	 * Represents the configuration key for header based tenant id from the properties file.
	 */
	public static final String HEADER_BASED_TENANT_ID_KEY = "sso.header.session.tenant.id"; 

	/**
	 * Represents the configuration key for header based roles from the properties file.
	 */
	public static final String HEADER_BASED_ROLES_KEY = "sso.header.session.roles"; 
	
	/**
	 * Represents the role delimiter key for attribute based sessions.
	 */
	public static final String ATTRIBUTE_BASED_ROLE_DELIMITER = "sso.attribute.session.role.delimiter"; 
		
	/**
	 * Represents the role delimiter key for header based sessions.
	 */
	public static final String HEADER_BASED_ROLE_DELIMITER = "sso.header.session.role.delimiter"; 
	
	/**
	 * Represents the attribute based role delimiter key for user attributes.
	 */
	public static final String ATTRIBUTE_BASED_USER_ATTRIBUTE_DELIMITER = "sso.attribute.session.user.attribute.delimiter"; 
	
	/**
	 * Represents the header based role delimiter key for user attributes.
	 */
	public static final String HEADER_BASED_USER_ATTRIBUTE_DELIMITER = "sso.header.session.user.attribute.delimiter"; 
	
	/**
	 * Represents the configuration key for grid configuration file location from the properties file.
	 */
	public static final String GRID_CONFIGURATION_KEY = "sso.session.grid.configuration";

	/**
	 * Represents the header based user's session attributes key fetched from the properties file.
	 */
	public static final String HEADER_BASED_USER_ATTRIBUTES = "sso.header.session.attributes"; 
	
	/**
	 * Security vault properties location identifier.
	 */
	public static final String SECURITY_VAULT_PROPERTIES_FILE_LOCATION = "security.vault.properties.file";
	
}

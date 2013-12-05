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
	 * Represents the configuration key for session from the properties file.
	 */
	public static final String SESSION_KEY = "sso.session.identifier"; 

	/**
	 * Represents the configuration key for username from the properties file.
	 */
	public static final String USERNAME_KEY= "sso.session.username"; 

	/**
	 * Represents the configuration key for tenant id from the properties file.
	 */
	public static final String TENANT_ID_KEY= "sso.session.tenant.id"; 

	/**
	 * Represents the configuration key for roles from the properties file.
	 */
	public static final String ROLES_KEY= "sso.session.roles"; 
	
	/**
	 * Represents the configuration key for grid configuration file location from the properties file.
	 */
	public static final String GRID_CONFIGURATION_KEY = "sso.session.grid.configuration";
	
}

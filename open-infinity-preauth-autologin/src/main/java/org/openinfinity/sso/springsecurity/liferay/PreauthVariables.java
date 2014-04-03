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
package org.openinfinity.sso.springsecurity.liferay;

/**
 * A post-logout action that redirects to a special SAML logout URL for a global logout.
 *
 * @author Ilkka Leinonen
 * @author Tommi Siitonen
 * @since 1.5.0
 */
public class PreauthVariables {

	private static final String BASE_PROPERTY = "user.principal.property.";
	
	public static final String SCREEN_NAME = BASE_PROPERTY + "screenname";
	
	public static final String EMAIL_ADDRESS = BASE_PROPERTY + "emailaddress";
	
	public static final String FACEBOOK_ID = BASE_PROPERTY +  "facebookid";
	
	public static final String OPEN_ID = BASE_PROPERTY + "openid";
	
	public static final String LOCALE = BASE_PROPERTY + "locale";
	
	public static final String FIRST_NAME = BASE_PROPERTY + "firstname";
	
	public static final String MIDDLE_NAME = BASE_PROPERTY + "middlename";
	
	public static final String LAST_NAME = BASE_PROPERTY + "lastname";
	
	public static final String PREFIX = BASE_PROPERTY + "prefix";
	
	public static final String SUFFIX= BASE_PROPERTY + "suffix";
	
	public static final String SEX = BASE_PROPERTY + "sex";
	
	public static final String BIRTH_DATE = BASE_PROPERTY + "birthdate";
	
	public static final String JOB_TITLE = BASE_PROPERTY + "jobtitle";
	
	public static final String PATTERN_ORGANIZATIONS = "user.principal.pattern.organizations";
	
	public static final String PATTERN_ROLES_ORGANIZATION = "user.principal.pattern.roles.organization";
	
	public static final String PATTERN_ROLES_GENERAL = "user.principal.pattern.roles.general";
	
	public static final String PATTERN_USERGROUPS = "user.principal.pattern.usergroups";
	
}
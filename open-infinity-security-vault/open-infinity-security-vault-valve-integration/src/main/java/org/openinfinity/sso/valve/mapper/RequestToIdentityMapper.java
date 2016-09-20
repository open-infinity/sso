/*
 * Copyright (c) 2013-2016 the original author or authors.
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

import org.apache.catalina.connector.Request;
import org.openinfinity.core.security.principal.Identity;

/**
 * Interface for mapping request content to <code>org.openinfinity.core.security.principal.Identity</code> object's principals.
 * 
 * @author Ilkka Leinonen
 * @version 1.0.0
 * @since 1.0.0
 */
public interface RequestToIdentityMapper {
	
	/**
	 * Maps the actual request to the code>org.openinfinity.core.security.principal.Identity</code> object.
	 * 
	 * @param request Represents the intercepted request.
	 * @return
	 */
	public Identity map(Request request);

}

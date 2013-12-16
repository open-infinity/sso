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

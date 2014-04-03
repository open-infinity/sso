/*
 * Copyright (c) 2012 the original author or authors.
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

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A post-logout action that redirects to a special SAML logout URL for a global logout.
 *
 * @author Mika Salminen
 * @author Ilkka Leinonen
 * @since 1.0.0
 */
public class SAMLPostLogoutAction extends Action {
    @Override
    public void run(HttpServletRequest httpServletRequest,
                    HttpServletResponse httpServletResponse)
            throws ActionException {
        try {
	    // TODO - parametrisize this
        	if (httpServletRequest.getRequestURI().startsWith("/c/portal/logout")) {
            	httpServletResponse.sendRedirect("/Shibboleth.sso/Logout");
        	}
        } catch (IOException e) {
            throw new ActionException(e);
        }
    }
}

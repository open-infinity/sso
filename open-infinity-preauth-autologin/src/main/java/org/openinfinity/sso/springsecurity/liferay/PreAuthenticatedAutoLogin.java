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

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.AutoLogin;
import com.liferay.portal.security.auth.AutoLoginException;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PrefsPropsUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.liferay.portal.kernel.util.Validator.isNotNull;

/**
 * A Liferay AutoLogin implementation that expects that the login is already
 * been done by some mechanism, and the principal can be obtained from the HTTP
 * Servlet request.
 *
 * @author Mika Salminen
 * @author Ilkka Leinonen
 * @since 1.0.0
 */
public class PreAuthenticatedAutoLogin implements AutoLogin {

    private static final Log LOG =
            LogFactoryUtil.getLog(PreAuthenticatedAutoLogin.class);

    private static final String PROVISIONING_KEY_NAME = "provisioning";


    public String[] login(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse)
            throws AutoLoginException {
    	
        LiferayRequest request = new LiferayRequest(httpServletRequest);    	
        
        try {

            User user;
            try {
                user = liferayUserBy(request);
                if (isProvisioned(request)) {
                    request.mergeTo(user);
                }
            } catch (NoSuchUserException nse) {
                if (!isProvisioned(request)) {
                    throw nse;
                }
                user = request.createUser();
                // user merged after creation 
                request.mergeTo(user);                
            }
            
            setRedirectAttributeIfNeededTo(httpServletRequest);

            String userID = String.valueOf(user.getUserId()),
                    password = user.getPassword(),
                    isPasswordEncoded = Boolean.FALSE.toString();

            return new String[]{userID, password, isPasswordEncoded};

        } catch (PortalException e) {
            LOG.error(e);
            throw new AutoLoginException(e);
        } catch (SystemException e) {
            LOG.error(e);
            throw new AutoLoginException(e);
        }
    }


    private User liferayUserBy(LiferayRequest request)
            throws PortalException, SystemException {
        return UserLocalServiceUtil.getUserByScreenName(
                request.companyID(), request.screenName());
    }

    private void setRedirectAttributeIfNeededTo(
            HttpServletRequest httpServletRequest) {
    	String redirect = ParamUtil.getString(
                httpServletRequest, "redirect");
        if (isNotNull(redirect)) {
            httpServletRequest.setAttribute(
                    AutoLogin.AUTO_LOGIN_REDIRECT, redirect);
        }
    }

    private boolean isProvisioned(LiferayRequest request) {
        try {
            String stringValue = PrefsPropsUtil
                    .getString(request.companyID(), PROVISIONING_KEY_NAME);
            return Boolean.valueOf(stringValue);
        } catch (Exception e) {
            LOG.error(e);
            return true;
        }
    }
}

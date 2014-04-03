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
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.ProtectedServletRequest;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.InstancePool;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.AutoLogin;
import com.liferay.portal.security.pwd.PwdEncryptor;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.servlet.filters.BasePortalFilter;
import com.liferay.portal.util.PortalInstances;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PropsValues;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.Boolean;
import java.lang.Exception;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

/**
 * A filter that basically duplicates the functionality of Liferay
 * {@code AutologinFilter} but checks for a flag set by a preceeding
 * authentication mechanism to determine whether further authentication is
 * needed.
 * <p/>
 * The regular {@code AutologinFilter } determines that the filter is not needed
 * if the user can be obtained with {@code request.getRemoteUser()}. This
 * doesn't fit the pre-authenticated scenario because even if the user is
 * authenticated and can be obtained from the request, the autologin filter
 * still needs to be run to load the Liferay specific data. This filter runs the
 * autologin hooks if there exists a
 * {@code org.openinfinity.sso.just.authenticated} request attribute.
 *
 * @author Mika Salminen
 * @author Ilkka Leinonen
 * @todo using the original filter w/ some kind of hook would be MUCH preferred
 * @since 1.0.0
 */
public class PreAuthenticationAwareAutologinFilter extends BasePortalFilter {

    private static final String _PATH_CHAT_LATEST = "/-/chat/latest";

    private static final String LIFERAY_USER_ID_SESSION_ATTRIBUTE_KEY =
            "org.openinfinity.sso.liferay.user.id";

    private static Log _log = LogFactoryUtil.getLog(
            PreAuthenticationAwareAutologinFilter.class);

    private static AutoLogin[] _autoLogins;


    public static void registerAutoLogin(AutoLogin autoLogin) {
        if (_autoLogins == null) {
            _log.error("AutoLoginFilter is not initialized yet");

            return;
        }

        List<AutoLogin> autoLogins = ListUtil.fromArray(_autoLogins);

        autoLogins.add(autoLogin);

        _autoLogins = autoLogins.toArray(new AutoLogin[autoLogins.size()]);
    }

    public static void unregisterAutoLogin(AutoLogin autoLogin) {
        if (_autoLogins == null) {
            _log.error("AutoLoginFilter is not initialized yet");

            return;
        }

        List<AutoLogin> autoLogins = ListUtil.fromArray(_autoLogins);

        if (autoLogins.remove(autoLogin)) {
            _autoLogins = autoLogins.toArray(new AutoLogin[autoLogins.size()]);
        }
    }

    public PreAuthenticationAwareAutologinFilter() {
        List<AutoLogin> autoLogins = new ArrayList<AutoLogin>();

        for (String autoLoginClassName : PropsValues.AUTO_LOGIN_HOOKS) {
            AutoLogin autoLogin = (AutoLogin) InstancePool.get(
                    autoLoginClassName);

            autoLogins.add(autoLogin);
        }

        _autoLogins = autoLogins.toArray(new AutoLogin[autoLogins.size()]);
    }

    protected String getLoginRemoteUser(
            HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String[] credentials)
            throws Exception {

        if ((credentials != null) && (credentials.length == 3)) {
            String jUsername = credentials[0];
            String jPassword = credentials[1];
            boolean encPassword = GetterUtil.getBoolean(credentials[2]);

            if (Validator.isNotNull(jUsername) &&
                    Validator.isNotNull(jPassword)) {

                try {
                    long userId = GetterUtil.getLong(jUsername);

                    if (userId > 0) {
                        User user = UserLocalServiceUtil.getUserById(userId);

                        if (user.isLockout()) {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } catch (NoSuchUserException nsue) {
                    return null;
                }

                session.setAttribute("j_username", jUsername);

                // Not having access to the unencrypted password
                // will not allow you to connect to external
                // resources that require it (mail server)

                if (encPassword) {
                    session.setAttribute("j_password", jPassword);
                } else {
                    session.setAttribute(
                            "j_password", PwdEncryptor.encrypt(jPassword));

                    if (PropsValues.SESSION_STORE_PASSWORD) {
                        session.setAttribute(
                                com.liferay.portal.util.WebKeys.USER_PASSWORD,
                                jPassword);
                    }
                }

                if (PropsValues.PORTAL_JAAS_ENABLE) {
                    response.sendRedirect(
                            PortalUtil.getPathMain()
                                    + "/portal/touch_protected");
                }

                return jUsername;
            }
        }

        return null;
    }

    protected void processFilter(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain)
            throws Exception {

        HttpSession session = request.getSession();

        String userID = (String) session
                .getAttribute(LIFERAY_USER_ID_SESSION_ATTRIBUTE_KEY);
        if (userID != null) {
            request = new LiferayRequestWrapper(request, userID);
        }

        String host = PortalUtil.getHost(request);

        if (PortalInstances.isAutoLoginIgnoreHost(host)) {
            if (_log.isDebugEnabled()) {
                _log.debug("Ignore host " + host);
            }

            processFilter(
                    PreAuthenticationAwareAutologinFilter.class,
                    request, response, filterChain);

            return;
        }

        String contextPath = PortalUtil.getPathContext();

        String path = request.getRequestURI().toLowerCase();

        if ((!contextPath.equals(StringPool.SLASH)) &&
                (path.indexOf(contextPath) != -1)) {

            path = path.substring(contextPath.length(), path.length());
        }

        if (PortalInstances.isAutoLoginIgnorePath(path)) {
            if (_log.isDebugEnabled()) {
                _log.debug("Ignore path " + path);
            }

            processFilter(
                    PreAuthenticationAwareAutologinFilter.class,
                    request, response, filterChain);

            return;
        }

        String remoteUser = request.getRemoteUser();
        String jUserName = (String) session.getAttribute("j_username");

        if (((remoteUser == null) && (jUserName == null)) ||
                autoLoginNeedingValveAuthenticationIn(request, session)) {
            setValveAuthenticationAttributeToFalseIfNeededIn(request);
            for (AutoLogin autoLogin : _autoLogins) {
                try {
                    String[] credentials = autoLogin.login(request, response);

                    session.setAttribute(LIFERAY_USER_ID_SESSION_ATTRIBUTE_KEY,
                            credentials[0]);

                    String redirect = (String) request.getAttribute(
                            AutoLogin.AUTO_LOGIN_REDIRECT);

                    if (Validator.isNotNull(redirect)) {
                        response.sendRedirect(redirect);

                        return;
                    }

                    String loginRemoteUser = getLoginRemoteUser(
                            request, response, session, credentials);

                    if (loginRemoteUser != null) {
                        request = new ProtectedServletRequest(
                                request, loginRemoteUser);

                        if (PropsValues.PORTAL_JAAS_ENABLE) {
                            return;
                        }

                        redirect = (String) request.getAttribute(
                                AutoLogin.AUTO_LOGIN_REDIRECT_AND_CONTINUE);

                        if (Validator.isNotNull(redirect)) {
                            response.sendRedirect(redirect);

                            break;
                        }
                    }
                } catch (Exception e) {
                    if (_log.isWarnEnabled()) {
                        _log.warn(e, e);
                    }

                    String currentURL = PortalUtil.getCurrentURL(request);

                    if (currentURL.endsWith(_PATH_CHAT_LATEST)) {
                        if (_log.isWarnEnabled()) {
                            _log.warn(
                                    "Current URL " + currentURL +
                                            " generates exception: "
                                            + e.getMessage());
                        }
                    } else {
                        _log.error(
                                "Current URL " + currentURL +
                                        " generates exception: "
                                        + e.getMessage());
                    }
                }
            }
        }

        processFilter(PreAuthenticationAwareAutologinFilter.class,
                request, response, filterChain);
    }


    private void setValveAuthenticationAttributeToFalseIfNeededIn(
            HttpServletRequest request) {
        request.setAttribute(
                "org.openinfinity.security.just.authenticated",
                Boolean.FALSE);
    }

    private boolean autoLoginNeedingValveAuthenticationIn(
            HttpServletRequest request, HttpSession session) {
        String remoteUser = request.getRemoteUser();
        String jUserName = (String) session.getAttribute("j_username");
        Boolean justAuthenticatedByValve = (Boolean) request.getAttribute(
                "org.openinfinity.security.just.authenticated");

        if (justAuthenticatedByValve == null) {
            justAuthenticatedByValve = Boolean.FALSE;
        }
        if (remoteUser == null) {
            return false;
        }
        if (jUserName != null) {
            return false;
        }

        return justAuthenticatedByValve;
    }


    private static class LiferayRequestWrapper
            extends HttpServletRequestWrapper {

        private final String userID;


        LiferayRequestWrapper(HttpServletRequest request, String userID) {
            super(request);
            this.userID = userID;
        }


        @Override
        public String getRemoteUser() {
            return userID;
        }
    }
}
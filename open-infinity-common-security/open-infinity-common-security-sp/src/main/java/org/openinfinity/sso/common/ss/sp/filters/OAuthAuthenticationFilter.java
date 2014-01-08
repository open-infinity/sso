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
package org.openinfinity.sso.common.ss.sp.filters;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.security.*;
import org.mule.api.security.Authentication;import org.mule.api.security.CryptoFailureException;import org.mule.api.security.EncryptionStrategyNotFoundException;import org.mule.api.security.SecurityContext;import org.mule.api.security.SecurityException;
import org.mule.api.security.SecurityProviderNotFoundException;import org.mule.api.security.UnauthorisedException;import org.mule.api.security.UnknownAuthenticationTypeException;import org.mule.config.i18n.CoreMessages;
import org.mule.module.spring.security.SpringAuthenticationAdapter;
import org.mule.security.AbstractAuthenticationFilter;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;import java.lang.Override;import java.lang.String;

/**
 * A Mule authentication filter that checks that the incoming event has an OAuth bearer token
 * in an inbound message property "Authorization", and creates a pre-authenticated
 * Authentication into the security context based on it.
 *
 * @author Mika Salminen
 * @author Ilkka Leinonen
 * @since 1.0.0
 */
public class OAuthAuthenticationFilter extends AbstractAuthenticationFilter {

    private static final Logger LOG =
            LoggerFactory.getLogger(OAuthAuthenticationFilter.class);


    private String realm = "oauth";


    @Override
    public void authenticate(MuleEvent event)
            throws org.mule.api.security.SecurityException,
            UnknownAuthenticationTypeException, CryptoFailureException,
            SecurityProviderNotFoundException,
            EncryptionStrategyNotFoundException, InitialisationException {
        LOG.debug("Authenticating Mule Event by OAuth 2");
        String accessToken = accessTokenFrom(event);
        PreAuthenticatedAuthenticationToken authRequest =
                new PreAuthenticatedAuthenticationToken(accessToken, "N/A");

        Authentication authResult = authenticate(event, authRequest);

        createSecurityContextToEventWithAuthResult(event, authResult);
    }


    private void createSecurityContextToEventWithAuthResult(
            MuleEvent event, org.mule.api.security.Authentication authResult)
            throws UnknownAuthenticationTypeException {
        SecurityContext context =
                getSecurityManager().createSecurityContext(authResult);
        context.setAuthentication(authResult);
        event.getSession().setSecurityContext(context);
    }

    private org.mule.api.security.Authentication authenticate(
            MuleEvent event, PreAuthenticatedAuthenticationToken authRequest)
            throws SecurityException, SecurityProviderNotFoundException {
        org.mule.api.security.Authentication authResult;
        try {
            authResult = getSecurityManager().authenticate(
                    new SpringAuthenticationAdapter(authRequest));
        } catch (UnauthorisedException e) {
            setUnauthenticated(event);
            throw new UnauthorisedException(
                    CoreMessages.authFailedForUser("?"), e);
        }
        return authResult;
    }

    private void failIfEmptyTokenInEvent(String accessToken, MuleEvent event)
            throws UnauthorisedException {
        if (accessToken == null) {
            setUnauthenticated(event);
            throw new UnauthorisedException(event,
                    event.getSession().getSecurityContext(), this);
        }
    }

    private String accessTokenFrom(MuleEvent event)
            throws UnauthorisedException {
        LOG.debug("Getting the access token from Mule event");

        String authHeader = event.getMessage().getInboundProperty(
                HttpConstants.HEADER_AUTHORIZATION);
        LOG.debug("Auth header value {} found", authHeader);

        failIfEmptyTokenInEvent(authHeader, event);

        return parseAccessTokenFromHeaderValue(authHeader);
    }


    private String parseAccessTokenFromHeaderValue(final String authHeader) {
        if ((authHeader.toLowerCase().startsWith(
                OAuth2AccessToken.BEARER_TYPE.toLowerCase()))) {
            String authHeaderValue = authHeader.substring(
                    OAuth2AccessToken.BEARER_TYPE.length()).trim();
            int commaIndex = authHeaderValue.indexOf(',');
            if (commaIndex > 0) {
                authHeaderValue = authHeaderValue.substring(0, commaIndex);
            }
            LOG.debug("OAuth token {} found", authHeaderValue);
            return authHeaderValue;
        } else {
            // todo: support additional authorization schemes for
            // different token types, e.g. "MAC" specified by
            // http://tools.ietf.org/html/draft-hammer-oauth-v2-mac-token
        }

        LOG.warn("No OAuth token found in Mule event!");

        return null;
    }

    private void setUnauthenticated(final MuleEvent event) {
        String realmHeader = "Basic realm=";
        if (realm != null) {
            realmHeader += "\"" + realm + "\"";
        }
        MuleMessage msg = event.getMessage();
        msg.setOutboundProperty(HttpConstants.HEADER_WWW_AUTHENTICATE,
                realmHeader);
        msg.setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
                HttpConstants.SC_UNAUTHORIZED);
    }
}

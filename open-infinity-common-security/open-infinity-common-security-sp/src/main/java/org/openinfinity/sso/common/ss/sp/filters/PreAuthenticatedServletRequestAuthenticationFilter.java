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
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.security.*;
import org.mule.api.security.Authentication;
import org.mule.api.security.CryptoFailureException;import org.mule.api.security.EncryptionStrategyNotFoundException;import org.mule.api.security.SecurityContext;import org.mule.api.security.SecurityException;
import org.mule.api.security.SecurityProviderNotFoundException;import org.mule.api.security.UnauthorisedException;import org.mule.api.security.UnknownAuthenticationTypeException;import org.mule.module.spring.security.SpringAuthenticationAdapter;
import org.mule.security.AbstractAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * This security filter expects that the user has already been authenticated and the
 * successful authentication is found in a Spring Security construct SecurityContextHolder.
 * The filter accesses the security context through this structure and sets the context as a
 * Mule security context.
 *
 * @author Mika Salminen
 * @author Ilkka Leinonen
 * @since 1.0.0
 */
public class PreAuthenticatedServletRequestAuthenticationFilter
        extends AbstractAuthenticationFilter {

    private static final Logger LOG = LoggerFactory.getLogger(
            PreAuthenticatedServletRequestAuthenticationFilter.class);


    public void authenticate(MuleEvent event) throws SecurityException,
            UnknownAuthenticationTypeException, CryptoFailureException,
            SecurityProviderNotFoundException,
            EncryptionStrategyNotFoundException, InitialisationException {

        LOG.debug("Authenticating Mule event by preauthenticated context");

        org.springframework.security.core.Authentication springAuthentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (nullOrUnauthorized(springAuthentication)) {
            LOG.error("Null or unauthorized security context {}",
                    event.getSession().getSecurityContext());
            throw new UnauthorisedException(event,
                    event.getSession().getSecurityContext(), this);
        }

        LOG.debug("Creating Mule authentication object from Spring one, " +
                "setting it to context");
        Authentication authentication =
                new SpringAuthenticationAdapter(springAuthentication);
        SecurityContext context = getSecurityManager()
                .createSecurityContext(authentication);
        context.setAuthentication(authentication);
        event.getSession().setSecurityContext(context);
    }


    private boolean nullOrUnauthorized(
            org.springframework.security.core.Authentication
                    springAuthentication) {
        return springAuthentication == null ||
                !springAuthentication.isAuthenticated();
    }
}

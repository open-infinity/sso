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
import org.mule.module.spring.security.SpringAuthenticationAdapter;
import org.mule.security.AbstractAuthenticationFilter;
import org.openinfinity.sso.common.ss.sp.SpringAuthenticationMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.lang.Override;
import java.lang.String;
import java.lang.System;
import java.util.HashMap;
import java.util.Map;

/**
 * This security filter expects that the user has already been authenticated and the
 * successful authentication is manifested in an inbound Mule message property
 * "org.openinfinity.sso.id" that contains an SSO session identifier. The filter accesses the
 * security context through this identifier and sets the context as a Mule security context.
 * The security context is cached and cache characteristics can be configured.
 * <p/>
 * NOTE: THIS IS OPENAM-SPECIFIC FUNCTIONALITY - USES OPENAM REST SERVICE TO
 * RESOLVE SECURITY CONTEXT.
 *
 * @author Mika Salminen
 * @author Ilkka Leinonen
 * @since 1.0.0
 */
public class PreAuthenticatedTokenAuthenticationFilter
        extends AbstractAuthenticationFilter {

    private static final Logger LOG = LoggerFactory.getLogger(
            PreAuthenticatedTokenAuthenticationFilter.class);

    private static final String SSO_ID_KEY = "org.openinfinity.sso.id";


    private Cache cache = new Cache();

    private String attributeURL =
            "http://localhost:8080/openam/identity/" +
                    "attributes?subjectid={tokenID}";


    public void setAttributeURL(String attributeURL) {
        this.attributeURL = attributeURL;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }


    @Override
    public void authenticate(MuleEvent event)
            throws org.mule.api.security.SecurityException,
            UnknownAuthenticationTypeException, CryptoFailureException,
            SecurityProviderNotFoundException,
            EncryptionStrategyNotFoundException, InitialisationException {

        LOG.debug("Authenticating by preauthenticated token");

        String ssoID = ssoIDFrom(event);
        if (ssoID == null) {
            LOG.error("No or unauthorized token found in Mule event / " +
                    "context is {}", event.getSession().getSecurityContext());
            throw new UnauthorisedException(event,
                    event.getSession().getSecurityContext(), this);
        }

        org.springframework.security.core.Authentication springAuthentication =
                springAuthenticationBy(ssoID);

        if (nullOrUnauthorized(springAuthentication)) {
            throw new UnauthorisedException(event,
                    event.getSession().getSecurityContext(), this);
        }

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

    private org.springframework.security.core.Authentication
    springAuthenticationBy(final String ssoID) {

        LOG.debug("Building the authentication context by token");

        org.springframework.security.core.Authentication springAuthentication =
                cache.authenticationBy(ssoID);
        if (springAuthentication != null) {
            LOG.debug("Auth {} found in cache by token {}",
                    springAuthentication, ssoID);
            return springAuthentication;
        }

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(new SpringAuthenticationMessageConverter());
        LOG.debug("No luck in cache, calling IDP by REST");
        springAuthentication = restTemplate.getForObject(attributeURL,
                org.springframework.security.core.Authentication.class,
                ssoID);
        LOG.debug("Got authentication {} from IDP via REST",
                springAuthentication);

        cache.put(ssoID, springAuthentication);
        return springAuthentication;
    }

    private String ssoIDFrom(final MuleEvent event) {
        String ssoID = event.getMessage().getInboundProperty(SSO_ID_KEY);
        LOG.debug("SSO ID in Mule event is {}", ssoID);
        return ssoID;
    }


    public static class Cache {

        private Map<String, Entry> map = new HashMap<String, Entry>();

        private long stalenessMillis = 2000L;

        private long cleanUpMillis = 10 * 60 * 1000L; // 10 minutes

        private long lastCleanUpMillis = System.currentTimeMillis();


        public void setStalenessMillis(long stalenessMillis) {
            this.stalenessMillis = stalenessMillis;
        }

        public void setCleanUpMillis(long cleanUpMillis) {
            this.cleanUpMillis = cleanUpMillis;
        }

        void put(String ssoID,
                 org.springframework.security.core.Authentication value) {
            cleanUpIfNeeded();
            map.put(ssoID, new Entry(value));
        }

        org.springframework.security.core.Authentication authenticationBy(
                String ssoId) {
            cleanUpIfNeeded();
            Entry entry = map.get(ssoId);
            if (entry == null) {
                return null;
            }
            if (stale(entry)) {
                map.remove(ssoId);
                return null;
            }
            return entry.value;
        }


        private boolean stale(Entry entry) {
            return (System.currentTimeMillis() - entry.lastAccessMillis) >
                    stalenessMillis;
        }

        private void cleanUpIfNeeded() {
            if (cleanUpNeeded()) {
                cleanUp();
            }
        }

        private void cleanUp() {
            map.clear();
            lastCleanUpMillis = System.currentTimeMillis();
        }

        private boolean cleanUpNeeded() {
            return (System.currentTimeMillis() - lastCleanUpMillis) > cleanUpMillis;
        }


        private static class Entry {
            private long lastAccessMillis;

            private org.springframework.security.core.Authentication value;

            Entry(org.springframework.security.core.Authentication value) {
                this.value = value;
                this.lastAccessMillis = System.currentTimeMillis();
            }
        }
    }
}

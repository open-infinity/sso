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

import org.mule.api.MuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.*;import javax.servlet.Filter;import javax.servlet.FilterChain;import javax.servlet.ServletException;import javax.servlet.ServletRequest;import javax.servlet.ServletResponse;
import java.io.IOException;import java.lang.IllegalStateException;import java.lang.Object;

/**
 * A class that functions like a Spring DelegatingFilterProxy but uses application context initiated
 * by Mule as the web application context..
 *
 * @see DelegatingFilterProxy
 * @author Mika Salminen
 * @author Ilkka Leinonen
 * @since 1.0.0
 */
public class MuleContextUsingDelegatingFilterProxy
        extends DelegatingFilterProxy {

    private static final Logger LOG = LoggerFactory
            .getLogger(MuleContextUsingDelegatingFilterProxy.class);


    private Filter delegate;

    private final Object delegateMonitor = new Object();


    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain filterChain)
            throws ServletException, IOException {
        lazilyInitializeDelegateIfNecessary();
        invokeDelegate(delegate, request, response, filterChain);
    }


    private void lazilyInitializeDelegateIfNecessary()
            throws ServletException {
        synchronized (delegateMonitor) {
            if (delegate == null) {
                LOG.debug("Lazily initializing application context");
                ApplicationContext ac = findApplicationContext();
                if (ac == null) {
                    LOG.error("Failed to find application context");
                    throw new IllegalStateException("KERNEL PANIC");
                }
                this.delegate = initDelegate(ac);
            }
        }
    }

    private ApplicationContext findApplicationContext() {
        LOG.debug("Getting Mule context by attribute 'mule.context'");
        MuleContext muleContext = (MuleContext)
                getServletContext().getAttribute("mule.context");
        LOG.debug("Found {} - trying to look up object " +
                "'springApplicationContext' from the registry", muleContext);
        return muleContext.getRegistry()
                .lookupObject("springApplicationContext");
    }

    private Filter initDelegate(ApplicationContext ac)
            throws ServletException {
        LOG.debug("Finding the delegate by bean name {}", getTargetBeanName());
        Filter delegate = ac.getBean(getTargetBeanName(), Filter.class);
        if (isTargetFilterLifecycle()) {
            LOG.debug("Initializing the delegate");
            delegate.init(getFilterConfig());
        }
        return delegate;
    }
}

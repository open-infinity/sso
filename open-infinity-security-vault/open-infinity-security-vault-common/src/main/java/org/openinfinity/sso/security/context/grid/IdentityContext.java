/*
 * Copyright (c) 2013-2015 the original author or authors.
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
package org.openinfinity.sso.security.context.grid;

import java.io.FileNotFoundException;
import java.util.concurrent.ConcurrentMap;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.openinfinity.core.security.principal.Identity;
import org.openinfinity.sso.security.util.GlobalVariables;
import org.openinfinity.sso.security.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * In-memory datagrid utility for storing <code>org.openinfinity.core.security.principal.Identity</code> objects. 
 * Objects are accessible by any member of the in-memory datagrid.
 *  
 * @author Ilkka Leinonen
 * @version 1.0.0
 * @since 1.0.0
 */
public class IdentityContext implements LifecycleListener {

	/**
	 * Logger for the class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IdentityContext.class);
	
	/**
	 * Name of the <code>org.openinfinity.core.security.principal.Identity</code> context map.
	 */
	private static final String IDENTITY_IDENTIFIER = "IDENTITY_CONTEXT_MAP";
	
	/**
	 * Concurrent in-memory datagrid for storing <code>org.openinfinity.core.security.principal.Identity</code> objects.
	 */
	private static ConcurrentMap<String, Identity> IN_MEMORY_DATAGRID = null;
	
	/**
	 * Stores the actual <code>org.openinfinity.core.security.principal.Identity</code> object into in-memory datagrid.
	 * 
	 * @param sessionId Represents the session id provided by identity provider.
	 * @param identity Represents the actual federated identity.
	 */
	public static void storeIdentity(String sessionId, Identity identity) {
		IN_MEMORY_DATAGRID.put(sessionId, identity);
	}

	/**
	 * 
	 * @param sessionId Represents the session id provided by identity provider.
	 * @return Identity Represents the actual federated identity load from in-memory datagrid.
	 */
	public static Identity loadIdentity(String sessionId) {
		return IN_MEMORY_DATAGRID.get(sessionId);	
	}
	
	/**
	 * Deletes federated identity from the in-memory datagrid.
	 * 
	 * @param sessionId Represents the session id provided by identity provider
	 */
	public static void clear(String sessionId) {
		IN_MEMORY_DATAGRID.remove(sessionId);
	}
	
	/**
	 * Lifecycle listener to start and shutdown an in-memory datagrid for storing <code>org.openinfinity.core.security.principal.Identity</code> objects.
	 */
	public void lifecycleEvent(LifecycleEvent event) {
		if (Lifecycle.START_EVENT.equals(event.getType())) {
			try {
				Config config = new FileSystemXmlConfig(PropertiesUtil.loadValue(GlobalVariables.GRID_CONFIGURATION_KEY));
				HazelcastInstance haze = Hazelcast.newHazelcastInstance(config);
				IN_MEMORY_DATAGRID = haze.getMap(IDENTITY_IDENTIFIER);
			} catch (FileNotFoundException fileNotFoundException) {
				LOGGER.error(fileNotFoundException.getMessage(), fileNotFoundException);
			}	
		} else if (Lifecycle.STOP_EVENT.equals(event.getType())) {
			IN_MEMORY_DATAGRID = null;
			Hazelcast.shutdownAll();
		}    
	}
	
}

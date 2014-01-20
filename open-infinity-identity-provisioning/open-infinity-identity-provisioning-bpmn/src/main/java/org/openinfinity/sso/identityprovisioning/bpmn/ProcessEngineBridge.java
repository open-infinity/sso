/*
 * Copyright (c) 2014 the original author or authors.
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
package org.openinfinity.sso.identityprovisioning.bpmn;

import java.util.Collection;
import java.util.Map;

/**
 * Bridge interface wraps and simplifys usage for BPMN- or workflow -engine.
 * 
 * @author Ilkka Leinonen
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ProcessEngineBridge {
	
	String startProcess(String processDefinitionKey, Map<String, Object> variables);
	
	void claimTask(String taskId);
	
	void completeTask(String taskId);
	
	void completeTask(String taskId, Map<String, Object> variables);
	
	Collection<Task> queryForTasksByUserRoles();
	
	Collection<Task> queryForTasksByRole(String role);
	
	Collection<Task> queryForUserTasks();
	
	Collection<Task> queryForUserTasks(String username);
	
	Collection<Task> queryForAllUserTasks();

}
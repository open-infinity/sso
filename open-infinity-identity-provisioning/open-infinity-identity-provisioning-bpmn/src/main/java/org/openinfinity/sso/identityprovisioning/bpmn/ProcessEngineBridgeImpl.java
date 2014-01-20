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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.openinfinity.core.annotation.AuditTrail;
import org.openinfinity.core.annotation.Log;
import org.openinfinity.core.annotation.Log.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Bridge interface wraps and simplifies usage for BPMN- or workflow -engine. This class implements wrapper for Activiti BPMN 2.0 engine.
 * 
 * @author Ilkka Leinonen
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@Scope("prototype")
public class ProcessEngineBridgeImpl implements ProcessEngineBridge {
	
	@Autowired
	private ProcessEngine processEngine;
	
	@Log(level=LogLevel.INFO)
	@AuditTrail
	public String startProcess(String processDefinitionKey, Map<String, Object> variables) {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		IdentityService identityService = processEngine.getIdentityService();
		identityService.setAuthenticatedUserId(authentication.getName());
		RuntimeService runtimeService =  processEngine.getRuntimeService();
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
		return processInstance.getId();
	}
	
	@Log(level=LogLevel.INFO)
	@AuditTrail
	public void claimTask(String taskId) {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		IdentityService identityService = processEngine.getIdentityService();
		identityService.setAuthenticatedUserId(authentication.getName());
		TaskService taskService =  processEngine.getTaskService();
		taskService.claim(taskId, authentication.getName());
	}
	
	@Log(level=LogLevel.INFO)
	@AuditTrail
	public void completeTask(String taskId) {
		completeTask(taskId, null);
	}
	
	@Log(level=LogLevel.INFO)
	@AuditTrail
	public void completeTask(String taskId, Map<String, Object> variables) {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		IdentityService identityService = processEngine.getIdentityService();
		identityService.setAuthenticatedUserId(authentication.getName());
		TaskService taskService =  processEngine.getTaskService();
		taskService.complete(taskId, variables);
	}
	
	@Log(level=LogLevel.INFO)
	@AuditTrail
	public Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> queryForAllUserTasks() {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		IdentityService identityService = processEngine.getIdentityService();
		identityService.setAuthenticatedUserId(authentication.getName());
		TaskService taskService = processEngine.getTaskService();
		List<Task> tasks = taskService.createTaskQuery().list();
		Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> simpleTasks = mapTasks(tasks);
		return Collections.unmodifiableCollection(simpleTasks);
	}
	
	@Log(level=LogLevel.INFO)
	@AuditTrail
	public Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> queryForUserTasks() {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		IdentityService identityService = processEngine.getIdentityService();
		identityService.setAuthenticatedUserId(authentication.getName());
		TaskService taskService = processEngine.getTaskService();
		List<Task> tasks = taskService.createTaskQuery().taskCandidateUser(authentication.getName()).list();
		Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> simpleTasks = mapTasks(tasks);
		return Collections.unmodifiableCollection(simpleTasks);
	}
	
	@Log(level=LogLevel.INFO)
	@AuditTrail
	public Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> queryForUserTasks(String username) {
		IdentityService identityService = processEngine.getIdentityService();
		identityService.setAuthenticatedUserId(username);
		TaskService taskService = processEngine.getTaskService();
		List<Task> tasks = taskService.createTaskQuery().taskCandidateUser(username).list();
		Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> simpleTasks = mapTasks(tasks);
		return Collections.unmodifiableCollection(simpleTasks);
	}

	public Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> queryForTasksByUserRoles() {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		IdentityService identityService = processEngine.getIdentityService();
		identityService.setAuthenticatedUserId(authentication.getName());
		TaskService taskService = processEngine.getTaskService();
		Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
		List<String> candidateGroups = new ArrayList<String>();
		for (GrantedAuthority grantedAuthority : grantedAuthorities) {
			if (grantedAuthority.getAuthority() != null)
				candidateGroups.add(grantedAuthority.getAuthority());
		}
		List<Task> tasks = taskService.createTaskQuery().taskCandidateGroupIn(candidateGroups).list();
		Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> simpleTasks = mapTasks(tasks);
		return Collections.unmodifiableCollection(simpleTasks);
	}

	public Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> queryForTasksByRole(String role) {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		IdentityService identityService = processEngine.getIdentityService();
		identityService.setAuthenticatedUserId(authentication.getName());
		TaskService taskService = processEngine.getTaskService();
		List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup(role).list();
		Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> simpleTasks = mapTasks(tasks);
		return Collections.unmodifiableCollection(simpleTasks);
	}
	
	private Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> mapTasks(List<Task> tasks) {
		Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> simpleTasks = new ArrayList<org.openinfinity.sso.identityprovisioning.bpmn.Task>();
		for (Task task : tasks) {
			simpleTasks.add(mapActivitiTask(task));
		}
		return simpleTasks;
	}
	
	private org.openinfinity.sso.identityprovisioning.bpmn.Task mapActivitiTask(Task task) {
		org.openinfinity.sso.identityprovisioning.bpmn.Task simpleTask = new org.openinfinity.sso.identityprovisioning.bpmn.Task();
		simpleTask.setId(task.getId());
		simpleTask.setAssignee(task.getAssignee());
		simpleTask.setDescription(task.getDescription());
		simpleTask.setName(task.getName());
		simpleTask.setPriority(task.getPriority());
		simpleTask.setDueDate(task.getDueDate());
		simpleTask.setOwner(task.getOwner());
		simpleTask.setPriority(task.getPriority());
		simpleTask.setProcessExecutionId(task.getProcessDefinitionId());
		simpleTask.setProcessInstanceId(task.getProcessInstanceId());
		simpleTask.setTaskDefinitionKey(task.getTaskDefinitionKey());
		return simpleTask;
	}

}

package org.openinfinity.sso.identityprovisioning.bpmn;

import java.util.Collection;
import java.util.Map;

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
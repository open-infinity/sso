package org.openinfinity.sso.userprovisioning.bpmn.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openinfinity.sso.identityprovisioning.api.IdentityProvisioning;
import org.openinfinity.sso.identityprovisioning.bpmn.ProcessEngineBridge;
import org.openinfinity.sso.identityprovisioning.bpmn.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class NetworkOrderIntegrationTestBean {
		
	@Autowired
	private ProcessEngineBridge processEngineBridge;
	
	@IdentityProvisioning
	public String create(NetworkOrder networkOrder) {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("networkOrder", networkOrder);
		String id = processEngineBridge.startProcess("network-order-process", parameterMap);
		return id;
	}
	
	@IdentityProvisioning
	public Collection<Task> loadById(String id) {
		return processEngineBridge.queryForUserTasks(id);
	}
	
	@IdentityProvisioning
	public Collection<Task> loadUserTaskIds(String processInstanceId) {
		return processEngineBridge.queryForUserTasks(processInstanceId);
	}
	
	@IdentityProvisioning
	public Collection<Task> loadAll() {
		return processEngineBridge.queryForAllUserTasks();
	}
	
	@IdentityProvisioning
	public void claimTask(String taskId) {
		processEngineBridge.claimTask(taskId);
	}	
		
}
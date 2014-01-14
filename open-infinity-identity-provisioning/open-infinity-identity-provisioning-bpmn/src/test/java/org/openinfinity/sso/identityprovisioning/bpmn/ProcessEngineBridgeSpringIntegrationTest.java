package org.openinfinity.sso.identityprovisioning.bpmn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openinfinity.core.security.principal.Identity;
import org.openinfinity.core.security.principal.RolePrincipal;
import org.openinfinity.core.security.principal.UserPrincipal;
import org.openinfinity.core.util.CollectionElementCallback;
import org.openinfinity.core.util.CollectionElementUtil;
import org.openinfinity.sso.userprovisioning.bpmn.common.LoggingRouter;
import org.openinfinity.sso.userprovisioning.bpmn.common.NetworkOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/META-INF/spring/spring-nodeployment-application-context.xml")
public class ProcessEngineBridgeSpringIntegrationTest extends LoggingRouter {

	@Autowired
	private RuntimeService runtimeService;
	
	@Autowired
	private TaskService taskService;
	
	@Autowired
	private ProcessEngine processEngine;

	@Autowired
	@Rule
	public ActivitiRule activitiSpringRule;
	
	@Autowired
	ProcessEngineBridge processEngineBridge;
	
	@Before
	public void setUp() {
		createStubIdentity();
	}

	@Test
	@Deployment(resources = { "META-INF/bpmn/network-connection-order.spring.bpmn20.xml" })
	public void givenKnownParameterMapWithEntityThenStartingTheProcessThenUniqueIdOfTheProcessMustBeGiven() {
		Map<String, Object> parameterMap = createTestEntity();
		
		String id = processEngineBridge.startProcess("network-order-process", parameterMap);
		assertNotNull(id);
	}
	
	@Test
	@Deployment(resources = { "META-INF/bpmn/network-connection-order.spring.bpmn20.xml" })
	public void givenKnownParameterMapWithEntityThenStartingTheProcessThenUniqueIdOfTheProcessMustBeGivenAndIdentityMustBeProvisioned() {
		IdentityService identityService = processEngine.getIdentityService();
		GroupQuery groupQuery = identityService.createGroupQuery();
		List<Group> groups = groupQuery.list();
		for (Group group : groups) {
			assertNotNull(group);
		}
		ExecutionQuery executionQuery = runtimeService.createExecutionQuery();
		List<Execution> executions = executionQuery.list();
		for (Execution execution : executions) {
			System.out.println("Process instance: " + execution.getProcessInstanceId() + " Execution id: " + execution.getId());
		}
		
		Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> userTasks = processEngineBridge.queryForUserTasks();
		for (org.openinfinity.sso.identityprovisioning.bpmn.Task task : userTasks) {
			System.out.println("Taskid : " + task.getId() + " , Task description " + task.getDescription());
		}
		List<Task> tasks = taskService.createTaskQuery().list();
		for (Task task : tasks) {
			assertNotNull(task);
			assertNotNull(task.getName());
			taskService.complete(task.getId());
			System.out.println("Task found: " + task.getName());
		}
	}
	
	@Test
	@Deployment(resources = { "META-INF/bpmn/network-connection-order.spring.bpmn20.xml" })
	public void givenKnownParameterMapWithEntityThenStartingTheProcessThenUniqueIdOfTheProcessMustBeGivenAndIdentityMustBeProvisioned2() {
		String id = processEngineBridge.startProcess("network-order-process", createTestEntity());
		assertNotNull(id);
		Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> userTasks = processEngineBridge.queryForAllUserTasks();
		processEngineBridge.queryForUserTasks("testprincipal");
		CollectionElementUtil.iterate(userTasks, new CollectionElementCallback<org.openinfinity.sso.identityprovisioning.bpmn.Task>() {
			public void callback(org.openinfinity.sso.identityprovisioning.bpmn.Task task) {
				System.out.println("Claiming task: " + task.getId() + " , Task description " + task.getDescription());
				processEngineBridge.claimTask(task.getId());
			}
		});
		CollectionElementUtil.iterate(userTasks, new CollectionElementCallback<org.openinfinity.sso.identityprovisioning.bpmn.Task>() {
			public void callback(org.openinfinity.sso.identityprovisioning.bpmn.Task task) {
				System.out.println("Completing task: " + task.getId() + " , Task description " + task.getDescription());
				processEngineBridge.completeTask(task.getId());
			}
		});
		
		Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> actualTaskSize = processEngineBridge.queryForAllUserTasks();
		assertEquals(0, actualTaskSize.size());
	}
	
	@Test
	@Deployment(resources = { "META-INF/bpmn/network-connection-order.spring.bpmn20.xml" })
	public void givenKnownParameterMapWithKnownUserWhenStartingTheProcessThenTasksMustBeAvailableBasedOnTheProcessThenAfterClaimingOfTasksThereMustBeNoTasksAvailable() {
		String id = processEngineBridge.startProcess("network-order-process", createTestEntity());
		assertNotNull(id);
		Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> userTasks = processEngineBridge.queryForTasksByUserRoles();
		CollectionElementUtil.iterate(userTasks, new CollectionElementCallback<org.openinfinity.sso.identityprovisioning.bpmn.Task>() {
			public void callback(org.openinfinity.sso.identityprovisioning.bpmn.Task task) {
				processEngineBridge.claimTask(task.getId());
			}
		});
		CollectionElementUtil.iterate(userTasks, new CollectionElementCallback<org.openinfinity.sso.identityprovisioning.bpmn.Task>() {
			public void callback(org.openinfinity.sso.identityprovisioning.bpmn.Task task) {
				processEngineBridge.completeTask(task.getId());
			}
		});
		Collection<org.openinfinity.sso.identityprovisioning.bpmn.Task> actualTaskSize = processEngineBridge.queryForAllUserTasks();
		assertEquals(0, actualTaskSize.size());
	}
	
	private Map<String, Object> createTestEntity() {
		NetworkOrder networkOrder = new NetworkOrder();
		networkOrder.setOrderType("4G SIM Card");
		networkOrder.setContractId("1324356");
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("networkOrder", networkOrder);
		return parameterMap;
	}
	
	private void createStubIdentity() {
		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		Identity identity = new Identity();
		identity.setEmail("test@tieto.com");
		identity.setFirstName("testprincipal");
		identity.setLastName("LastName");
		identity.setPhoneNumber("12345");
		identity.setUserPrincipal(new UserPrincipal("testprincipal"));
		Collection<RolePrincipal> roles = new ArrayList<RolePrincipal>();
		roles.add(new RolePrincipal("ROLE_sales"));
		identity.setRolePrincipals(roles);
		identity.setAuthenticated(true);
		securityContext.setAuthentication(identity);
		SecurityContextHolder.setContext(securityContext);
	}
	
}

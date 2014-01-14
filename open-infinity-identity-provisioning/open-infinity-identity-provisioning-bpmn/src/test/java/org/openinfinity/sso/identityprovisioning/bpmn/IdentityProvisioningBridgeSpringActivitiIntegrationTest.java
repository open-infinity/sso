package org.openinfinity.sso.identityprovisioning.bpmn;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openinfinity.core.security.principal.Identity;
import org.openinfinity.core.security.principal.RolePrincipal;
import org.openinfinity.core.security.principal.UserPrincipal;
import org.openinfinity.sso.userprovisioning.bpmn.common.LoggingRouter;
import org.openinfinity.sso.userprovisioning.bpmn.common.NetworkOrder;
import org.openinfinity.sso.userprovisioning.bpmn.common.NetworkOrderIntegrationTestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/META-INF/spring/spring-nodeployment-application-context.xml")
public class IdentityProvisioningBridgeSpringActivitiIntegrationTest extends LoggingRouter {

	@Autowired
	private ProcessEngine processEngine;

	@Autowired
	@Rule
	public ActivitiRule activitiSpringRule;
	
	@Autowired
	ProcessEngineBridge processEngineBridge;
	
	@Autowired
	NetworkOrderIntegrationTestBean networkOrderIntegrationTestBean;
	
	@Before
	public void setUp() {
		createStubIdentity();
	}
	
	@Test
	@Deployment(resources = { "META-INF/bpmn/network-connection-order.spring.bpmn20.xml" })
	public void givenKnownParameterMapWithKnownUserWhenStartingTheProcessThenTasksMustBeAvailableBasedOnTheProcessThenAfterClaimingOfTasksThereMustBeNoTasksAvailable() {
		String id = networkOrderIntegrationTestBean.create(createTestEntity());
		assertNotNull(id);
		Collection<Task> userTasks = networkOrderIntegrationTestBean.loadById("testprincipal");//processEngineBridge.queryForTasksByUserRoles();
		
		IdentityService identityService = processEngine.getIdentityService();
		User user = identityService.createUserQuery().userId("testprincipal").singleResult();
		List<Group> groups = //identityService.createNativeGroupQuery().list();
		identityService.createGroupQuery().groupNameLike("ROLE_sales").list();
		System.out.println("################################### starting");
		for (Group group : groups) {
			System.out.println("###################################"+group.getName());
		}
		Collection<Task> simpleTasks = new ArrayList<Task>();
		assertNotNull(userTasks);
	}
	
	private NetworkOrder createTestEntity() {
		NetworkOrder networkOrder = new NetworkOrder();
		networkOrder.setOrderType("4G SIM Card");
		networkOrder.setContractId("1324356");
		return networkOrder;
	}
	
	private Map<String, Object> createParameterMapWithEntity() {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("networkOrder", createTestEntity());
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
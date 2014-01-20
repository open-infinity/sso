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
import java.util.HashSet;
import java.util.Set;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.openinfinity.core.annotation.Log;
import org.openinfinity.core.annotation.Log.LogLevel;
import org.openinfinity.core.security.principal.Identity;
import org.openinfinity.sso.identityprovisioning.api.IdentityProvisioningBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Identity provisioning Implementation for Activiti BPMN 2.0 (business process and workflow engine).
 * 
 * @author Ilkka Leinonen
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class IdentityProvisioningBridgeSpringActivitiImpl implements IdentityProvisioningBridge {

	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IdentityProvisioningBridgeSpringActivitiImpl.class);
	
	private static final String PASSWORD_PROPERTY_KEY_NAME = "N/A";
	
	public IdentityProvisioningBridgeSpringActivitiImpl(){}
	
	private boolean deleteNotExistingGroupsInMasterData = true;
	
	public boolean isDeleteNotExistingGroupsInMasterData() {
		return deleteNotExistingGroupsInMasterData;
	}

	public void setDeleteNotExistingGroupsInMasterData(boolean deleteNotExistingGroupsInMasterData) {
		this.deleteNotExistingGroupsInMasterData = deleteNotExistingGroupsInMasterData;
	}

	@Autowired
	private ProcessEngine processEngine;

	@Log(level=LogLevel.INFO)
	@Transactional
	public void provision(Identity identity) {
		LOGGER.debug("Starting provisioning of user information to Activiti BPMN 2.0 engine.");
		String username = identity.getName();
		LOGGER.info("Provisioning of user data for [" + username + "]");
		IdentityService identityService = processEngine.getIdentityService();
		executeRoleProvisioning(identityService, username);
		executeUserProvisioning(identity, username, identityService);
	}

	private void executeRoleProvisioning(IdentityService identityService, String userId) {
		org.springframework.security.core.context.SecurityContext securityContext = SecurityContextHolder.getContext();
		Collection<? extends GrantedAuthority> grantedAuthorities = securityContext.getAuthentication().getAuthorities();
		Set<String> existiningGroupsFromMasterData = new HashSet<String>();
		addGrantedAuthoritiesAsRolesAndCreateMembershipWithUserAndGroup(identityService, userId, grantedAuthorities, existiningGroupsFromMasterData);
		if (isDeleteNotExistingGroupsInMasterData())
			invalideMembershipAndRemoveNonExistingRoles(identityService, userId, existiningGroupsFromMasterData);
	}

	private void invalideMembershipAndRemoveNonExistingRoles(IdentityService identityService, String userId, Set<String> existiningGroupsFromMasterData) {
		Collection<Group> groups = identityService.createGroupQuery().groupMember(userId).list();
		for (Group group : groups) {
			if (!existiningGroupsFromMasterData.contains(group.getName())) {
				identityService.deleteGroup(group.getId());
				identityService.deleteMembership(userId, group.getId());
				LOGGER.debug("Invalidated group [" + group.getId() + "]");
			}
		}
	}

	private void addGrantedAuthoritiesAsRolesAndCreateMembershipWithUserAndGroup(IdentityService identityService, String userId, Collection<? extends GrantedAuthority> grantedAuthorities, Set<String> existiningGroupsFromMasterData) {
		for (GrantedAuthority grantedAuthority : grantedAuthorities) {
			Long countForGrantedAuthority = identityService.createGroupQuery().groupId(grantedAuthority.getAuthority()).count();
			if (countForGrantedAuthority == 0) {
				LOGGER.debug("Provisioning of new role started [" + grantedAuthority.getAuthority() + "]");
				long startTime = System.currentTimeMillis();
				Group activitiGroup = identityService.newGroup(grantedAuthority.getAuthority());
				identityService.saveGroup(activitiGroup);
				identityService.createMembership(userId, activitiGroup.getId());
				existiningGroupsFromMasterData.add(activitiGroup.getName());
				LOGGER.debug("Role provisioning and membership creation finalized in " + (System.currentTimeMillis()-startTime) + " ms to Activiti BPMN 2.0 engine.");
			}
		}
	}

	private void executeUserProvisioning(Identity identity, String userId, IdentityService identityService) {
		Long countForUser = identityService.createUserQuery().userId(userId).count();
		if (countForUser == 0) {
			String email = identity.getEmail();
			String firstName = identity.getFirstName();
			String lastName = identity.getLastName();
			User activitiUser = identityService.newUser(identity.getName());
			activitiUser.setPassword(PASSWORD_PROPERTY_KEY_NAME);
			activitiUser.setFirstName(firstName);
			activitiUser.setLastName(lastName);
			activitiUser.setEmail(email);
			LOGGER.debug("Provisining of new user started [" + activitiUser + "].");
			long startTime = System.currentTimeMillis();
			identityService.saveUser(activitiUser);
			LOGGER.debug("User provisioning finalized in " + (System.currentTimeMillis()-startTime) + " ms to Activiti BPMN 2.0 engine.");
		} else {
			LOGGER.debug("Provisining of new user finalized, user already existed [" + userId + "].");	
		}
	}
	
}
package org.openinfinity.sso.springsecurity.liferay;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;

class RoleOperations {

    private LiferayRequest liferayRequest;


    public RoleOperations(LiferayRequest liferayRequest) {
        this.liferayRequest = liferayRequest;
    }


    long[] groupIDs() {
        long[] genericRoleIDs = new GenericRoleOperations(liferayRequest).groupIDs();
        long[] organizationRoleIDs = new OrganizationRoleOperations(liferayRequest).groupIDs();
        long[] roleIDs = new long[genericRoleIDs.length + organizationRoleIDs.length];
        System.arraycopy(genericRoleIDs, 0, roleIDs, 0, genericRoleIDs.length);
        System.arraycopy(organizationRoleIDs, 0, roleIDs, genericRoleIDs.length,organizationRoleIDs.length);
        return roleIDs;
    }

    public void createNonExistentGroupsFor(User user)
            throws SystemException, PortalException {
        new GenericRoleOperations(liferayRequest)
                .createNonExistentGroupsFor(user);
        new OrganizationRoleOperations(liferayRequest)
                .createNonExistentGroupsFor(user);
    }
}

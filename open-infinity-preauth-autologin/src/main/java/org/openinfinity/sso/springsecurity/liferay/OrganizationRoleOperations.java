package org.openinfinity.sso.springsecurity.liferay;

import com.liferay.portal.NoSuchRoleException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.OrganizationLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserGroupRoleLocalServiceUtil;

import org.openinfinity.sso.security.util.PropertiesUtil;
import org.openinfinity.sso.springsecurity.liferay.PreauthVariables;

import java.util.Locale;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.openinfinity.sso.springsecurity.liferay.LiferayRequest.WHAT_LIFERAY_CONSIDERS_MISSING_LONG;
import static org.openinfinity.sso.springsecurity.liferay.LiferayRequest.WHAT_LIFERAY_CONSIDERS_MISSING_STRING;

class OrganizationRoleOperations extends MetaGroupOperations {

    private static final String ORGANIZATION_ROLE_PATTERN = PropertiesUtil.loadValue(PreauthVariables.PATTERN_ROLES_ORGANIZATION),
            NO_COMMENTS = WHAT_LIFERAY_CONSIDERS_MISSING_STRING;

    private static final Map<Locale, String> EMPTY_TITLE_MAP = emptyMap(),
            EMPTY_DESCRIPTION_MAP = emptyMap();


    OrganizationRoleOperations(LiferayRequest liferayRequest) {
        super(liferayRequest, ORGANIZATION_ROLE_PATTERN,
                new GenericRoleOperations(liferayRequest));
    }


    @Override
    long groupIDByName(String name)
            throws SystemException, PortalException {
        try {
            return RoleLocalServiceUtil.getService()
                    .getRole(liferayRequest().companyID(), name)
                    .getRoleId();
        } catch (NoSuchRoleException nsre) {
            return WHAT_LIFERAY_CONSIDERS_MISSING_LONG;
        }
    }

    @Override
    long createNewGroupForUser(Group group, User user)
            throws SystemException, PortalException {

        long roleID = RoleLocalServiceUtil.addRole(user.getUserId(),
                liferayRequest().companyID(),
                group.name(), EMPTY_TITLE_MAP, EMPTY_DESCRIPTION_MAP,
                RoleConstants.TYPE_ORGANIZATION).getRoleId();
        MetaGroup metaGroup = (MetaGroup) group;
        long organizationID = OrganizationLocalServiceUtil.getOrganization(
                liferayRequest().companyID(), metaGroup.groupName())
                .getOrganizationId();
        associate(roleID, organizationID, user);

        return roleID;
    }


    private void associate(long roleID, long organizationID, User user)
            throws SystemException, PortalException {
        long groupID = GroupLocalServiceUtil.getOrganizationGroup(
                user.getCompanyId(), organizationID).getGroupId();
        UserGroupRoleLocalServiceUtil.addUserGroupRoles(user.getUserId(),
                groupID, new long[]{roleID});
    }
}

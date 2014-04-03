package org.openinfinity.sso.springsecurity.liferay;

import com.liferay.portal.NoSuchUserGroupException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserGroupLocalServiceUtil;

import org.openinfinity.sso.security.util.PropertiesUtil;
import org.openinfinity.sso.springsecurity.liferay.PreauthVariables;



import static org.openinfinity.sso.springsecurity.liferay.LiferayRequest.WHAT_LIFERAY_CONSIDERS_MISSING_LONG;
import static org.openinfinity.sso.springsecurity.liferay.LiferayRequest.WHAT_LIFERAY_CONSIDERS_MISSING_STRING;

class UserGroupOperations extends GroupOperations {

    private static final String USER_GROUP_PATTERN = PropertiesUtil.loadValue(PreauthVariables.PATTERN_USERGROUPS);


    UserGroupOperations(LiferayRequest liferayRequest) {
        super(liferayRequest, USER_GROUP_PATTERN);
    }


    @Override
    long groupIDByName(String name)
            throws SystemException, PortalException {
        try {
            return UserGroupLocalServiceUtil.getService()
                    .getUserGroup(liferayRequest().companyID(), name)
                    .getUserGroupId();
        } catch (NoSuchUserGroupException nsuge) {
            return WHAT_LIFERAY_CONSIDERS_MISSING_LONG;
        }
    }

    @Override
    long createNewGroupForUser(Group group, User user)
            throws SystemException, PortalException {
    	    	
        return UserGroupLocalServiceUtil.addUserGroup(user.getUserId(),
                liferayRequest().companyID(), group.name(),
                WHAT_LIFERAY_CONSIDERS_MISSING_STRING).getUserGroupId();
    }
}

package org.openinfinity.sso.springsecurity.liferay;

import com.liferay.portal.NoSuchRoleException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.service.RoleLocalServiceUtil;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.openinfinity.sso.security.util.PropertiesUtil;

import static java.util.Collections.emptyMap;
import static org.openinfinity.sso.springsecurity.liferay.LiferayRequest.WHAT_LIFERAY_CONSIDERS_MISSING_LONG;
import static org.openinfinity.sso.springsecurity.liferay.LiferayRequest.WHAT_LIFERAY_CONSIDERS_MISSING_STRING;

class GenericRoleOperations extends GroupOperations {

    private static final String ROLE_PATTERN = 
    		PropertiesUtil.loadValue(PreauthVariables.PATTERN_ROLES_GENERAL),
            NO_COMMENTS = WHAT_LIFERAY_CONSIDERS_MISSING_STRING;

    private static final Map<Locale, String> EMPTY_TITLE_MAP = emptyMap(),
            EMPTY_COMMENT_MAP = emptyMap();


    GenericRoleOperations(LiferayRequest liferayRequest) {
        super(liferayRequest, ROLE_PATTERN);
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
        return RoleLocalServiceUtil.addRole(user.getUserId(),
                liferayRequest().companyID(),
                group.name(), EMPTY_TITLE_MAP, EMPTY_COMMENT_MAP,
                RoleConstants.TYPE_REGULAR).getRoleId();
    }
}

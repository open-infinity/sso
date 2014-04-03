package org.openinfinity.sso.springsecurity.liferay;

import org.openinfinity.sso.security.util.PropertiesUtil;
import org.openinfinity.sso.springsecurity.liferay.PreauthVariables;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.OrganizationLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;

import static com.liferay.portal.model.OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID;
import static com.liferay.portal.model.OrganizationConstants.TYPE_REGULAR_ORGANIZATION;
import static org.openinfinity.sso.springsecurity.liferay.LiferayRequest.WHAT_LIFERAY_CONSIDERS_MISSING_LONG;
import static org.openinfinity.sso.springsecurity.liferay.LiferayRequest.WHAT_LIFERAY_CONSIDERS_MISSING_STRING;

class OrganizationOperations extends GroupOperations {

    private static final String ORGANIZATION_PATTERN = 
    		PropertiesUtil.loadValue(PreauthVariables.PATTERN_ORGANIZATIONS),
            NO_COMMENTS = WHAT_LIFERAY_CONSIDERS_MISSING_STRING;

    private static final Boolean NON_RECURSABLE = false,
            ASSOCIATED_WITH_MAIN_SITE = true;

    private static final Long NO_REGION =
            WHAT_LIFERAY_CONSIDERS_MISSING_LONG,
            NO_COUNTRY = WHAT_LIFERAY_CONSIDERS_MISSING_LONG;

    private static final Integer FULL_MEMBER_ORGANIZATION_STATUS = 12017;


    OrganizationOperations(LiferayRequest liferayRequest) {
        super(liferayRequest, ORGANIZATION_PATTERN);
    }


    @Override
    long groupIDByName(String name)
            throws SystemException, PortalException {
        return OrganizationLocalServiceUtil.getService()
                .getOrganizationId(liferayRequest().companyID(), name);
    }

    // TODO parent organizations other than root, irregular orgs
    @Override
    long createNewGroupForUser(Group group, User user)
            throws SystemException, PortalException {
    	return OrganizationLocalServiceUtil.addOrganization(
                user.getUserId(), DEFAULT_PARENT_ORGANIZATION_ID, group.name(),
                TYPE_REGULAR_ORGANIZATION, NON_RECURSABLE, NO_REGION,
                NO_COUNTRY, FULL_MEMBER_ORGANIZATION_STATUS, NO_COMMENTS,
                ASSOCIATED_WITH_MAIN_SITE,
                new ServiceContext()).getOrganizationId();    	
    }
}

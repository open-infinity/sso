/*
 * Copyright (c) 2012 the original author or authors.
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
package org.openinfinity.sso.springsecurity.liferay;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.*;
import com.liferay.portal.security.auth.AutoLoginException;
import com.liferay.portal.service.*;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PrefsPropsUtil;


import org.openinfinity.core.security.principal.Identity;
import org.openinfinity.sso.security.context.grid.IdentityContext;
import org.openinfinity.sso.security.util.GlobalVariables;
import org.openinfinity.sso.security.util.PropertiesUtil;

import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>A wrapper class that encapsulates some Liferay property value parsing
 * logic.</p>
 * <p/>
 * <p>The user can have the following properties in the
 * <code>portal-ext.properties</code> file:
 * <ul>
 * <li><code>user.principal.property.screenname</code></li>
 * <li><code>user.principal.property.emailaddress</code></li>
 * <li><code>user.principal.property.facebookid</code></li>
 * <li><code>user.principal.property.openid</code></li>
 * <li><code>user.principal.property.locale</code></li>
 * <li><code>user.principal.property.firstname</code></li>
 * <li><code>user.principal.property.middlename</code></li>
 * <li><code>user.principal.property.lastname</code></li>
 * <li><code>user.principal.property.prefix</code></li>
 * <li><code>user.principal.property.suffix</code></li>
 * <li><code>user.principal.property.sex</code></li>
 * <li><code>user.principal.property.birthdate</code></li>
 * <li><code>user.principal.property.birthdate.pattern</code></li>
 * <li><code>user.principal.property.jobtitle</code></li>
 * </ul>
 * </p>
 * <p>Screen name and e-mail address are
 * obligatory. The other properties are optional. The birthdate pattern
 * property value is a <code>SimpleDateFormat</code> date string pattern that is
 * used to parse the birthdate property value to a Date, the default being
 * M.d.YYYY. All the other property values are names for SAML user attributes
 * that hold the actual value used in creating the Liferay user.</p>
 * <p/>
 * <p>The locale user attribute value should equal a legal Java locale String
 * representation. The prefix and suffix values should equal integer values
 * that correspond to the String representations - see Liferay's
 * <code>portal-data-common.sql</code>. The sex attribute should equal
 * M or F.</p>
 *
 * @author Mika Salminen
 * @author Ilkka Leinonen
 * @since 1.0.2
 */
class LiferayRequest {

    private static final Log LOG = LogFactoryUtil.getLog(LiferayRequest.class);

    static final String WHAT_LIFERAY_CONSIDERS_MISSING_STRING =
            StringPool.BLANK;


    private static String SCREEN_NAME_PROPERTY_KEY_NAME = null;
    private static String E_MAIL_ADDRESS_PROPERTY_KEY_NAME = null;
    private static String FACEBOOK_ID_PROPERTY_KEY_NAME = null;
    private static String OPEN_ID_PROPERTY_KEY_NAME = null;
    private static String LOCALE_PROPERTY_KEY_NAME = null;
    private static String FIRST_NAME_PROPERTY_KEY_NAME = null;
    private static String MIDDLE_NAME_PROPERTY_KEY_NAME = null;
    private static String LAST_NAME_PROPERTY_KEY_NAME = null;
    private static String PREFIX_PROPERTY_KEY_NAME = null;
    private static String SUFFIX_PROPERTY_KEY_NAME = null;
    private static String SEX_PROPERTY_KEY_NAME = null;
    private static String BIRTH_DATE_PROPERTY_KEY_NAME = null;
    private static String JOB_TITLE_PROPERTY_KEY_NAME = null;
    //static final String WHAT_LIFERAY_CONSIDERS_MISSING_STRING = null;
    
    private static String[] USER_ATTRIBUTES;
    
    static {
	   SCREEN_NAME_PROPERTY_KEY_NAME = PropertiesUtil.loadValue(PreauthVariables.SCREEN_NAME);
	   E_MAIL_ADDRESS_PROPERTY_KEY_NAME = PropertiesUtil.loadValue(PreauthVariables.EMAIL_ADDRESS);
	   FACEBOOK_ID_PROPERTY_KEY_NAME = PropertiesUtil.loadValue(PreauthVariables.FACEBOOK_ID);
	   OPEN_ID_PROPERTY_KEY_NAME = PropertiesUtil.loadValue(PreauthVariables.OPEN_ID);
	   LOCALE_PROPERTY_KEY_NAME = PropertiesUtil.loadValue(PreauthVariables.LOCALE);
	   FIRST_NAME_PROPERTY_KEY_NAME = PropertiesUtil.loadValue(PreauthVariables.FIRST_NAME);
	   MIDDLE_NAME_PROPERTY_KEY_NAME = PropertiesUtil.loadValue(PreauthVariables.MIDDLE_NAME);
	   LAST_NAME_PROPERTY_KEY_NAME = PropertiesUtil.loadValue(PreauthVariables.LAST_NAME);
	   SEX_PROPERTY_KEY_NAME = PropertiesUtil.loadValue(PreauthVariables.SEX);
	   USER_ATTRIBUTES = PropertiesUtil.loadValue(
		   GlobalVariables.ATTRIBUTE_BASED_USER_ATTRIBUTES).split(PropertiesUtil.loadValue(
			   GlobalVariables.ATTRIBUTE_BASED_USER_ATTRIBUTE_DELIMITER));
    }
    
    private static final String BIRTH_DATE_PATTERN_PROPERTY_KEY_NAME =
            "birthdate.pattern";
    private static final String DEFAULT_BIRTH_DATE_PATTERN = "d.M.yyyy";

    private static final Integer
            WHAT_LIFERAY_CONSIDERS_MISSING_BIRTH_MONTH = Calendar.JANUARY,
            WHAT_LIFERAY_CONSIDERS_MISSING_BIRTHDAY = 1,
            WHAT_LIFERAY_CONSIDERS_MISSING_BIRTH_YEAR = 1970;

    private static final long NO_CREATOR_USER = 0;

    private static final boolean NO_AUTO_PASSWORD = false,
            NOT_AUTO_SCREEN_NAME = false, DONT_SEND_EMAIL = false;

    private static final String WHATEVER_PASSWORD =
            String.valueOf(System.currentTimeMillis()),
            WHATEVER_SECOND_PASSWORD = WHATEVER_PASSWORD;

    private static final List<UserGroupRole> NO_CHANGE_IN_USER_GROUP_ROLES =
            null;

    static final Long WHAT_LIFERAY_CONSIDERS_MISSING_LONG = 0L;

    static final long[] NO_GROUPS = new long[0];

    private HttpServletRequest request;

    private Long companyID;

    private OrganizationOperations organizations;

    private RoleOperations roles;

    private UserGroupOperations userGroups;
    
    private Identity identity;



    LiferayRequest(HttpServletRequest request) throws AutoLoginException {
        this.request = request;
        this.identity=loadIdentity();
        
        for (Map.Entry<String, String> userPropertyEntry :
                userProperties().entrySet()) {
            LOG.debug(userPropertyEntry.getKey() + ": "
                    + userPropertyEntry.getValue());
        }
        
        //validateNonNull(request.getUserPrincipal());
        validateNonNull(loadIdentity());
        
        organizations = new OrganizationOperations(this);        
        roles = new RoleOperations(this);
        userGroups = new UserGroupOperations(this);  
    }


    public String screenName() {
        return stringValueFor(SCREEN_NAME_PROPERTY_KEY_NAME);
    }

    public String eMailAddress() {
        return stringValueFor(E_MAIL_ADDRESS_PROPERTY_KEY_NAME);
    }

    public long facebookID() {
        return longValueFor(FACEBOOK_ID_PROPERTY_KEY_NAME);
    }

    public String openID() {
        return stringValueFor(OPEN_ID_PROPERTY_KEY_NAME);
    }

    public Locale locale() {
        String localeString = stringValueFor(LOCALE_PROPERTY_KEY_NAME);
        if (localeString == null) {
            return Locale.getDefault();
        } else {
            return new Locale(localeString);
        }
    }

    public String firstName() {
        return stringValueFor(FIRST_NAME_PROPERTY_KEY_NAME);
    }

    public String middleName() {
        return stringValueFor(MIDDLE_NAME_PROPERTY_KEY_NAME);
    }

    public String lastName() {
        return stringValueFor(LAST_NAME_PROPERTY_KEY_NAME);
    }

    public Integer prefixID() {
        return intValueFor(PREFIX_PROPERTY_KEY_NAME);
    }

    public Integer suffixID() {
        return intValueFor(SUFFIX_PROPERTY_KEY_NAME);
    }

    public boolean isMale() {
        String sex = stringValueFor(SEX_PROPERTY_KEY_NAME);
        return "M".equals(sex);
    }

    public int birthMonth() {
        return birthDateValue(Calendar.MONTH);
    }

    public int birthday() {
        return birthDateValue(Calendar.DAY_OF_MONTH);
    }

    public int birthYear() {
        return birthDateValue(Calendar.YEAR);
    }

    public String jobTitle() {
        return stringValueFor(JOB_TITLE_PROPERTY_KEY_NAME);
    }

    public long companyID() {
        if (companyID == null) {
            companyID = companyIDFromLiferayByNameInRequest();
        }
        if (companyID == null) {
            throw new IllegalArgumentException("Company ID missing");
        }
        return companyID;
    }

    public long[] groupIDs() {
        return NO_GROUPS;
    }

    public long[] organizationIDs() {
        return organizations.groupIDs();
    }

    public long[] roleIDs() {
        return roles.groupIDs();
    }

    public long[] userGroupIDs() {
        return userGroups.groupIDs();
    }

    public Principal userPrincipal() {
        return identity.getUserPrincipal();
    }

    public User createUser() throws SystemException, PortalException {
        User createdUser = UserLocalServiceUtil.addUser(
                NO_CREATOR_USER, companyID(), NO_AUTO_PASSWORD,
                WHATEVER_PASSWORD, WHATEVER_SECOND_PASSWORD,
                NOT_AUTO_SCREEN_NAME, screenName(), eMailAddress(),
                facebookID(), openID(), locale(), firstName(), middleName(),
                lastName(), prefixID(), suffixID(), isMale(), birthMonth(),
                birthday(), birthYear(), jobTitle(), groupIDs(),
                organizationIDs(), roleIDs(), userGroupIDs(), DONT_SEND_EMAIL,
                new ServiceContext());
        createNewMembershipsIfNeededFor(createdUser);
        return createdUser;
    }

    /**
     * E-mail, facebook ID, open ID, locale, first, middle and last names,
     * prefix and suffix IDs, sex and birthdate, job title, organizationIDs,
     * roleIDs, and user groupIDs are expected to be changed from global user
     * data.
     * <p/>
     * TODO make more parameters possible to be changed from global user data
     * TODO some kind of strategy interface for customization
     *
     * @param oldUser old user
     * @throws SystemException
     * @throws PortalException
     */
    public void mergeTo(User oldUser) throws SystemException, PortalException {
        createNewMembershipsIfNeededFor(oldUser);
        Contact contact =
                ContactLocalServiceUtil.getContact(oldUser.getContactId());
        UserLocalServiceUtil.updateUser(oldUser.getUserId(),
                oldUser.getPassword(), oldUser.getPassword(),
                oldUser.getPassword(), oldUser.getPasswordReset(),
                oldUser.getReminderQueryQuestion(),
                oldUser.getReminderQueryAnswer(),
                oldUser.getScreenName(), eMailAddress(), facebookID(), openID(),
                locale().toString(), oldUser.getTimeZone().getID(),
                oldUser.getGreeting(), oldUser.getComments(), firstName(),
                middleName(), lastName(), prefixID(), suffixID(), isMale(),
                birthMonth(), birthday(), birthYear(),
                contact.getSmsSn(), contact.getAimSn(), contact.getFacebookSn(),
                contact.getIcqSn(), contact.getJabberSn(), contact.getMsnSn(),
                contact.getMySpaceSn(), contact.getSkypeSn(),
                contact.getTwitterSn(), contact.getYmSn(),
                jobTitle(), oldUser.getGroupIds(), organizationIDs(), roleIDs(),
                NO_CHANGE_IN_USER_GROUP_ROLES,
                userGroupIDs(), new ServiceContext());
    }

    public Identity loadIdentity() {
		String sessionId = (String) request.getAttribute("Shib-Session-ID");
		Identity identity = IdentityContext.loadIdentity(sessionId);
    	return identity;
    }

    private void createNewMembershipsIfNeededFor(User user)
            throws SystemException, PortalException {
        organizations.createNonExistentGroupsFor(user);
        roles.createNonExistentGroupsFor(user);
        userGroups.createNonExistentGroupsFor(user);
    }

    private Integer birthDateValue(Integer calendarValue) {
        switch (calendarValue) {
            case Calendar.MONTH:
                return birthDateOrMissingValueWithPossibleOffset(
                        WHAT_LIFERAY_CONSIDERS_MISSING_BIRTH_MONTH,
                        calendarValue, 1);
            case Calendar.YEAR:
                return birthDateOrMissingValueWithPossibleOffset(
                        WHAT_LIFERAY_CONSIDERS_MISSING_BIRTH_YEAR,
                        calendarValue);
            case Calendar.DAY_OF_MONTH:
                return birthDateOrMissingValueWithPossibleOffset(
                        WHAT_LIFERAY_CONSIDERS_MISSING_BIRTHDAY,
                        calendarValue);
            default:
                throw new IllegalArgumentException(
                        "Illegal calendar value " + calendarValue);
        }
    }

    private int birthDateOrMissingValueWithPossibleOffset(
            Integer missingValue, Integer calendarValue,
            Integer... offset) {
        String birthDateString =
                stringValueFor(BIRTH_DATE_PROPERTY_KEY_NAME);
        if (missing(birthDateString)) {
            return missingValue;
        }
        try {
            Calendar calendar =
                    dateForPatternedString(
                            birthDateString, birthDatePattern());
            Integer value = calendar.get(calendarValue);
            value = applyOffsetToValue(value, offset);
            return value;
        } catch (ParseException e) {
            return WHAT_LIFERAY_CONSIDERS_MISSING_BIRTH_MONTH;
        }
    }

    private Integer applyOffsetToValue(Integer value, Integer[] offset) {
        if (offset != null && offset[0] != null) {
            value += offset[0];
        }
        return value;
    }

    private Calendar dateForPatternedString(String birthDateString,
                                            String pattern)
            throws ParseException {
        Date birthDate = new SimpleDateFormat(pattern)
                .parse(birthDateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(birthDate);
        return calendar;
    }

    private String birthDatePattern() {
        String pattern = liferayProperty(
                BIRTH_DATE_PATTERN_PROPERTY_KEY_NAME);
        if (pattern == null) {
            pattern = DEFAULT_BIRTH_DATE_PATTERN;
        }
        return pattern;
    }

    private String userPropertyKeyForLiferayKey(String key) {
        return liferayProperty(key);
    }

    private String liferayProperty(String key) {
        try {
            return PrefsPropsUtil
                    .getString(companyID(), key);
        } catch (Exception e) {
            LOG.error(e);
            return null;
        }
    }

    private String stringValueFor(String key) {
        String value = userProperties().get(key);
        //LOG.debug("Liferay key " + key + " mapped in user properties to "+ userPropertyKey + " has value " + value);
        if (value == null) {
            return WHAT_LIFERAY_CONSIDERS_MISSING_STRING;
        } else {
            return value;
        }
    }

    private Long longValueFor(String key) {
        String stringValue = stringValueFor(key);
        if (!missing(stringValue)) {
            return Long.valueOf(stringValue);
        } else {
            return WHAT_LIFERAY_CONSIDERS_MISSING_LONG;
        }
    }

    private Integer intValueFor(String key) {
        return longValueFor(key).intValue();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> userProperties() {
		String sessionId = (String) request.getAttribute("Shib-Session-ID");
		
		Identity identity = IdentityContext.loadIdentity(sessionId);
                
		Map<String, String> attributes = identity.getUserAttributes();
		return attributes;

    }

    
    private Long companyIDFromLiferayByNameInRequest() {
        return PortalUtil.getCompanyId(request);
    }

    private Boolean missing(String string) {
        return string == null || string.isEmpty();
    }

    private void validateNonNull(Principal userPrincipal)
            throws AutoLoginException {
        if (userPrincipal == null) {
            throw new AutoLoginException("JAAS User Principal is null");
        }
    }
}

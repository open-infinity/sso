package org.openinfinity.sso.springsecurity.liferay;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptySet;
import static org.openinfinity.sso.springsecurity.liferay.LiferayRequest.NO_GROUPS;
import static org.openinfinity.sso.springsecurity.liferay.LiferayRequest.WHAT_LIFERAY_CONSIDERS_MISSING_LONG;

abstract class GroupOperations {

    private String pattern;

    private int groupIndex;

    private LiferayRequest liferayRequest;


    GroupOperations(LiferayRequest liferayRequest, String pattern) {
        this(liferayRequest, pattern, 1);
    }

    GroupOperations(LiferayRequest liferayRequest, String pattern,
                    int groupIndex) {
        this.liferayRequest = liferayRequest;
        this.pattern = pattern;
        this.groupIndex = groupIndex;
    }


    LiferayRequest liferayRequest() {
        return liferayRequest;
    }


    void createNonExistentGroupsFor(User user)
            throws SystemException, PortalException {
        for (Group nonExistentGroup : nonExistentGroups()) {
            createNewGroupForUser(nonExistentGroup, user);
        }
    }

    long[] groupIDs() {
    	
        List<Long> groupIDs = new ArrayList<Long>();
        try {
            Set<Group> groups = groupsMatching(pattern);
            for (Group group : groups) {
                long groupID = groupIDByName(group.name);
                if (existing(groupID)) {
                    groupIDs.add(groupID);
                }
            }
        } catch (SystemException e) {
            return NO_GROUPS;
        } catch (PortalException e) {
            return NO_GROUPS;
        }
        return longArrayFrom(groupIDs);
    }


    abstract long groupIDByName(String name)
            throws SystemException, PortalException;

    abstract long createNewGroupForUser(Group group, User user)
            throws SystemException, PortalException;


    private boolean existing(long groupID) {
        return groupID > WHAT_LIFERAY_CONSIDERS_MISSING_LONG;
    }

    private boolean no(long groupID) {
        return groupID == WHAT_LIFERAY_CONSIDERS_MISSING_LONG;
    }

    private Set<? extends Group> nonExistentGroups() {
        Set<Group> nonExistentGroups = new HashSet<Group>();
        try {
            Set<Group> groups = groupsMatching(pattern);
            for (Group group : groups) {
                if (no(groupIDByName(group.name))) {
                    nonExistentGroups.add(group);
                }
            }
            return nonExistentGroups;
        } catch (PortalException e) {
            return emptySet();
        } catch (SystemException e) {
            return emptySet();
        }
    }

    private Set<Group> groupsMatching(String patternString) {

        //Collection<? extends GrantedAuthority> grantedAuthorities =
        //        ((Authentication) liferayRequest.userPrincipal())
        //                .getAuthorities();

		Collection<? extends GrantedAuthority> grantedAuthorities = liferayRequest.loadIdentity().getAuthorities();
	
        Set<Group> groupsMatchingPattern = new HashSet<Group>();
        
        Pattern pattern = Pattern.compile(patternString);
        
        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            Matcher matcher = pattern.matcher(
                    grantedAuthority.getAuthority());
            if (matcher.matches()) {
                if (this instanceof MetaGroupOperations) {
                    groupsMatchingPattern.add(
                            new MetaGroup(matcher.group(groupIndex),
                                    matcher.group(groupIndex - 1)));
                } else {
                    groupsMatchingPattern.add(                            
                    		new Group(matcher.group(groupIndex)));                    
                }
            }
        }
        return groupsMatchingPattern;
    }

    private long[] longArrayFrom(List<Long> longList) {
        long[] longArray = new long[longList.size()];
        for (int idx = 0; idx < longArray.length; idx++) {
            longArray[idx] = longList.get(idx);
        }
        return longArray;
    }


    static class Group {

        private final String name;


        Group(String name) {
            this.name = name;
        }


        String name() {
            return name;
        }
    }

    static class MetaGroup extends Group {

        private final String groupName;


        MetaGroup(String name, String groupName) {
            super(name);
            this.groupName = groupName;
        }


        String groupName() {
            return groupName;
        }
    }
}

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

import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.AutoLoginException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.Principal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@code PreAuthenticatedAutoLogin}.
 *
 * @author Mika Salminen
 * @author Ilkka Leinonen
 * @todo tests (Liferay static class usage makes this hard)
 * @since 1.0.0
 */
public class PreAuthenticatedAutoLoginTest {

    private PreAuthenticatedAutoLogin sut;

    private HttpServletRequest mockHTTPServletRequest;

    private HttpServletResponse mockHTTPServletResponse;

    private Principal mockPrincipal;

    private User mockUser;


    @Before
    public void setUp() {
        sut = new PreAuthenticatedAutoLogin();
        mockHTTPServletRequest = mock(HttpServletRequest.class);
        mockHTTPServletResponse = mock(HttpServletResponse.class);
        mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("frank");
        mockUser = mock(User.class);
    }


    @Test(expected = AutoLoginException.class)
    public void nullPrincipalInRequestResultsInAutoLoginException()
            throws AutoLoginException {
        sut.login(mockHTTPServletRequest, mockHTTPServletResponse);
    }

    @Test
    @Ignore
    public void liferayUserIsObtainedFromUserLocalServiceUtilWithPrincipalUserNameAndPortalUtilResolvedCompanyIDInRequest() {

    }

    @Test
    @Ignore
    public void redirectRequestAttributeIsSetIfNeeded() {

    }

    @Test
    @Ignore
    public void
    successfulLoginReturnsArrayOfThreeContainingUserIDPasswordAndBooleanFalse() {

    }
}

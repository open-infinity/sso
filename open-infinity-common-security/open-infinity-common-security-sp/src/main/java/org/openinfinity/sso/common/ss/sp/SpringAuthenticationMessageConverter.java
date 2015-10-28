/*
 * Copyright (c) 2012-2015 the original author or authors.
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
package org.openinfinity.sso.common.ss.sp;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * An HTTP message converter that reads a plain text OpenAM REST interface user property
 * query result as an input and produces a Spring Authentication object from it.
 *
 * @author Mika Salminen
 * @author Ilkka Leinonen
 * @since 1.0.0
 * @version 1.1.0
 */
public class SpringAuthenticationMessageConverter
        extends AbstractHttpMessageConverter<Authentication> {

    private static final String SSO_TOKEN_ID_KEY =
            "urn:oasis:names:tc:SAML:2.0:profiles:session:sessionId";


    private HttpMessageConverter<String> stringHttpMessageConverter;


    public SpringAuthenticationMessageConverter() {
        stringHttpMessageConverter = new StringHttpMessageConverter();
        setSupportedMediaTypes(Arrays.asList(MediaType.TEXT_PLAIN));
    }


    void setStringHttpMessageConverter(
            HttpMessageConverter<String> stringHttpMessageConverter) {
        this.stringHttpMessageConverter = stringHttpMessageConverter;
    }


    @Override
    protected boolean supports(Class<?> clazz) {
        return Authentication.class.equals(clazz);
    }

    @Override
    protected Authentication readInternal(Class<? extends Authentication> clazz,
                                          HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        String resultString =
                stringHttpMessageConverter.read(String.class, inputMessage);
        Scanner resultScanner = new Scanner(resultString);
        Map<String, String> properties = new HashMap<String, String>();
        Collection<GrantedAuthority> authorities =
                new HashSet<GrantedAuthority>();
        String name = null, attributeName = null;
        boolean collectingAttributeValues = false;
        while (resultScanner.hasNextLine()) {
            String resultLine = resultScanner.nextLine();
            if (collectingAttributeValues &&
                    !resultLine.startsWith("userdetails.attribute.value")) {
                collectingAttributeValues = false;
            }
            if (resultLine.startsWith("userdetails.token.id")) {
                properties.put(SSO_TOKEN_ID_KEY, valueFrom(resultLine));
            } else if (resultLine.startsWith("userdetails.attribute.name")) {
                attributeName = valueFrom(resultLine);
                collectingAttributeValues = true;
            } else if (collectingAttributeValues &&
                    resultLine.startsWith("userdetails.attribute.value")) {
                final String value = valueFrom(resultLine);
                if (attributeName.equals("memberof")) {
                    authorities.add(new GrantedAuthority() {

						@Override
						public String getAuthority() {
							return value;
						}
                    	
                    });
                } else {
                    if (attributeName.equals("screenname")) {
                        name = value;
                    }
                    properties.put(attributeName, value);
                }
            }
        }

        return new PreAuthenticatedAuthenticationToken(
                new User(name, properties), properties.get(SSO_TOKEN_ID_KEY),
                authorities);
    }

    @Override
    protected void writeInternal(
            Authentication authentication, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        // not supported
    }


    private String valueFrom(String line) {
        Scanner resultLineScanner = new Scanner(line).useDelimiter("=");
        resultLineScanner.next();
        return resultLineScanner.next();
    }
}

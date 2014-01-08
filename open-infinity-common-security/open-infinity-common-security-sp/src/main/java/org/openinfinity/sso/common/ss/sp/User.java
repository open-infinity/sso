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
package org.openinfinity.sso.common.ss.sp;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A JAAS User principal that has user properties.
 *
 * @author Mika Salminen
 * @author Ilkka Leinonen
 * @since 1.0.0
 */
public final class User implements Principal, Serializable {

    private final String name;

    private final Map<String, String> properties;


    User(final String name, final Map<String, String> properties) {
        this.name = name;
        this.properties = new HashMap<String, String>(properties);
    }


    public String getName() {
        return name;
    }

    public Map<String, String> properties() {
        return Collections.unmodifiableMap(properties);
    }
    
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry property : properties.entrySet()) {
            sb.append(property.toString());
            sb.append(", ");
        }
        String propertiesAsString = sb.length() > 0 ?
                sb.toString().substring(0, sb.length() - 2) : "";
        return name + (propertiesAsString.isEmpty() ?
                "" : " (" + propertiesAsString + ")");
    }
}

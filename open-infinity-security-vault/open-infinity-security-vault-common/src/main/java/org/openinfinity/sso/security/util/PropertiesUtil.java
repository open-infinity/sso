/*
 * Copyright (c) 2013 the original author or authors.
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
package org.openinfinity.sso.security.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.openinfinity.core.util.IOUtil;

/**
 * Utility for managing properties files. 
 *  
 * @author Ilkka Leinonen
 * @version 1.0.0
 * @since 1.0.0
 */
public class PropertiesUtil {
	
	//"-DsecurityVaultPropertiesLocation=/opt/openinfinity/3.0.0/tomcat/conf/conf.d/securityvault.properties";
	private static final String PROPERTY_FILE_LOCATION = System.getenv("securityVaultPropertiesLocation");
	
	/**
	 * Properties object for storing key value pairs.
	 */
	private static Properties PROPERTIES = null;
	
	/**
	 * Initializes the configuration.
	 */
	public static void init() {
		File file = null;
		FileInputStream fileInputStream = null;
		try {
			file = new File(PROPERTY_FILE_LOCATION);
			fileInputStream = new FileInputStream(file);
			PROPERTIES = new Properties();
			PROPERTIES.load(fileInputStream);
		} catch (Throwable throwable) {
			IOUtil.closeStream(fileInputStream);
			if (file != null) {
				file = null;
			}
		}
	}
	
	/**
	 * Loads the value for given key.
	 * 
	 * @param key Represents the key for value.
	 * @return String Represents the value corresponding to the key.
	 */
	public static String loadValue(String key) {
		return PROPERTIES.getProperty(key);
	}

}

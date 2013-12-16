package org.openinfinity.sso.security.util;

import org.junit.Before;
import org.junit.Test;

public class PropertiesUtilUnitTests {
	
	@Before
	public void setUp() {
		System.setProperty(GlobalVariables.SECURITY_VAULT_PROPERTIES_FILE_LOCATION, "src/test/resources/securityvault.properties");
		PropertiesUtil.init();
	}
	
	@Test
	public void testRetrievingValue() {
		System.out.println(PropertiesUtil.loadValue(GlobalVariables.ATTRIBUTE_BASED_ROLES_KEY));
	}

}

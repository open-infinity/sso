package org.openinfinity.sso.userprovisioning.bpmn.common;

import java.io.InputStream;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.activiti.engine.impl.pvm.PvmException;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.junit.BeforeClass;
import org.slf4j.bridge.SLF4JBridgeHandler;

public abstract class LoggingRouter {
	
	@BeforeClass
	public static void routeLoggingToSlf4j() {
		readJavaUtilLoggingConfigFromClasspath();
		java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");  
		Handler[] handlers = rootLogger.getHandlers();  
		for (int i = 0; i < handlers.length; i++) {  
			rootLogger.removeHandler(handlers[i]);  
		}
		SLF4JBridgeHandler.install();
	}
	
	public static void readJavaUtilLoggingConfigFromClasspath() {
	    InputStream inputStream = ReflectUtil.getResourceAsStream("logging.properties");
	    try {
	      if (inputStream != null) {
	        LogManager.getLogManager().readConfiguration(inputStream);

	        String redirectCommons = LogManager.getLogManager().getProperty("redirect.commons.logging");
	        if ((redirectCommons != null) && (!redirectCommons.equalsIgnoreCase("false"))) {
	          System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");
	        }
	      }
	    } catch (Exception e) {
	      throw new PvmException("couldn't initialize logging properly", e);
	    } finally {
	      IoUtil.closeSilently(inputStream);
	    }
	  }

}

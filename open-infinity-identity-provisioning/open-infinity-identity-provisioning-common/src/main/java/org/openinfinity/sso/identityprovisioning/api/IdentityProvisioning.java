package org.openinfinity.sso.identityprovisioning.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.LOCAL_VARIABLE, ElementType.PACKAGE, ElementType.TYPE})
public @interface IdentityProvisioning {}
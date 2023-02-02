package org.talend.components.api.service;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * use this to locate the method which fetch runtime schema in component properties
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface SchemaRetrieve {
    
}

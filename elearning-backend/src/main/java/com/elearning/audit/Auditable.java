package com.elearning.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer les méthodes à auditer automatiquement.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /** Action auditée (ex: UPLOAD_RESOURCE, CREATE_SESSION, etc.) */
    String action();

    /** Type d'entité concernée (ex: CourseResource, LiveSession) */
    String entityType() default "";
}

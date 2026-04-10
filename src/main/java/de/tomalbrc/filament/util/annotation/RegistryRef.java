package de.tomalbrc.filament.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE_PARAMETER})
public @interface RegistryRef {
    /**
     * reg. id
     */
    String value();

    boolean tags() default false;

    boolean tagsOnly() default false;

    boolean withHash() default false;
}
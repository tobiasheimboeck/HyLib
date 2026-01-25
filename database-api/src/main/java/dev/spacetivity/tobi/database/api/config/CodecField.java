package dev.spacetivity.tobi.database.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface CodecField {

    String value();

    String defaultValue() default "";

    boolean hasDefault() default false;

}

package org.lets_do_this_the_easy_way.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface DTOExtraField {
    String name();
    String type();
    String defaultValue() default "";
}

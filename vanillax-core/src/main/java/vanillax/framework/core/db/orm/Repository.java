package vanillax.framework.core.db.orm;

import vanillax.framework.core.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Repository {
    /* 사용할 DataSource 이름 */
    public String value() default Constants.DEFAULT_DATA_SOURCE;
}

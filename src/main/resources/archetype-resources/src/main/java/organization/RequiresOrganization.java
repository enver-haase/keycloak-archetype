package ${package}.organization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a view as accessible only when a specific organization is selected.
 * The {@link ${package}.base.MainLayout} checks this annotation on every
 * navigation event and redirects to the dashboard if the required organization
 * does not match the currently selected one. The side navigation is also
 * filtered accordingly.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiresOrganization {
    String value();
}

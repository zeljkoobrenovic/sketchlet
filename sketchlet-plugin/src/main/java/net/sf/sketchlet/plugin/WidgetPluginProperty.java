/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugin;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WidgetPluginProperty {

    String name();

    String initValue() default "";

    String description() default "";

    String[] valueList() default {};
}
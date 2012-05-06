package tv.kress.bill.minecraft.ezplugin;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cmd {
    String value();

    String permission() default "";

    String description() default "No description given";
}
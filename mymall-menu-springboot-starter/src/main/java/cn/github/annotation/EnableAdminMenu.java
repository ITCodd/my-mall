package cn.github.annotation;

import cn.github.selector.AdminMenuImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(AdminMenuImportSelector.class)
public @interface EnableAdminMenu {
}

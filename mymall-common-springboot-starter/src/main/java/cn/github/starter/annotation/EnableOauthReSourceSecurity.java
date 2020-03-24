package cn.github.starter.annotation;

import cn.github.starter.selector.OauthReSourceImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(OauthReSourceImportSelector.class)
public @interface EnableOauthReSourceSecurity {
}

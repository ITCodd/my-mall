package cn.github.starter.selector;

import cn.github.starter.config.OauthReSourceConfig;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class OauthReSourceImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        return new String[]{OauthReSourceConfig.class.getName()};
    }
}

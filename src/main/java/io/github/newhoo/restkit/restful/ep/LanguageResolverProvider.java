package io.github.newhoo.restkit.restful.ep;

import com.intellij.openapi.extensions.ExtensionPointName;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.restful.LanguageResolver;
import org.jetbrains.annotations.NotNull;

/**
 * LanguageResolverProvider
 *
 * @author huzunrong
 * @since 2.0.1
 */
@NotProguard
public interface LanguageResolverProvider {

    ExtensionPointName<LanguageResolverProvider> EP_NAME = ExtensionPointName.create("io.github.newhoo.restkit.languageResolver");

    @NotNull
    LanguageResolver createLanguageResolver();
}

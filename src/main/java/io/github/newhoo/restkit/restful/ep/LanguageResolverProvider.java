package io.github.newhoo.restkit.restful.ep;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.restful.LanguageResolver;
import org.jetbrains.annotations.NotNull;

/**
 * LanguageResolverProvider
 *
 * @author huzunrong
 * @since 2.0.1
 */
public interface LanguageResolverProvider {

    ExtensionPointName<LanguageResolverProvider> EP_NAME = ExtensionPointName.create("io.github.newhoo.restkit.languageResolver");

    @NotNull
    LanguageResolver createLanguageResolver(@NotNull Project project);
}

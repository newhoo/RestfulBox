package io.github.newhoo.restkit.restful.ep;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.restful.RequestResolver;
import org.jetbrains.annotations.NotNull;

/**
 * RestfulResolverProvider
 *
 * @author huzunrong
 * @since 2.0.0
 */
@NotProguard
public interface RestfulResolverProvider {

    ExtensionPointName<RestfulResolverProvider> EP_NAME = ExtensionPointName.create("io.github.newhoo.restkit.restfulResolver");

    RequestResolver createRequestResolver(@NotNull Project project);
}

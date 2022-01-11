package io.github.newhoo.restkit.restful.local;

import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.LocalApiLibrary;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.restful.ep.RestfulResolverProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.newhoo.restkit.common.RestConstant.WEB_FRAMEWORK_LOCAL;

/**
 * 本地缓存实现
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class LocalRequestResolver implements RequestResolver {

    @NotNull
    @Override
    public String getFrameworkName() {
        return WEB_FRAMEWORK_LOCAL;
    }

    @Override
    public List<RestItem> findRestItemInProject(@NotNull Project project) {
        return LocalApiLibrary.getInstance(project).getItemList();
    }

    public static class LocalCacheResolverProvider implements RestfulResolverProvider {
        @Override
        public RequestResolver createRequestResolver(@NotNull Project project) {
            return project.getComponent(LocalRequestResolver.class);
        }
    }
}

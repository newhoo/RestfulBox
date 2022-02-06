package io.github.newhoo.restkit.restful.local;

import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.restful.ep.RestfulResolverProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.common.RestConstant.WEB_FRAMEWORK_LOCAL;

/**
 * 本地缓存实现
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class LocalRequestResolver implements RequestResolver {

    private final LocalStoreHelper localStore;

    public LocalRequestResolver(Project project) {
        this.localStore = new LocalStoreHelper(project);
    }

    @NotNull
    @Override
    public String getFrameworkName() {
        return WEB_FRAMEWORK_LOCAL;
    }

    @Override
    public List<RestItem> findRestItemInProject(@NotNull Project project) {
        return localStore.queryAll();
    }

    @Override
    public void add(List<RestItem> itemList) {
        localStore.asyncAdd(itemList);
    }

    @Override
    public void update(List<RestItem> itemList) {
        AppExecutorUtil.getAppExecutorService().submit(() -> {
            List<RestItem> list = getRemainList(itemList);
            list.addAll(itemList);
            localStore.replaceAll(list);
        });
    }

    @Override
    public void delete(List<RestItem> itemList) {
        AppExecutorUtil.getAppExecutorService().submit(() -> {
            List<RestItem> list = getRemainList(itemList);
            Set<String> keySet = itemList.stream().map(item -> item.getUrl() + item.getMethod()).collect(Collectors.toSet());
            list.removeIf(item -> keySet.contains(item.getUrl() + item.getMethod()));
            localStore.replaceAll(list);
        });
    }

    private List<RestItem> getRemainList(List<RestItem> itemList) {
        List<RestItem> list = new ArrayList<>(localStore.queryAll());
        Set<String> keySet = itemList.stream().map(item -> item.getUrl() + item.getMethod()).collect(Collectors.toSet());
        list.removeIf(item -> keySet.contains(item.getUrl() + item.getMethod()));
        return list;
    }

    public static class FileResolverProvider implements RestfulResolverProvider {
        @Override
        public RequestResolver createRequestResolver(@NotNull Project project) {
            return new LocalRequestResolver(project);
        }
    }
}

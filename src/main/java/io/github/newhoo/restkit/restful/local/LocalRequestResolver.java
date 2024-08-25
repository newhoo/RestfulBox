package io.github.newhoo.restkit.restful.local;

import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.common.RestRegistry;
import io.github.newhoo.restkit.config.ConfigHelper;
import io.github.newhoo.restkit.config.ide.CommonSetting;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.restful.ep.RestfulResolverProvider;
import io.github.newhoo.restkit.util.FileUtils;
import io.github.newhoo.restkit.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * 本地缓存实现
 *
 * @author huzunrong
 * @since 2.0.1
 */
public class LocalRequestResolver implements RequestResolver {

    private final Project project;
    private final CommonSetting setting;

    public LocalRequestResolver(Project project) {
        this.project = project;
        this.setting = ConfigHelper.getCommonSetting(project);
    }

    @NotNull
    @Override
    public String getFrameworkName() {
        return "Local Store";
    }

    @Override
    public ScanType getScanType() {
        return ScanType.STORAGE;
    }

    @NotNull
    @Override
    public List<RestItem> findRestItemInProject(@NotNull Project project) {
        return queryAll();
    }

    @Override
    public void add(List<RestItem> itemList) {
        itemList.forEach(item -> item.setId(UUID.randomUUID().toString()));
        AppExecutorUtil.getAppExecutorService().submit(() -> {
            List<RestItem> list = new ArrayList<>(queryAll());
            list.addAll(itemList);
            replaceAll(list);
        });
    }

    @Override
    public void update(List<RestItem> itemList) {
        AppExecutorUtil.getAppExecutorService().submit(() -> {
            Map<String, RestItem> idMap = itemList.stream()
                                                  .collect(Collectors.toMap(RestItem::getId, o -> o, (o1, o2) -> o1));
            List<RestItem> list = new ArrayList<>(queryAll());
            list.stream()
                .filter(item -> idMap.containsKey(item.getId()))
                .forEach(item -> {
                    RestItem restItem = idMap.get(item.getId());
                    item.setUrl(restItem.getUrl());
                    item.setMethod(restItem.getMethod());
                    item.setHeaders(restItem.getHeaders());
                    item.setParams(restItem.getParams());
                    item.setBodyJson(restItem.getBodyJson());
                    item.setDescription(restItem.getDescription());
                    item.setProject(restItem.getProject());
                    item.setModuleName(restItem.getModuleName());
                    item.setPackageName(restItem.getPackageName());
                    item.setFramework(restItem.getFramework());
                    item.setTs(restItem.getTs());
                });
            replaceAll(list);
        });
    }

    @Override
    public void delete(List<RestItem> itemList) {
        AppExecutorUtil.getAppExecutorService().submit(() -> {
            Set<String> idSet = itemList.stream().map(RestItem::getId).collect(Collectors.toSet());
            List<RestItem> list = new ArrayList<>(queryAll());
            list.removeIf(item -> idSet.contains(item.getId()));
            replaceAll(list);
        });
    }

    private List<RestItem> queryAll() {
        String apiFile = setting.getApiFilePath();
        // 默认创建项目级别
        if (StringUtils.isEmpty(apiFile)) {
            apiFile = FileUtils.getApiFilePath(project);
            setting.setApiFilePath(apiFile);
        }
        try {
            Path path = Paths.get(apiFile);
            if (Files.notExists(path)) {
                return Collections.emptyList();
            }
            byte[] bytes = Files.readAllBytes(path);
            List<RestItem> list = JsonUtils.fromJsonArr(new String(bytes), RestItem.class);
            if (list != null) {
                List<RestItem> needInitId = list.stream()
                                                .filter(item -> StringUtils.isEmpty(item.getId()))
                                                .collect(Collectors.toList());
                // 初始化API的ID
                if (!needInitId.isEmpty()) {
                    list.stream()
                        .filter(item -> StringUtils.isEmpty(item.getId()))
                        .forEach(item -> item.setId(UUID.randomUUID().toString()));
                    replaceAll(list);
                }
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private void replaceAll(List<RestItem> restItems) {
        String apiFile = setting.getApiFilePath();
        try {
            Path path = Paths.get(apiFile);
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
                Files.write(path, JsonUtils.toJson(restItems).getBytes(StandardCharsets.UTF_8), CREATE);
                return;
            }
            Files.write(path, JsonUtils.toJson(restItems).getBytes(StandardCharsets.UTF_8), TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class FileResolverProvider implements RestfulResolverProvider {
        @Override
        public RequestResolver createRequestResolver(@NotNull Project project) {
            if (RestRegistry.enableLocalApi()) {
                return new LocalRequestResolver(project);
            }
            return null;
        }
    }
}

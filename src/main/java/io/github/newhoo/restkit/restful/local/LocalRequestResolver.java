package io.github.newhoo.restkit.restful.local;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.CommonSetting;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.config.SettingConfigurable;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.restful.ep.RestfulResolverProvider;
import io.github.newhoo.restkit.util.FileUtils;
import io.github.newhoo.restkit.util.HtmlUtil;
import io.github.newhoo.restkit.util.JsonUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
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

import static io.github.newhoo.restkit.common.RestConstant.WEB_FRAMEWORK_LOCAL;
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
        this.setting = CommonSettingComponent.getInstance(project).getState();
    }

    @NotNull
    @Override
    public String getFrameworkName() {
        return WEB_FRAMEWORK_LOCAL;
    }

    @Override
    public ScanType getScanType() {
        return ScanType.STORAGE;
    }

    @Override
    public boolean checkConfig() {
        if (StringUtils.isEmpty(setting.getApiFilePath())) {
            NotifierUtils.infoBalloon("", "Local api store path is empty. " + HtmlUtil.link("Edit", "Edit"), new NotificationListener.Adapter() {
                @Override
                protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingConfigurable.class);
                }
            }, project);
            return false;
        }
        return true;
    }

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
                    item.setModuleName(restItem.getModuleName());
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
            return new LocalRequestResolver(project);
        }
    }
}

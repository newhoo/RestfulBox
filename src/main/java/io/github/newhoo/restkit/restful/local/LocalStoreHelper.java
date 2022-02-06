package io.github.newhoo.restkit.restful.local;

import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.CommonSetting;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.util.FileUtils;
import io.github.newhoo.restkit.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * local api operation
 *
 * @author huzunrong
 * @since 2.0.2
 */
public class LocalStoreHelper {

    private final Project project;
    private final CommonSetting setting;

    public LocalStoreHelper(Project project) {
        this.project = project;
        this.setting = CommonSettingComponent.getInstance(project).getState();
    }

    public List<RestItem> queryAll() {
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
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public void asyncAdd(List<RestItem> itemList) {
        AppExecutorUtil.getAppExecutorService().submit(() -> {
            List<RestItem> list = new ArrayList<>(queryAll());
            list.addAll(itemList);
            replaceAll(list);
        });
    }

    public void asyncReplaceAll(List<RestItem> restItems) {
        AppExecutorUtil.getAppExecutorService().submit(() -> {
            replaceAll(restItems);
        });
    }

    public void replaceAll(List<RestItem> restItems) {
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
}

package io.github.newhoo.restkit.datasource.ide;

import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.ProjectInfo;
import io.github.newhoo.restkit.common.RestClientEditorInfo;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.CommonHeader;
import io.github.newhoo.restkit.config.Environment;
import io.github.newhoo.restkit.config.ProjectTypeSetting;
import io.github.newhoo.restkit.config.RequestSetting;
import io.github.newhoo.restkit.config.certificate.Certificate;
import io.github.newhoo.restkit.datasource.DataSource;
import io.github.newhoo.restkit.datasource.ep.DataSourceProvider;
import io.github.newhoo.restkit.util.FileUtils;
import io.github.newhoo.restkit.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.common.RestConstant.DATA_SOURCE_IDE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class IdeDataSource implements DataSource {

    @Override
    public @NotNull String name() {
        return DATA_SOURCE_IDE;
    }

    @Override
    public @NotNull String getDefaultConfig() {
        return "";
    }

    @Override
    public boolean checkConfig(String config) {
        return true;
    }

    @Override
    public void init(String config) {
        try {
            Path path = Paths.get(FileUtils.getUserHomeBasePath() + "command.json");
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean getStatus() {
        return true;
    }

    @Override
    public void destroy() {
    }

    @Override
    public @NotNull List<ProjectInfo> selectProjects(Project project) {
        List<ProjectInfo> projectInfos = new LinkedList<>();
        File baseDir = new File(FileUtils.getUserHomeBasePath());
        String[] projectDirs = baseDir.list((dir, name) -> new File(dir + File.separator + name).isDirectory() && !StringUtils.equalsAny(name, "Default (Template) Project", "logs", "Downloads", "pki", "-"));
        for (String projectDir : projectDirs) {
            ProjectInfo projectInfo = new ProjectInfo();
            projectInfo.setProject(projectDir);
            projectInfo.setCountApi(queryAll(projectDir).size());
            projectInfo.setCountSetting(Files.exists(Paths.get(FileUtils.getRestDirectory(projectDir) + "setting-request.json")) ? 1 : 0);
            projectInfo.setCountEnv(Files.exists(Paths.get(FileUtils.getRestDirectory(projectDir) + "setting-environment.json")) ? 1 : 0);
            projectInfo.setCountHeader(Files.exists(Paths.get(FileUtils.getRestDirectory(projectDir) + "setting-header.json")) ? 1 : 0);
            projectInfo.setCountParameter(countParameter(projectDir));
            projectInfos.add(projectInfo);
        }
        return projectInfos;
    }

    @Override
    public void renameProject(String projectName, String newProjectName, Project project) {
        try {
            Files.move(Path.of(FileUtils.getRestDirectory(projectName)), Path.of(FileUtils.getRestDirectory(newProjectName)), REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeProject(String projectName, Project project) {
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(FileUtils.getRestDirectory(projectName)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull RequestSetting selectRequestSetting(String projectName, Project project) {
        try {
            Path path = Paths.get(FileUtils.getRestDirectory(projectName) + "setting-request.json");
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
            RequestSetting setting = JsonUtils.fromJson(Files.readString(path), RequestSetting.class);
            if (setting != null) {
                if (!StringUtils.equals(projectName, setting.getProject())) {
                    setting.setProject(projectName);
                }
                return setting;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestSetting setting = new RequestSetting();
        setting.setProject(projectName);
        return setting;
    }

    @Override
    public void syncRequestSetting(RequestSetting requestSetting, Project project) {
        Path path = Paths.get(FileUtils.getRestDirectory(requestSetting.getProject()) + "setting-request.json");
        try {
            Files.write(path, JsonUtils.toJson(requestSetting).getBytes(StandardCharsets.UTF_8), TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull String selectSetting(String projectName, String type, Project project) {
        try {
            Path path = StringUtils.isNotEmpty(projectName)
                    ? Paths.get(FileUtils.getRestDirectory(projectName) + type)
                    : Paths.get(FileUtils.getUserHomeBasePath() + type);
            if (Files.exists(path)) {
                return Files.readString(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @NotNull
    @Override
    public List<ProjectTypeSetting> selectSetting(List<String> projectNames, List<String> types, Project project) {
        List<ProjectTypeSetting> list = new LinkedList<>();
        for (int i = 0; i < projectNames.size(); i++) {
            ProjectTypeSetting setting = new ProjectTypeSetting();
            setting.setProject(projectNames.get(i));
            setting.setType(types.get(i));
            setting.setContent(selectSetting(setting.getProject(), setting.getType(), project));
            list.add(setting);
        }
        return list;
    }

    @Override
    public void syncSetting(String projectName, String type, String content, Project project) {
        Path path = StringUtils.isNotEmpty(projectName)
                ? Paths.get(FileUtils.getRestDirectory(projectName) + type)
                : Paths.get(FileUtils.getUserHomeBasePath() + type);
        try {
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
            Files.write(path, content.getBytes(StandardCharsets.UTF_8), TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull Environment selectEnvironment(String projectName, Project project) {
        try {
            Path path = Paths.get(FileUtils.getRestDirectory(projectName) + "setting-environment.json");
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
            Environment environment = JsonUtils.fromJson(Files.readString(path), Environment.class);
            if (environment != null) {
                if (!StringUtils.equals(projectName, environment.getProject())) {
                    environment.setProject(projectName);
                }
                return environment;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Environment environment = new Environment();
        environment.setProject(projectName);
        return environment;
    }

    @Override
    public void syncEnvironment(Environment environment, Project project) {
        try {
            Path path = Paths.get(FileUtils.getRestDirectory(environment.getProject()) + "setting-environment.json");
            Files.write(path, JsonUtils.toJson(environment).getBytes(StandardCharsets.UTF_8), TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull CommonHeader selectHeader(String projectName, Project project) {
        try {
            Path path = Paths.get(FileUtils.getRestDirectory(projectName) + "setting-header.json");
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
            CommonHeader commonHeader = JsonUtils.fromJson(Files.readString(path), CommonHeader.class);
            if (commonHeader != null) {
                if (!StringUtils.equals(projectName, commonHeader.getProject())) {
                    commonHeader.setProject(projectName);
                }
                return commonHeader;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        CommonHeader commonHeader = new CommonHeader();
        commonHeader.setProject(projectName);
        return commonHeader;
    }

    @Override
    public void syncHeader(CommonHeader commonHeader, Project project) {
        try {
            Path path = Paths.get(FileUtils.getRestDirectory(commonHeader.getProject()) + "setting-header.json");
            Files.write(path, JsonUtils.toJson(commonHeader).getBytes(StandardCharsets.UTF_8), TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull List<Certificate> selectCertificate(Project project) {
        try {
            Path path = Paths.get(FileUtils.getUserHomeBasePath() + "certificate.json");
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
            List<Certificate> certificates = JsonUtils.fromJsonArr(Files.readString(path), Certificate.class);
            if (certificates != null) {
                return certificates;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LinkedList<>();
    }

    @Override
    public Certificate selectEnabledCertificate(String host, Project project) {
        return selectCertificate(project)
                .stream()
                .filter(Certificate::getEnable)
                .filter(e -> host.equals(e.getHost()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void syncCertificate(List<Certificate> certificates, Project project) {
        Path path = Paths.get(FileUtils.getUserHomeBasePath() + "certificate.json");
        try {
            Files.write(path, JsonUtils.toJson(certificates).getBytes(StandardCharsets.UTF_8), TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull List<KV> selectParameter(RestClientEditorInfo restClientEditorInfo, Project project) {
        try {
            Path path = Paths.get(FileUtils.getRestDirectory(restClientEditorInfo.getProject()) + "parameter-library.json");
            if (Files.notExists(path)) {
                Files.createFile(path);
            }

            Map<String, Map<String, List<KV>>> projectParameterFromFile = JsonUtils.fromJson(Files.readString(path), TypeToken.getParameterized(Map.class, String.class, TypeToken.getParameterized(Map.class, String.class, TypeToken.getParameterized(List.class, KV.class).getType()).getType()).getType());
            if (projectParameterFromFile != null) {
                return projectParameterFromFile.getOrDefault(restClientEditorInfo.getEditor(), Collections.emptyMap())
                                               .getOrDefault(restClientEditorInfo.getParameterKey(), Collections.emptyList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public int countParameter(String projectName) {
        try {
            Path path = Paths.get(FileUtils.getRestDirectory(projectName) + "parameter-library.json");
            if (Files.notExists(path)) {
                return 0;
            }

            Map<String, Map<String, List<KV>>> projectParameterFromFile = JsonUtils.fromJson(Files.readString(path), TypeToken.getParameterized(Map.class, String.class, TypeToken.getParameterized(Map.class, String.class, TypeToken.getParameterized(List.class, KV.class).getType()).getType()).getType());
            if (projectParameterFromFile != null) {
                return projectParameterFromFile.values().stream().mapToInt(e -> e.size()).sum();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void syncParameter(RestClientEditorInfo editorInfo, List<KV> parameterList, Project project) {
        try {
            Path path = Paths.get(FileUtils.getRestDirectory(editorInfo.getProject()) + "parameter-library.json");
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
            Map<String, Map<String, List<KV>>> projectParameterFromFile = JsonUtils.fromJson(Files.readString(path), TypeToken.getParameterized(Map.class, String.class, TypeToken.getParameterized(Map.class, String.class, TypeToken.getParameterized(List.class, KV.class).getType()).getType()).getType());
            if (projectParameterFromFile == null) {
                projectParameterFromFile = new HashMap<>();
                projectParameterFromFile.put(editorInfo.getEditor(), new HashMap<>());
            }
            Map<String, List<KV>> orDefault = projectParameterFromFile.get(editorInfo.getEditor());
            orDefault.put(editorInfo.getParameterKey(), parameterList);
            projectParameterFromFile.put(editorInfo.getEditor(), orDefault);

            Files.write(path, JsonUtils.toJson(projectParameterFromFile).getBytes(StandardCharsets.UTF_8), TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public List<RestItem> selectRestItems(@NotNull List<String> selectedProjectNames, Project project) {
        return queryAll(selectedProjectNames.get(0));
    }

    @Override
    public void syncRestItems(List<RestItem> restItems, Project project) {
        String projectName = restItems.get(0).getProject();
        List<RestItem> list = new ArrayList<>(restItems);
        List<RestItem> collect = queryAll(projectName).stream().filter(restItem -> !restItem.isSyncFlag()).collect(Collectors.toList());
        list.addAll(collect);
        replaceAll(list, projectName);
    }

    @Override
    public void addRestItem(List<RestItem> restItems, Project project) {
        String projectName = restItems.get(0).getProject();
        restItems.forEach(item -> item.setId(UUID.randomUUID().toString()));
        AppExecutorUtil.getAppExecutorService().submit(() -> {
            List<RestItem> list = new ArrayList<>(queryAll(projectName));
            list.addAll(restItems);
            replaceAll(list, projectName);
        });
    }

    @Override
    public void updateRestItem(List<RestItem> restItems, Project project) {
        String projectName = restItems.get(0).getProject();
        AppExecutorUtil.getAppExecutorService().submit(() -> {
            Map<String, RestItem> idMap = restItems.stream()
                                                   .collect(Collectors.toMap(RestItem::getId, o -> o, (o1, o2) -> o1));
            List<RestItem> list = new ArrayList<>(queryAll(projectName));
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
            replaceAll(list, projectName);
        });
    }

    @Override
    public void deleteRestItem(List<RestItem> restItems, Project project) {
        String projectName = restItems.get(0).getProject();
        AppExecutorUtil.getAppExecutorService().submit(() -> {
            Set<String> idSet = restItems.stream().map(RestItem::getId).collect(Collectors.toSet());
            List<RestItem> list = new ArrayList<>(queryAll(projectName));
            list.removeIf(item -> idSet.contains(item.getId()));
            replaceAll(list, projectName);
        });
    }

    private List<RestItem> queryAll(String projectName) {
        String apiFile = FileUtils.getRestDirectory(projectName) + "apifile.json";
        try {
            Path path = Paths.get(apiFile);
            if (Files.notExists(path)) {
                return Collections.emptyList();
            }
            List<RestItem> list = JsonUtils.fromJsonArr(Files.readString(path), RestItem.class);
            if (list != null) {
                List<RestItem> needInitId = list.stream()
                                                .filter(item -> StringUtils.isEmpty(item.getId()))
                                                .collect(Collectors.toList());
                // 初始化API的ID
                if (!needInitId.isEmpty()) {
                    list.stream()
                        .filter(item -> StringUtils.isEmpty(item.getId()))
                        .forEach(item -> item.setId(UUID.randomUUID().toString()));
                    replaceAll(list, projectName);
                }
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private void replaceAll(List<RestItem> restItems, String projectName) {
        String apiFile = FileUtils.getRestDirectory(projectName) + "apifile.json";
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

    public static class IdeDataSourceProvider implements DataSourceProvider {

        @Override
        public DataSource createRepository() {
            return new IdeDataSource();
        }
    }
}

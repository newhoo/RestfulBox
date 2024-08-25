//package io.github.newhoo.restkit.action;
//
//import com.intellij.openapi.actionSystem.AnAction;
//import com.intellij.openapi.actionSystem.AnActionEvent;
//import com.intellij.openapi.project.Project;
//import io.github.newhoo.restkit.common.RestItem;
//import io.github.newhoo.restkit.common.RestRegistry;
//import io.github.newhoo.restkit.config.CommonHeader;
//import io.github.newhoo.restkit.config.ConfigHelper;
//import io.github.newhoo.restkit.config.Environment;
//import io.github.newhoo.restkit.config.RequestSetting;
//import io.github.newhoo.restkit.config.certificate.Certificate;
//import io.github.newhoo.restkit.datasource.DataSource;
//import io.github.newhoo.restkit.datasource.DataSourceHelper;
//import io.github.newhoo.restkit.util.IdeaUtils;
//import io.github.newhoo.restkit.util.JsonUtils;
//import io.github.newhoo.restkit.util.NotifierUtils;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import static io.github.newhoo.restkit.common.RestConstant.DATA_SOURCE_IDE;
//
///**
// * ExportPluginDataAction
// *
// * @author huzunrong
// * @since 3.0.1
// */
//public class ExportPluginDataAction extends AnAction {
//
//    @Override
//    public void update(@NotNull AnActionEvent e) {
//        Project project = e.getProject();
//        if (project == null) return;
//
//        String name = DataSourceHelper.getDataSource().name();
//        e.getPresentation().setEnabledAndVisible(!DATA_SOURCE_IDE.equals(name) && RestRegistry.exportData()
//                && !ConfigHelper.getGlobalSetting().getAllProjects().isEmpty());
//    }
//
//    @Override
//    public void actionPerformed(@NotNull AnActionEvent e) {
//        Project project = e.getProject();
//        if (project == null) return;
//
//        Set<String> allProjects = ConfigHelper.getGlobalSetting().getAllProjects();
//        if (allProjects.isEmpty()) {
//            return;
//        }
//        DataSource dataSource = DataSourceHelper.getDataSource();
//
//        Map<String, Object> pluginDataMap = new HashMap<>();
//        Map<String, Object> projectDataMap = new HashMap<>();
//
//        for (String p : allProjects) {
//            Map<String, Object> projectData = new HashMap<>();
//
//            List<RestItem> restItems = dataSource.selectRestItems(Collections.singletonList(p), project);
//            projectData.put("restItems", restItems);
//
//            RequestSetting requestSetting = dataSource.selectRequestSetting(p, project);
//            projectData.put("requestSetting", requestSetting);
//
//            Environment environment = dataSource.selectEnvironment(p, project);
//            projectData.put("environment", environment);
//
//            CommonHeader commonHeader = dataSource.selectHeader(p, project);
//            projectData.put("commonHeader", commonHeader);
//
//            /*RestClientEditorInfo editorInfo = new RestClientEditorInfo();
//            editorInfo.setProject(p);
//            editorInfo.setMethod();
//            editorInfo.setUrl();
//            editorInfo.setEditor();
//
//            List<KV> kvs = dataSource.selectParameter(editorInfo, project);
//            projectData.put("parameter", requestSetting);*/
//
//            projectDataMap.put(p, projectData);
//        }
//        pluginDataMap.put("projectData", projectDataMap);
//
//        List<Certificate> certificates = dataSource.selectCertificate(project);
//        pluginDataMap.put("certificates", certificates);
//
//
//        String json = JsonUtils.toJson(pluginDataMap);
//        IdeaUtils.copyToClipboard(json);
//        NotifierUtils.infoBalloon("Export plugin data successful", "Plugin data copied to clipboard, just import in another datasource.", null, project);
//    }
//}

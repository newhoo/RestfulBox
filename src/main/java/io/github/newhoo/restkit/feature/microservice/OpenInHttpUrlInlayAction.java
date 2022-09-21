package io.github.newhoo.restkit.feature.microservice;//package io.github.newhoo.restkit.feature.microservice;
//
//import com.intellij.microservices.url.UrlResolveRequest;
//import com.intellij.microservices.url.inlay.UrlPathInlayAction;
//import com.intellij.microservices.url.references.UrlPathContext;
//import com.intellij.openapi.editor.Editor;
//import com.intellij.openapi.module.Module;
//import com.intellij.openapi.module.ModuleUtil;
//import com.intellij.openapi.project.Project;
//import com.intellij.psi.PsiFile;
//import io.github.newhoo.restkit.common.ToolkitIcons;
//import io.github.newhoo.restkit.config.CommonSettingComponent;
//import io.github.newhoo.restkit.i18n.RestBundle;
//import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
//import io.github.newhoo.restkit.util.FileUtils;
//import org.jetbrains.annotations.NotNull;
//
//import javax.swing.*;
//import java.awt.event.MouseEvent;
//
///**
// * OpenInHttpUrlInlayAction
// *
// * @author huzunrong
// * @since 2.0.1
// */
//public class OpenInHttpUrlInlayAction implements UrlPathInlayAction {
//
//    @NotNull
//    @Override
//    public Icon getIcon() {
//        return ToolkitIcons.SERVICE;
//    }
//
//    @NotNull
//    @Override
//    public String getName() {
//        return RestBundle.message("toolkit.navigate.text");
//    }
//
//    @Override
//    public void actionPerformed(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull UrlPathContext urlPathContext, @NotNull MouseEvent mouseEvent) {
//        Project project = editor.getProject();
//        // urlPathContext.getResolveRequests().iterator().next().getMethod()
//        if (project != null) {
//            boolean usingFileName = CommonSettingComponent.getInstance(project).getState().isDisplayApiGroupUsingFileName();
//            for (UrlResolveRequest urlResolveRequest : urlPathContext.getResolveRequests()) {
//                String presentation = urlResolveRequest.getPath().getPresentation();
//                String method = urlResolveRequest.getMethod();
//                String moduleName = null;
//                if (usingFileName) {
//                    moduleName = FileUtils.removeFileSuffix(psiFile.getName());
//                } else {
//                    Module module = ModuleUtil.findModuleForFile(psiFile);
//                    if (module != null) {
//                        moduleName = module.getName();
//                    }
//                }
//                String finalModuleName = moduleName;
//                RestToolWindowFactory.getRestServiceToolWindow(project, restServiceToolWindow -> {
//                    restServiceToolWindow.navigateToTree(presentation, method, finalModuleName);
//                });
//                break;
//            }
//        }
//    }
//
//    @Override
//    public boolean isAvailable(@NotNull PsiFile psiFile, @NotNull UrlPathContext urlPathContext) {
//        return true;
//    }
//}

package io.github.newhoo.restkit.parameter.library;//package io.github.newhoo.restkit.parameter.library;
//
//import com.intellij.icons.AllIcons;
//import com.intellij.openapi.actionSystem.AnAction;
//import com.intellij.openapi.actionSystem.AnActionEvent;
//import com.intellij.openapi.actionSystem.Presentation;
//import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
//import com.intellij.openapi.actionSystem.impl.ActionButtonWithText;
//import com.intellij.openapi.editor.Editor;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.ui.InputValidator;
//import com.intellij.openapi.ui.Messages;
//import com.intellij.util.ui.JBUI;
//import io.github.newhoo.restkit.common.KV;
//import io.github.newhoo.restkit.common.RestClientEditorInfo;
//import io.github.newhoo.restkit.common.RestDataKey;
//import io.github.newhoo.restkit.config.ParameterLibrary;
//import org.apache.commons.lang3.StringUtils;
//import org.jetbrains.annotations.NotNull;
//
//import javax.swing.*;
//import java.awt.*;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
///**
// * SaveParameterAction
// *
// * @author huzunrong
// * @since 1.0.8
// */
//public class SaveParameterAction extends AnAction implements CustomComponentAction {
//
//    private final Editor editor;
//    private final ParameterLibrary parameterLibrary;
//
//    public SaveParameterAction(Editor editor, ParameterLibrary parameterLibrary) {
//        super(() -> "", () -> "Save parameter", AllIcons.Actions.MenuSaveall);
//        this.editor = editor;
//        this.parameterLibrary = parameterLibrary;
//    }
//
//    @Override
//    public void update(@NotNull AnActionEvent e) {
//        Project project = editor.getProject();
//        if (project == null || project.isDefault()) {
//            return;
//        }
//        Presentation presentation = e.getPresentation();
//        presentation.setEnabled(editor.getDocument().getTextLength() > 0
//                && RestDataKey.CLIENT_EDITOR_INFO.getData(e.getDataContext()) != null);
//    }
//
//    @Override
//    public JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
//        // JBUI.size(16)
//        // ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE
//        return new ActionButtonWithText(this, presentation, place, JBUI.size(18)) {
//            @Override
//            protected int iconTextSpace() {
//                return JBUI.scale(2);
//            }
//
//            @Override
//            public Insets getInsets() {
//                return JBUI.insets(2);
//            }
//
//            @Override
//            protected Insets getMargins() {
////                    if (myPresentation.icon == AllIcons.General.ReaderMode) JBUI.emptyInsets() else JBUI.insetsRight(5)
//                return JBUI.emptyInsets();
//            }
//        };
//    }
//
//    @Override
//    public void actionPerformed(@NotNull AnActionEvent e) {
//        RestClientEditorInfo restClientEditorInfo = RestDataKey.CLIENT_EDITOR_INFO.getData(e.getDataContext());
//        if (restClientEditorInfo == null) {
//            return;
//        }
//        Map<String, Map<String, java.util.List<KV>>> paramMap = parameterLibrary.getParameterMap();
//        Map<String, List<KV>> param = paramMap.getOrDefault(restClientEditorInfo.getEditor(), new HashMap<>(2));
//        List<KV> parameterList = param.getOrDefault(restClientEditorInfo.getParameterKey(), new LinkedList<>());
//
//        String paramName = Messages.showInputDialog(e.getProject(), "Input unique parameter name", "Save " + restClientEditorInfo.getEditor() + " Parameter", null, "parameter1", new InputValidator() {
//
//            @Override
//            public boolean checkInput(String inputString) {
//                return StringUtils.isNotEmpty(inputString) && parameterList.stream().noneMatch(kv -> inputString.equals(kv.getKey()));
//            }
//
//            @Override
//            public boolean canClose(String inputString) {
//                return true;
//            }
//        });
//        if (StringUtils.isEmpty(paramName)) {
//            return;
//        }
//
//        parameterList.add(new KV(paramName, editor.getDocument().getText()));
//        param.putIfAbsent(restClientEditorInfo.getParameterKey(), parameterList);
//        paramMap.putIfAbsent(restClientEditorInfo.getEditor(), param);
//    }
//}
package io.github.newhoo.restkit.util;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorImpl;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.AppUIUtil;
import com.intellij.util.DisposeAwareRunnable;
import io.github.newhoo.restkit.common.NotProguard;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;

/**
 * IdeaUtil
 *
 * @author huzunrong
 * @since 1.0.0
 */
@NotProguard
public class IdeaUtils {

    public static void copyToClipboard(String content) {
        if (StringUtils.isNoneEmpty(content)) {
            CopyPasteManager.getInstance().setContents(new StringSelection(content));
        }
    }

    /*public static void runWhenInitialized(final Project project, final Runnable r) {

        if (project.isDisposed()) {
            return;
        }

        if (isNoBackgroundMode()) {
            r.run();
            return;
        }

        if (!project.isInitialized()) {
            StartupManager.getInstance(project).registerPostStartupActivity(DisposeAwareRunnable.create(r, project));
            return;
        }

*//*        System.out.println((DumbService.getInstance(project).isDumb()));
        if (DumbService.getInstance(project).isDumb()) {
//            return;
            runWhenInitialized(project,r);
        }*//*
//        runDumbAware(project, r);
        invokeLater(project, r);
//        ApplicationManager.getApplication().invokeAndWait(r);
    }*/

    public static void run(Runnable runnable) {
        ApplicationManager.getApplication().executeOnPooledThread(runnable);
    }

    public static void invokeOnEdt(Runnable runnable) {
        AppUIUtil.invokeOnEdt(runnable);
    }

    public static void runWhenProjectIsReady(final Project project, final Runnable runnable) {
//        DumbService.getInstance(project).runWhenSmart(runnable);
        DumbService.getInstance(project).smartInvokeLater(runnable);
//        DumbService.getInstance(project).runReadActionInSmartMode(runnable);
    }

    public static void runDumbAware(final Project project, final Runnable r) {
        if (DumbService.isDumbAware(r)) {
            r.run();
        } else {
            DumbService.getInstance(project).runWhenSmart(DisposeAwareRunnable.create(r, project));
        }
    }

    public static void invokeLater(Runnable r) {
        ApplicationManager.getApplication().invokeLater(r);
    }

    public static void invokeLater(Project p, Runnable r) {
        invokeLater(p, ModalityState.defaultModalityState(), r);
    }

    public static void invokeLater(final Project p, final ModalityState state, final Runnable r) {
        if (isNoBackgroundMode()) {
            r.run();
        } else {
            ApplicationManager.getApplication().invokeLater(DisposeAwareRunnable.create(r, p), state);
        }
    }

    public static void runWriteAction(@NotNull Runnable action) {
        ApplicationManager.getApplication().runWriteAction(action);
    }

    public static boolean isNoBackgroundMode() {
        return (ApplicationManager.getApplication().isUnitTestMode()
                || ApplicationManager.getApplication().isHeadlessEnvironment());
    }

    public static FileEditor createEditor(String filename, Language language, String text, String menuGroupId, Project project) {
        if (StringUtils.isEmpty(text)) {
            text = "";
        }
        PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(filename, language, text);
        DaemonCodeAnalyzer daemonCodeAnalyzer = DaemonCodeAnalyzer.getInstance(project);
        daemonCodeAnalyzer.setImportHintsEnabled(psiFile, false);
        FileEditor fileEditor = TextEditorProvider.getInstance().createEditor(project, psiFile.getVirtualFile());
        if (fileEditor instanceof PsiAwareTextEditorImpl) {
            Editor editor = ((PsiAwareTextEditorImpl) fileEditor).getEditor();
            if (language instanceof PlainTextLanguage) {
                daemonCodeAnalyzer.setHighlightingEnabled(psiFile, false);
            }

            EditorSettings settings = editor.getSettings();
            settings.setGutterIconsShown(false);

            if (StringUtils.isNotEmpty(menuGroupId) && editor instanceof EditorEx) {
                // IdeActions.GROUP_BASIC_EDITOR_POPUP
                ((EditorEx) editor).setContextMenuGroupId(menuGroupId);
            }
        }
        Disposer.register(project, fileEditor);
        return fileEditor;
    }

//    public static Editor createEditor(String filename, Project project) {
//        final File file = new File(project.getBasePath() + "/" + DIRECTORY_STORE_FOLDER + "/" + filename);
//        if (!file.exists()) {
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        final VirtualFile fileByIoFile = LocalFileSystem.getInstance().findFileByIoFile(file);
//        // fileByIoFile.setCharset(StandardCharsets.UTF_8);
//        return EditorFactory.getInstance().createEditor(FileDocumentManager.getInstance().getCachedDocument(fileByIoFile));
//    }

    public static String getEditorText(FileEditor editor) {
        Document doc = FileDocumentManager.getInstance().getCachedDocument(editor.getFile());
        return doc.getText();
    }

    public static void setEditorText(FileEditor editor, String text, Project project) {
        if (editor == null) {
            return;
        }
        // FIX: Wrong line separators: '... 2.0//EN\">\r\n<html>\r\n...'
        text = StringUtils.replace(text, "\r", "");
        if (StringUtils.endsWith(text, "\n")) {
            text = text.substring(0, text.length() - 1);
        }
        final String docText = StringUtils.defaultString(text);
        WriteCommandAction.runWriteCommandAction(project, () -> {
            Document doc = FileDocumentManager.getInstance().getCachedDocument(editor.getFile());
            doc.setText(docText);
        });
    }

    public static boolean isNewUI() {
        try {
            return com.intellij.openapi.util.registry.Registry.is("ide.experimental.ui");
        } catch (Exception e) {
            return true;
        }
    }

//    public static void appendEditorText(Editor editor, String text, Project project) {
//        if (editor == null) {
//            return;
//        }
//        WriteCommandAction.runWriteCommandAction(project, () -> {
//            Document doc = FileDocumentManager.getInstance().getCachedDocument(editor());
//            doc.insertString(doc.getTextLength(), StringUtils.defaultString(text));
//        });
//    }

}

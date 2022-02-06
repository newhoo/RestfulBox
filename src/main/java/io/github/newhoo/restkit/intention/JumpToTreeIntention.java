package io.github.newhoo.restkit.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.restful.LanguageHelper;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.NotNull;

/**
 * JumpToTreeIntention
 *
 * @author huzunrong
 * @since 1.0
 */
public class JumpToTreeIntention extends PsiElementBaseIntentionAction implements IntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        if (element.getParent() != null) {
            RestToolWindowFactory.getRestServiceToolWindow(project, restServiceToolWindow -> {
                restServiceToolWindow.navigateToTree(element.getParent());
            });
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return element.getParent() != null && LanguageHelper.canNavigateToTree(element.getParent());
    }

    @Nls(capitalization = Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return getClass().getSimpleName();
    }

    @NotNull
    @Override
    public String getText() {
        return RestBundle.message("toolkit.navigate.text");
    }
}
package io.github.newhoo.restkit.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.psi.PsiElement;
import io.github.newhoo.restkit.restful.LanguageHelper;
import io.github.newhoo.restkit.util.NotifierUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.datatransfer.StringSelection;

/**
 * 对象转成json
 */
public class ConvertClassToJSONAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        e.getPresentation().setEnabledAndVisible(psiElement != null
                && LanguageHelper.canConvertToJSON(psiElement)
        );
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiElement psiElement = e.getRequiredData(CommonDataKeys.PSI_ELEMENT);
        String json = LanguageHelper.convertClassToJSON(psiElement);
        if (StringUtils.isNotEmpty(json)) {
            CopyPasteManager.getInstance().setContents(new StringSelection(json));
            NotifierUtils.infoBalloon("", "Class object json copied to clipboard successfully.", null, psiElement.getProject());
        }
    }
}

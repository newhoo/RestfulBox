package io.github.newhoo.restkit.feature.javaimpl.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import io.github.newhoo.restkit.common.ToolkitIcons;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.restful.LanguageHelper;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import org.jetbrains.annotations.NotNull;

/**
 * MappingLineMarkerProvider
 *
 * @author newhoo
 * @date 2022/4/4 10:49
 * @since 2.0.5
 */
public class MappingLineMarkerProvider implements LineMarkerProvider {

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (LanguageHelper.canGenerateLineMarker(element)
                && CommonSettingComponent.getInstance(element.getProject()).getState().isEnableMethodLineMarker()) {
            return new LineMarkerInfo<>(element, element.getTextRange(), ToolkitIcons.REQUEST,
                    psiElement -> RestBundle.message("toolkit.navigate.text"),
                    (e, elt) -> {
                        RestToolWindowFactory.getRestServiceToolWindow(elt.getProject(), restServiceToolWindow -> {
                            restServiceToolWindow.navigateToTree(elt.getParent(), () -> LanguageHelper.generateRestItem(element));
                        });
                    },
                    GutterIconRenderer.Alignment.LEFT, () -> RestBundle.message("toolkit.config.name"));
        }
        return null;
    }
}

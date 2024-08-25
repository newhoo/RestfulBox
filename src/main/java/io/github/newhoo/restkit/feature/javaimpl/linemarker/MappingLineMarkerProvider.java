package io.github.newhoo.restkit.feature.javaimpl.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import io.github.newhoo.restkit.config.ConfigHelper;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.toolwindow.ToolWindowHelper;
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
        if (ConfigHelper.getGlobalSetting().isEnableMethodLineMarker() && RequestHelper.canGenerateLineMarker(element)) {
            return new LineMarkerInfo<>(element, element.getTextRange(), ConfigHelper.NAVIGATE_ICON,
                                        psiElement -> RestBundle.message("toolkit.navigate.text"),
                                        (e, elt) -> ToolWindowHelper.navigateToTree(elt.getParent(), () -> RequestHelper.generateRestItem(element)),
                                        GutterIconRenderer.Alignment.LEFT, () -> RestBundle.message("toolkit.name"));
        }
        return null;
    }
}

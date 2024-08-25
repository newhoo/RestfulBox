package io.github.newhoo.restkit.restful;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.restful.ep.LanguageResolverProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * LanguageHelper
 *
 * @author huzunrong
 * @since 1.0.0
 */
@NotProguard
public class LanguageHelper {

    public static final Map<Language, LanguageResolver> LANGUAGE_RESOLVER_MAP = LanguageResolverProvider.EP_NAME.getExtensionList()
                                                                                                                .stream()
                                                                                                                .filter(Objects::nonNull)
                                                                                                                .map(LanguageResolverProvider::createLanguageResolver)
                                                                                                                .collect(Collectors.toMap(LanguageResolver::getLanguage, o -> o, (o1, o2) -> o1));

    public static boolean canConvertToJSON(@NotNull PsiElement psiElement) {
        return LANGUAGE_RESOLVER_MAP.containsKey(psiElement.getLanguage())
                && LANGUAGE_RESOLVER_MAP.get(psiElement.getLanguage()).canConvertToJSON(psiElement);
    }

    public static String convertClassToJSON(@NotNull PsiElement psiElement) {
        if (LANGUAGE_RESOLVER_MAP.containsKey(psiElement.getLanguage())) {
            return LANGUAGE_RESOLVER_MAP.get(psiElement.getLanguage()).convertToJSON(psiElement);
        }
        return null;
    }

    /**
     * @param psiElement
     * @deprecated It's looks awful
     */
    @Deprecated
    public static boolean canNavigateToTree(@NotNull PsiElement psiElement) {
//        return LANGUAGE_RESOLVER_MAP.containsKey(psiElement.getLanguage())
//                && LANGUAGE_RESOLVER_MAP.get(psiElement.getLanguage()).canNavigateToTree(psiElement);
        return RequestHelper.canNavigateToTree(psiElement);
    }

    /**
     * 能否生成line marker图标，用于跳转
     *
     * @param psiElement 鼠标所在的元素
     * @deprecated It's looks awful
     */
    @Deprecated
    public static boolean canGenerateLineMarker(@NotNull PsiElement psiElement) {
//        return LANGUAGE_RESOLVER_MAP.containsKey(psiElement.getLanguage())
//                && LANGUAGE_RESOLVER_MAP.get(psiElement.getLanguage()).canGenerateLineMarker(psiElement);
        return RequestHelper.canGenerateLineMarker(psiElement);
    }

    /**
     * 生成RestItem
     *
     * @param psiElement 鼠标所在的元素
     * @deprecated It's looks awful
     */
    @Deprecated
    public static RestItem generateRestItem(@NotNull PsiElement psiElement) {
//        if (LANGUAGE_RESOLVER_MAP.containsKey(psiElement.getLanguage())) {
//            RestItem restItem = LANGUAGE_RESOLVER_MAP.get(psiElement.getLanguage()).tryGenerateRestItem(psiElement);
//            if (restItem != null) {
//                restItem.setProject(psiElement.getProject().getName());
//            }
//            return restItem;
//        }
        return RequestHelper.generateRestItem(psiElement);
    }
}

package io.github.newhoo.restkit.restful;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import io.github.newhoo.restkit.common.PsiRestItem;
import org.jetbrains.annotations.NotNull;

/**
 * resolver for some operations associated with language
 *
 * @author huzunrong
 * @since 2.0.1
 */
public interface LanguageResolver {

    /**
     * idea中默认提供JavaLanguage、KotlinLanguage
     */
    @NotNull
    Language getLanguage();

    /**
     * 能否转成json
     *
     * @param psiElement
     */
    boolean canConvertToJSON(@NotNull PsiElement psiElement);

    /**
     * 转json
     * 如果上一步返回false，则不会调用到此方法
     *
     * @param psiElement
     */
    String convertToJSON(@NotNull PsiElement psiElement);

    /**
     * 能否从代码方法跳转到tree
     * <p>
     * 实际上是 {@link PsiRestItem#psiElement} == psiElement
     *
     * @param psiElement 鼠标所在的元素
     */
    boolean canNavigateToTree(@NotNull PsiElement psiElement);
}
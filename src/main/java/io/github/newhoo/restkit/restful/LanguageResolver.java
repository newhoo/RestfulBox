package io.github.newhoo.restkit.restful;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.PsiRestItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    /**
     * 根据 psiElement 构造Header
     * <p>
     * return format:
     * <pre>
     *     header1: value1
     *     header2: value2
     * </pre>
     *
     * @param psiElement {@link PsiRestItem#psiElement}
     */
    @NotNull
    List<KV> buildHeaders(@NotNull PsiElement psiElement);

    /**
     * 根据 psiElement 构造Param: URL参数
     * <p>
     * return format:
     * <pre>
     *     key1: value1
     *     key2: value2
     * </pre>
     *
     * @param psiElement {@link PsiRestItem#psiElement}
     */
    @NotNull
    List<KV> buildParams(@NotNull PsiElement psiElement);

    /**
     * 根据 psiElement 构造request body
     *
     * @param psiElement {@link PsiRestItem#psiElement}
     * @return json string
     */
    @NotNull
    String buildRequestBodyJson(@NotNull PsiElement psiElement);

    /**
     * 根据 psiElement 构造request description
     *
     * @param psiElement {@link PsiRestItem#psiElement}
     * @return json string
     */
    @NotNull
    String buildDescription(@NotNull PsiElement psiElement);
}
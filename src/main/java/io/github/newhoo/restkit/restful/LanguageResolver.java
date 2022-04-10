package io.github.newhoo.restkit.restful;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import io.github.newhoo.restkit.common.PsiRestItem;
import io.github.newhoo.restkit.common.RestItem;
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
     * <p>
     * 功能太弱了，不好区分不同框架，不建议外部使用
     *
     * @param psiElement 鼠标所在的元素
     */
    boolean canNavigateToTree(@NotNull PsiElement psiElement);

    /**
     * 能否生成line marker图标，图标跳转
     *
     * @param psiElement 每行循环的元素
     */
    boolean canGenerateLineMarker(@NotNull PsiElement psiElement);

    /**
     * 根据PsiElement生成RestItem
     * <p>
     * 跳转到树节点不存在时，直接用RestItem在rest client中生成请求
     * <p>
     * 只要扫描写得好，一般用不到！！
     *
     * @param psiElement 每行循环的元素
     */
    RestItem tryGenerateRestItem(@NotNull PsiElement psiElement);
}
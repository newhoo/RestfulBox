package io.github.newhoo.restkit.restful;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.common.RestItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * resolve restful apis in project
 *
 * @author huzunrong
 * @since 2.0.1
 */
@NotProguard
public interface RequestResolver {

    /**
     * supported framework
     *
     * @return not empty
     */
    @NotNull
    String getFrameworkName();

    /**
     * scan type
     * SCANNER - spring mvc/structs
     * STORAGE - Local Store/Redis Store
     */
    default ScanType getScanType() {
        return ScanType.SCANNER;
    }

    /**
     * check relative config
     */
    default boolean checkConfig() {
        return true;
    }

    /**
     * find items in project
     */
    @NotNull
    List<RestItem> findRestItemInProject(@NotNull Project project);

    /**
     * 根据PsiElement生成RestItem
     * <p>
     * 跳转到树节点不存在时，直接用RestItem在rest client中生成请求
     * <p>
     * 只要扫描写得好，一般用不到！！
     *
     * @param psiElement 每行循环的元素
     * @since 3.3.0
     */
    default RestItem tryGenerateRestItem(@NotNull PsiElement psiElement) {
        return null;
    }

    /**
     * 能否从代码方法跳转到tree
     *
     * @param psiElement 鼠标所在的元素
     * @since 3.3.0
     */
    default boolean canNavigateToTree(@NotNull PsiElement psiElement) {
        return false;
    }

    /**
     * 能否生成line marker图标，图标跳转
     *
     * @param psiElement 每行循环的元素
     * @since 3.3.0
     */
    default boolean canGenerateLineMarker(@NotNull PsiElement psiElement) {
        return false;
    }

    /**
     * add api
     *
     * @param itemList not null
     * @deprecated try use io.github.newhoo.restkit.datasource.DataSource#addRestItem(java.util.List)
     */
    @Deprecated
    default void add(List<RestItem> itemList) {
    }

    /**
     * update api
     *
     * @param itemList not null
     * @deprecated try use io.github.newhoo.restkit.datasource.DataSource#updateRestItem(java.util.List)
     */
    @Deprecated
    default void update(List<RestItem> itemList) {
    }

    /**
     * delete api
     *
     * @param itemList not null
     * @deprecated try use io.github.newhoo.restkit.datasource.DataSource#addRestItem(java.util.List)
     */
    @Deprecated
    default void delete(List<RestItem> itemList) {
    }

    @NotProguard
    enum ScanType {
        SCANNER, STORAGE
    }
}
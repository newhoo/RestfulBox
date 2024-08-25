package io.github.newhoo.restkit.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.stubindex.KotlinAnnotationsIndex;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;

import java.util.Collection;

/**
 * 只能被Kotlin模块调用
 */
public class KtCompactHelper {

    public static Collection<KtAnnotationEntry> getAnnotations(@NotNull String s, @NotNull Project project, @NotNull GlobalSearchScope scope) {
//        return KotlinAnnotationsIndex.getInstance().get(s, project, scope);
        return KotlinAnnotationsIndex.Helper.get(s, project, scope);
    }
}

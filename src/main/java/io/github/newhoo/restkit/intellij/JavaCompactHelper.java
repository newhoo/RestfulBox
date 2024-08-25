package io.github.newhoo.restkit.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * 只能被Java模块调用
 */
public class JavaCompactHelper {

    public static Collection<PsiAnnotation> getAnnotations(@NotNull String s, @NotNull Project project, @NotNull GlobalSearchScope scope) {
//        return JavaAnnotationIndex.getInstance().get(s, project, scope);
        return JavaAnnotationIndex.getInstance().getAnnotations(s, project, scope);
    }
}

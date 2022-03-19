package io.github.newhoo.restkit.feature.javaimpl.helper;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReferenceExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * PsiAnnotationHelper in Java
 *
 * @author huzunrong
 * @since 2.0.0
 */
public class PsiAnnotationHelper {

    /**
     * 获取class注解
     *
     * @param psiClass
     * @param fqn
     */
    public static PsiAnnotation getInheritedAnnotation(PsiClass psiClass, String fqn) {
        if (psiClass == null) {
            return null;
        }
        PsiAnnotation annotation = psiClass.getAnnotation(fqn);
        if (annotation != null) {
            return annotation;
        }
        for (PsiClass aSuper : psiClass.getSupers()) {
            if (!"java.lang.Object".equals(aSuper.getQualifiedName())) {
                PsiAnnotation superClassAnno = getInheritedAnnotation(aSuper, fqn);
                if (superClassAnno != null) {
                    return superClassAnno;
                }
            }
        }
        return null;
    }

    /**
     * 获取method注解
     *
     * @param psiMethod
     * @param fqn
     */
    public static PsiAnnotation getInheritedAnnotation(PsiMethod psiMethod, String fqn) {
        PsiAnnotation annotation = psiMethod.getAnnotation(fqn);
        if (annotation != null) {
            return annotation;
        }
        for (PsiMethod aSuper : psiMethod.findSuperMethods()) {
            PsiAnnotation superMethodAnnotation = getInheritedAnnotation(aSuper, fqn);
            if (superMethodAnnotation != null) {
                return superMethodAnnotation;
            }
        }
        return null;
    }

    @NotNull
    public static List<String> getAnnotationAttributeValues(PsiAnnotation annotation, String attr) {
        PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue(attr);

        List<String> values = new ArrayList<>();
        //只有注解
        //一个值 class com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
        //多个值  class com.intellij.psi.impl.source.tree.java.PsiArrayInitializerMemberValueImpl
        if (value instanceof PsiReferenceExpression) {
            PsiReferenceExpression expression = (PsiReferenceExpression) value;
            values.add(expression.getText());
        } else if (value instanceof PsiLiteralExpression) {
//            values.add(psiNameValuePair.getLiteralValue());
            values.add(((PsiLiteralExpression) value).getValue().toString());
        } else if (value instanceof PsiArrayInitializerMemberValue) {
            PsiAnnotationMemberValue[] initializers = ((PsiArrayInitializerMemberValue) value).getInitializers();

            for (PsiAnnotationMemberValue initializer : initializers) {
                values.add(initializer.getText().replaceAll("\"", ""));
            }
        }

        return values;
    }

    public static String getAnnotationValue(PsiAnnotation annotation, String attributeName) {
        String paramName = null;
        PsiAnnotationMemberValue attributeValue = annotation.findDeclaredAttributeValue(attributeName);

        if (attributeValue instanceof PsiLiteralExpression) {
            paramName = (String) ((PsiLiteralExpression) attributeValue).getValue();
        }
        return paramName;
    }
}

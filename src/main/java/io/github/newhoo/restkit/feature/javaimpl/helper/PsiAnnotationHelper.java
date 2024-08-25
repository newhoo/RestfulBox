package io.github.newhoo.restkit.feature.javaimpl.helper;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportStatementBase;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiPolyadicExpression;
import com.intellij.psi.PsiReferenceExpression;
import org.apache.commons.lang3.StringUtils;
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
        List<String> values = new ArrayList<>();
        PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue(attr);
        if (value == null) {
            return values;
        }

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
        } else if (value instanceof PsiPolyadicExpression) {
            String s = "";
            try {
                for (PsiElement child : value.getChildren()) {
                    if (child instanceof PsiLiteralExpression) {
                        s += ((PsiLiteralExpression) child).getValue().toString();
                        continue;
                    }
                    // 计算常量，最多支持两级内部类常量
                    PsiFile containingFile = child.getContainingFile();
                    if (child instanceof PsiReferenceExpression && containingFile instanceof PsiJavaFile && ((PsiJavaFile) containingFile).getImportList() != null) {
                        PsiClass referedConstClass = null;
                        String childText = child.getText();
                        if (!childText.contains(".")) {
                            PsiImportStatementBase singleImportStatement = ((PsiJavaFile) containingFile).getImportList().findSingleImportStatement(childText);
                            if (singleImportStatement != null && singleImportStatement.getImportReference() != null) {
                                String qualifiedName = singleImportStatement.getImportReference().getQualifiedName();
                                String referConstClassQname = qualifiedName.substring(0, qualifiedName.indexOf(childText) - 1);
                                referedConstClass = PsiClassHelper.findPsiClass(referConstClassQname, annotation.getProject());
                            }
                        } else {
                            String s1 = childText.substring(0, childText.lastIndexOf("."));
                            if (!s1.contains(".")) {
                                PsiImportStatementBase singleImportStatement = ((PsiJavaFile) containingFile).getImportList().findSingleImportStatement(s1);
                                if (singleImportStatement != null && singleImportStatement.getImportReference() != null) {
                                    String referConstClassQname = singleImportStatement.getImportReference().getQualifiedName();
                                    referedConstClass = PsiClassHelper.findPsiClass(referConstClassQname, annotation.getProject());
                                }
                            } else {
                                // com.example.constants.C.PREFIX1
                                PsiClass psiClass = PsiClassHelper.findPsiClass(s1, annotation.getProject());
                                if (psiClass != null) {
                                    referedConstClass = psiClass;
                                } else {
                                    // 内部类
                                    String s2 = s1.substring(0, s1.lastIndexOf("."));
                                    if (!s2.contains(".")) {
                                        PsiImportStatementBase singleImportStatement = ((PsiJavaFile) containingFile).getImportList().findSingleImportStatement(s2);
                                        if (singleImportStatement != null && singleImportStatement.getImportReference() != null) {
                                            String referConstClassQname = singleImportStatement.getImportReference().getQualifiedName();
                                            referedConstClass = PsiClassHelper.findPsiClass(referConstClassQname + s1.substring(s1.lastIndexOf(".")), annotation.getProject());
                                        }
                                    } else {
                                        psiClass = PsiClassHelper.findPsiClass(s2, annotation.getProject());
                                        if (psiClass != null) {
                                            for (PsiClass innerClass : psiClass.getInnerClasses()) {
                                                if (innerClass.getName() != null && innerClass.getName().equals(s1.substring(s1.lastIndexOf("." + 1)))) {
                                                    referedConstClass = innerClass;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 引用的常量所在类
                        if (referedConstClass == null) {
                            referedConstClass = ((PsiJavaFile) containingFile).getClasses()[0];
                        }
                        String cValue = "{{" + childText + "}}";
                        for (PsiField field : referedConstClass.getFields()) {
                            if (childText.endsWith(field.getName())) {
                                cValue = String.valueOf(field.computeConstantValue());
                                break;
                            }
                        }
                        s += cValue;
                    }
                }
                values.add(s);
            } catch (Exception e) {
            }
        }

        return values;
    }

    @NotNull
    public static String getAnnotationValue(PsiAnnotation annotation, String attributeName) {
        if (annotation == null) {
            return "";
        }
        String paramName = null;
        PsiAnnotationMemberValue attributeValue = annotation.findDeclaredAttributeValue(attributeName);

        if (attributeValue instanceof PsiLiteralExpression) {
            paramName = (String) ((PsiLiteralExpression) attributeValue).getValue();
        }
        return StringUtils.defaultString(paramName);
    }
}

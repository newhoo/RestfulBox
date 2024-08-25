package io.github.newhoo.restkit.feature.javaimpl.jaxrs;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.feature.javaimpl.MethodPath;
import io.github.newhoo.restkit.feature.javaimpl.helper.PsiAnnotationHelper;
import io.github.newhoo.restkit.restful.BaseRequestResolver;
import io.github.newhoo.restkit.restful.ParamResolver;
import io.github.newhoo.restkit.util.TypeUtils;

import java.util.Optional;

import static io.github.newhoo.restkit.common.RestConstant.WEB_FRAMEWORK_JAX_RS;

public abstract class BaseJaxrsResolver extends BaseRequestResolver implements ParamResolver {

    @Override
    public String getFrameworkName() {
        return WEB_FRAMEWORK_JAX_RS;
    }

    protected Optional<KV> getParamKv(PsiParameter psiParameter, JaxrsAnnotation.ParamAnnotation annotation) {
        PsiAnnotation headerAnno = getPsiAnnotation(psiParameter, annotation);
        if (headerAnno != null) {
            String paramName = PsiAnnotationHelper.getAnnotationValue(headerAnno, "value");
            String value = "";
            PsiAnnotation headerDefaultAnno = getPsiAnnotation(psiParameter, JaxrsAnnotation.ParamAnnotation.DEFAULT_VALUE);
            if (headerDefaultAnno != null) {
                value = PsiAnnotationHelper.getAnnotationValue(headerDefaultAnno, "value");
            } else {
                value = String.valueOf(TypeUtils.getExampleValue(psiParameter.getType().getPresentableText(), psiParameter.getProject()));
            }
            return Optional.of(new KV(paramName, value));
        }
        return Optional.empty();
    }

    protected PsiAnnotation getPsiAnnotation(PsiParameter psiParameter, JaxrsAnnotation.ParamAnnotation paramAnnotation) {
        PsiAnnotation annotation = psiParameter.getAnnotation(paramAnnotation.getQualifiedName());
        if (annotation == null) {
            annotation = psiParameter.getAnnotation(paramAnnotation.getQualifiedName2());
        }
        return annotation;
    }

    protected MethodPath getMethodPath(PsiMethod psiMethod) {
        PsiAnnotation wsPathAnnotation = psiMethod.getModifierList().findAnnotation(JaxrsAnnotation.PathAnnotation.PATH.getQualifiedName());
        if (wsPathAnnotation == null) {
            wsPathAnnotation = psiMethod.getModifierList().findAnnotation(JaxrsAnnotation.PathAnnotation.PATH.getQualifiedName2());
        }
        String path = wsPathAnnotation == null ? "" : PsiAnnotationHelper.getAnnotationValue(wsPathAnnotation, "value");

        PsiAnnotation[] annotations = psiMethod.getModifierList().getAnnotations();
        JaxrsAnnotation.MethodAnnotation[] jaxrsHttpMethodAnnotations = JaxrsAnnotation.MethodAnnotation.values();
        for (PsiAnnotation annotation : annotations) {
            for (JaxrsAnnotation.MethodAnnotation methodAnnotation : jaxrsHttpMethodAnnotations) {
                if (methodAnnotation.getQualifiedName().equals(annotation.getQualifiedName()) || methodAnnotation.getQualifiedName2().equals(annotation.getQualifiedName())) {
                    return new MethodPath(path, methodAnnotation.name());
                }
            }
        }
        return null;
    }
}

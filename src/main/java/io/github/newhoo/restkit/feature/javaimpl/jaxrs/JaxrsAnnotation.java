package io.github.newhoo.restkit.feature.javaimpl.jaxrs;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JaxrsAnnotation {

    @Getter
    @AllArgsConstructor
    public enum PathAnnotation {

        PATH("Path", "javax.ws.rs.Path", "jakarta.ws.rs.Path");

        private final String shortName;
        private final String qualifiedName;
        private final String qualifiedName2;
    }

    @Getter
    @AllArgsConstructor
    public enum ParamAnnotation {

        PATH_PARAM("javax.ws.rs.PathParam", "jakarta.ws.rs.PathParam"),
        QUERY_PARAM("javax.ws.rs.QueryParam", "jakarta.ws.rs.QueryParam"),
        FORM_PARAM("javax.ws.rs.FormParam", "jakarta.ws.rs.FormParam"),
        HEADER_PARAM("javax.ws.rs.HeaderParam", "jakarta.ws.rs.HeaderParam"),
        MATRIX_PARAM("javax.ws.rs.MatrixParam", "jakarta.ws.rs.MatrixParam"),
        COOKIE_PARAM("javax.ws.rs.CookieParam", "jakarta.ws.rs.CookieParam"),
        BEAN_PARAM("javax.ws.rs.BeanParam", "jakarta.ws.rs.BeanParam"),
        DEFAULT_VALUE("javax.ws.rs.DefaultValue", "jakarta.ws.rs.DefaultValue"),
        CONTEXT("javax.ws.rs.core.Context", "jakarta.ws.rs.core.Context");

        private final String qualifiedName;
        private final String qualifiedName2;

        public static Set<String> getByQualifiedNameSet() {
            return Arrays.stream(ParamAnnotation.values())
                         .map(o -> Stream.of(o.qualifiedName, o.getQualifiedName2()))
                         .flatMap((Function<Stream<String>, Stream<String>>) stringStream -> stringStream)
                         .collect(Collectors.toSet());
        }
    }

    @Getter
    @AllArgsConstructor
    public enum MethodAnnotation {

        GET("javax.ws.rs.GET", "jakarta.ws.rs.GET"),
        POST("javax.ws.rs.POST", "jakarta.ws.rs.POST"),
        PUT("javax.ws.rs.PUT", "jakarta.ws.rs.PUT"),
        DELETE("javax.ws.rs.DELETE", "jakarta.ws.rs.DELETE"),
        HEAD("javax.ws.rs.HEAD", "jakarta.ws.rs.HEAD"),
        PATCH("javax.ws.rs.PATCH", "jakarta.ws.rs.PATCH");

        private final String qualifiedName;
        private final String qualifiedName2;

        public static MethodAnnotation getByQualifiedName(String qualifiedName) {
            for (MethodAnnotation anno : MethodAnnotation.values()) {
                if (anno.qualifiedName.equals(qualifiedName) || anno.qualifiedName2.equals(qualifiedName)) {
                    return anno;
                }
            }
            return null;
        }
    }
}

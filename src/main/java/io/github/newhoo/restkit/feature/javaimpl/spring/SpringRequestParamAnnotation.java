package io.github.newhoo.restkit.feature.javaimpl.spring;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpringRequestParamAnnotation {
    REQUEST_PARAM("RequestParam", "org.springframework.web.bind.annotation.RequestParam"),
    REQUEST_BODY("RequestBody", "org.springframework.web.bind.annotation.RequestBody"),
    PATH_VARIABLE("PathVariable", "org.springframework.web.bind.annotation.PathVariable"),
    REQUEST_HEADER("RequestHeader", "org.springframework.web.bind.annotation.RequestHeader");

    private final String shortName;
    private final String qualifiedName;
}
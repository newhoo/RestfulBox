package io.github.newhoo.restkit.feature.javaimpl;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MethodPath
 */
@Getter
@AllArgsConstructor
public class MethodPath {
    private final String path;
    private final String method;
}

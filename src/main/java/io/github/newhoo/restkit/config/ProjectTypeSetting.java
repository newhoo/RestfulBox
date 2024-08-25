package io.github.newhoo.restkit.config;

import io.github.newhoo.restkit.common.NotProguard;
import lombok.Data;

@NotProguard
@Data
public class ProjectTypeSetting {

    private String project;
    private String type;
    private String content;
}

package io.github.newhoo.restkit.common;

import lombok.Data;

/**
 * ProjectInfo
 *
 * @author huzunrong
 * @since 3.0.1
 */
@NotProguard
@Data
public class ProjectInfo {

    /**
     * project name
     */
    private String project;
    private long countApi;
    private long countSetting;
    private long countEnv;
    private long countHeader;
    private long countParameter;
//    private String desc;
}

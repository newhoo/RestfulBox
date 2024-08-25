package io.github.newhoo.restkit.config;

import io.github.newhoo.restkit.common.NotProguard;
import lombok.Data;

/**
 * common setting
 *
 * @author huzunrong
 * @since 2.0.0
 */
@NotProguard
@Data
public class RequestSetting {

    private String project;

    private boolean saveRequestLog = true;
    private boolean enablePreRequestScript = true;
    private boolean enablePostRequestScript = true;
    private String preRequestScriptPath = "";
    private String postRequestScriptPath = "";
    private String preRequestScriptContent = "";
    private String postRequestScriptContent = "";
    // 脚本内容最后更新时间
    private String preRequestScriptContentLastModifiedTime = "";
    private String postRequestScriptContentLastModifiedTime = "";

    // HTTP
    private int requestTimeout = 0;
    private boolean generateMultilineCurlSnippet = false;
    private boolean supportForWslPath = false;
    private boolean supportMinifyJson = false;
    private String downloadDirectory = "";

    // Other
    private String defaultDateFormat;
}

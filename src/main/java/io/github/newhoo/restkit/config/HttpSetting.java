package io.github.newhoo.restkit.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * http setting
 *
 * @author huzunrong
 * @since 2.0.8
 */
@Data
public class HttpSetting {

    private int requestTimeout = 0;

    private boolean generateMultilineCurlSnippet = false;
    private boolean supportForWslPath = false;

    private String downloadDirectory = "";

    public boolean isModified(HttpSetting modifiedSetting) {
        return requestTimeout != modifiedSetting.getRequestTimeout()
                || generateMultilineCurlSnippet != modifiedSetting.isGenerateMultilineCurlSnippet()
                || supportForWslPath != modifiedSetting.isSupportForWslPath()
                || !StringUtils.equals(downloadDirectory, modifiedSetting.getDownloadDirectory());
    }
}

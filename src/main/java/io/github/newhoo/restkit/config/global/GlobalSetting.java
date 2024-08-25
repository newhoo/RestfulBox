package io.github.newhoo.restkit.config.global;

import io.github.newhoo.restkit.common.NotProguard;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@NotProguard
@Data
public class GlobalSetting {

    private Map<String, String> projectCurrentEnvMap = new LinkedHashMap<>();

    private String language = "en"; // zh_CN
    private boolean showTreeRoot = true;
    private boolean showMultiLevelServiceNode = true;
    private boolean showModuleInSearchEvery = false;
    private boolean enableFilterDescriptionInSearchEvery = false;
    private boolean showCompletePackageName = false;
    private boolean displayTreeListUsingApiDesc = false;
    private boolean enableParameterLibrary = true;
    private boolean enableParameterLibraryShortcut = true;
    private boolean enableMethodLineMarker = true;
    private boolean enableMultiRequestClient = true;
}

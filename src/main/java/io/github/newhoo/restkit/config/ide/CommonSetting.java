package io.github.newhoo.restkit.config.ide;

import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.NotProguard;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * common setting
 *
 * @author huzunrong
 * @since 2.0.0
 */
@NotProguard
@Data
public class CommonSetting {

    private Set<String> disabledWebFrameworks = new HashSet<>();
    private Set<HttpMethod> filterMethods = new HashSet<>();

    private boolean scanServiceWithLib = false;

    private String apiFilePath = "";
}

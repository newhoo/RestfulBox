package io.github.newhoo.restkit.feature.javaimpl.config;

import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.datasource.DataSource;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.util.JsonUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static io.github.newhoo.restkit.common.RestConstant.DATA_SOURCE_IDE;

/**
 * 过滤参数
 *
 * @author huzunrong
 * @since 2.0.3 update 3.2.1
 */
@Data
public class JavaFilterSetting {

    @NotProguard
    private Set<String> filterClassQualifiedNames = new LinkedHashSet<>(Arrays.asList(
            "org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController",
            "org.springframework.boot.actuate.autoconfigure.web.servlet.ManagementErrorEndpoint"
    ));
    @NotProguard
    private Set<String> fileParameterQualifiedNames = new LinkedHashSet<>(Arrays.asList(
            "org.springframework.web.multipart.MultipartFile"
    ));
    @NotProguard
    private Set<String> filterParamQualifiedNames = new LinkedHashSet<>(Arrays.asList(
            "org.springframework.web.bind.annotation.RequestHeader",
            "org.springframework.web.bind.annotation.RequestBody",
            "javax.servlet.http.HttpServletRequest",
            "javax.servlet.http.HttpServletResponse",
            "org.springframework.web.context.request.ServletWebRequest",
            "org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput"
    ));

    public static JavaFilterSetting getInstance() {
        DataSource dataSource = DataSourceHelper.getDataSource();
        String content = dataSource.selectSetting("-", getType(dataSource.name()), null);
        if (StringUtils.isNotEmpty(content)) {
            return JsonUtils.fromJson(content, JavaFilterSetting.class);
        }
        return new JavaFilterSetting();
    }

    public static String getType(String dataSourceName) {
        String type = "FILTER";
        if (DATA_SOURCE_IDE.equals(dataSourceName)) {
            type = "java-filter.json";
        }
        return type;
    }
}

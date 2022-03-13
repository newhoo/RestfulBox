package io.github.newhoo.restkit.restful;

import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.CommonSetting;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.restful.ep.RestClientProvider;
import io.github.newhoo.restkit.restful.ep.RestfulResolverProvider;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 服务相关工具类
 *
 * @author huzunrong
 */
public class RequestHelper {

    public static List<RequestResolver> getAllRequestResolvers(@NotNull Project project) {
        return RestfulResolverProvider.EP_NAME.getExtensionList()
                                              .stream()
                                              .filter(Objects::nonNull)
                                              .map(restfulResolverProvider -> restfulResolverProvider.createRequestResolver(project))
                                              .filter(Objects::nonNull)
                                              .sorted(Comparator.comparing(RequestResolver::getScanType))
                                              .collect(Collectors.toList());
    }

    public static List<RequestResolver> getEnabledRequestResolvers(@NotNull Project project) {
        CommonSetting commonSetting = CommonSettingComponent.getInstance(project).getState();
        Set<String> enabledWebFrameworks = commonSetting.getEnabledWebFrameworks();
        return RestfulResolverProvider.EP_NAME.getExtensionList()
                                              .stream()
                                              .filter(Objects::nonNull)
                                              .map(restfulResolverProvider -> restfulResolverProvider.createRequestResolver(project))
                                              .filter(Objects::nonNull)
                                              .filter(o -> enabledWebFrameworks.contains(o.getFrameworkName()))
                                              .filter(RequestResolver::checkConfig)
                                              .sorted(Comparator.comparing(RequestResolver::getScanType))
                                              .collect(Collectors.toList());
    }

    public static RestClient getRestClient(@NotNull String protocol, @NotNull Supplier<RestClient> defaultClient) {
        return RestClientProvider.EP_NAME.getExtensionList()
                                         .stream()
                                         .filter(Objects::nonNull)
                                         .map(RestClientProvider::createClient)
                                         .filter(Objects::nonNull)
                                         .filter(client -> client.getProtocol().equalsIgnoreCase(protocol)
                                                 || client.getProtocol().equalsIgnoreCase(protocol + "s")
                                                 || (client.getProtocol() + "s").equalsIgnoreCase(protocol))
                                         .findFirst()
                                         .orElseGet(defaultClient);
    }

    public static List<RestClient> getRestClient() {
        return RestClientProvider.EP_NAME.getExtensionList()
                                         .stream()
                                         .filter(Objects::nonNull)
                                         .map(RestClientProvider::createClient)
                                         .filter(Objects::nonNull)
                                         .filter(restClient -> StringUtils.isNotEmpty(restClient.getProtocol()))
                                         .collect(Collectors.toList());
    }

    public static List<RestItem> buildRequestItemList(@NotNull Project project) {
        List<RequestResolver> requestResolvers = getEnabledRequestResolvers(project);
        return requestResolvers.stream()
                               .map(resolver -> resolver.findRestItemInProject(project))
                               .filter(Objects::nonNull)
                               .flatMap(Collection::stream)
                               .filter(Objects::nonNull)
                               .filter(item -> item.getModuleName() != null && item.getUrl() != null)
                               .collect(Collectors.toList());
    }

    @NotNull
    public static String getCombinedPath(@NotNull String typePath, @NotNull String methodPath) {
        if (typePath.isEmpty()) {
            typePath = "/";
        } else if (!typePath.startsWith("/")) {
            typePath = "/".concat(typePath);
        }

        if (!methodPath.isEmpty()) {
            if (!methodPath.startsWith("/") && !typePath.endsWith("/")) {
                methodPath = "/".concat(methodPath);
            }
        }

        return (typePath + methodPath).replace("//", "/");
    }
}

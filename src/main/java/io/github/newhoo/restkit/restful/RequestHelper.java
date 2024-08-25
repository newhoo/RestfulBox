package io.github.newhoo.restkit.restful;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.common.RestConstant;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.ConfigHelper;
import io.github.newhoo.restkit.config.ide.CommonSetting;
import io.github.newhoo.restkit.datasource.DataSource;
import io.github.newhoo.restkit.restful.ep.RestClientProvider;
import io.github.newhoo.restkit.restful.ep.RestItemDetailProvider;
import io.github.newhoo.restkit.restful.ep.RestfulResolverProvider;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 服务相关工具类
 *
 * @author huzunrong
 */
@NotProguard
public class RequestHelper {

    private static final Map<String, RestItemDetail> REST_ITEM_DETAIL_MAP = RestItemDetailProvider.EP_NAME.getExtensionList()
                                                                                                          .stream()
                                                                                                          .filter(Objects::nonNull)
                                                                                                          .map(RestItemDetailProvider::createRestItemDetail)
                                                                                                          .filter(Objects::nonNull)
                                                                                                          .collect(Collectors.toMap(RestItemDetail::getProtocol, o -> o, (o1, o2) -> o1));

    public static List<RequestResolver> getAllScanRequestResolvers(@NotNull Project project) {
        return RestfulResolverProvider.EP_NAME.getExtensionList()
                                              .stream()
                                              .filter(Objects::nonNull)
                                              .map(restfulResolverProvider -> restfulResolverProvider.createRequestResolver(project))
                                              .filter(Objects::nonNull)
                                              .filter(o -> o.getScanType() == RequestResolver.ScanType.SCANNER)
                                              .filter(o -> !o.getFrameworkName().isEmpty())
                                              .filter(RequestResolver::checkConfig)
                                              .collect(Collectors.toList());
    }

    public static List<RequestResolver> getAllStorageRequestResolvers(@NotNull Project project) {
        return RestfulResolverProvider.EP_NAME.getExtensionList()
                                              .stream()
                                              .filter(Objects::nonNull)
                                              .map(restfulResolverProvider -> restfulResolverProvider.createRequestResolver(project))
                                              .filter(Objects::nonNull)
                                              .filter(o -> o.getScanType() == RequestResolver.ScanType.STORAGE)
                                              .filter(o -> !o.getFrameworkName().isEmpty())
                                              .filter(RequestResolver::checkConfig)
                                              .collect(Collectors.toList());
    }

    /**
     * ScanType.STORAGE默认启用
     */
    private static List<RequestResolver> getEnabledRequestResolvers(@NotNull Project project) {
        CommonSetting commonSetting = ConfigHelper.getCommonSetting(project);
        Set<String> disabledWebFrameworks = commonSetting.getDisabledWebFrameworks();
        return RestfulResolverProvider.EP_NAME.getExtensionList()
                                              .stream()
                                              .filter(Objects::nonNull)
                                              .map(restfulResolverProvider -> restfulResolverProvider.createRequestResolver(project))
                                              .filter(Objects::nonNull)
                                              .filter(o -> o.getScanType() == RequestResolver.ScanType.STORAGE || !disabledWebFrameworks.contains(o.getFrameworkName()))
                                              .filter(o -> !(o instanceof DataSource))
                                              .filter(RequestResolver::checkConfig)
                                              .collect(Collectors.toList());
    }

    public static RestClient getRestClient(@NotNull String protocol, @NotNull Supplier<RestClient> defaultClient) {
        return RestClientProvider.EP_NAME.getExtensionList()
                                         .stream()
                                         .filter(Objects::nonNull)
                                         .map(RestClientProvider::createClient)
                                         .filter(Objects::nonNull)
                                         .filter(client -> client.getProtocol().equalsIgnoreCase(protocol))
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

    public static RestItemDetail getRestItemDetail(@NotNull String protocol) {
        if (RestConstant.PROTOCOL_HTTP.equals(protocol)) {
            return REST_ITEM_DETAIL_MAP.get(protocol);
        }
        return REST_ITEM_DETAIL_MAP.keySet().stream()
                                   .filter(s -> s.equalsIgnoreCase(protocol))
                                   .findFirst()
                                   .map(REST_ITEM_DETAIL_MAP::get)
                                   .orElse(null);
    }

    public static int getProtocolOrder(@NotNull String protocol) {
        RestItemDetail restItemDetail = getRestItemDetail(protocol);
        if (restItemDetail != null) {
            return restItemDetail.getProtocolOrder();
        }
        return 999;
    }

    public static List<RestItem> buildRequestItemList(@NotNull Project project) {
        // 当前项目，一般为扫描类型，可注册多个
        List<RequestResolver> requestResolvers = getEnabledRequestResolvers(project);
        List<RestItem> restItemListFromResolver = requestResolvers.stream()
                                                                  .map(resolver -> resolver.findRestItemInProject(project))
                                                                  .flatMap(Collection::stream)
                                                                  .filter(Objects::nonNull)
                                                                  .peek(item -> {
                                                                      if (StringUtils.isEmpty(item.getProject())) {
                                                                          item.setProject(project.getName());
                                                                      }
                                                                  })
                                                                  .collect(Collectors.toList());

        // 合并
        return Stream.of(restItemListFromResolver)
                     .flatMap(Collection::stream)
                     .filter(item -> item.getModuleName() != null && item.getUrl() != null)
                     .collect(Collectors.groupingBy(RestItem::getProject, Collectors.toList()))
                     .entrySet()
                     .stream()
                     .sorted((o1, o2) -> (project.getName().equals(o1.getKey())) ? -1 : o1.getKey().compareToIgnoreCase(o2.getKey()))
                     .map(entry -> {
                         entry.getValue().sort(new Comparator<RestItem>() {
                                     @Override
                                     public int compare(RestItem o1, RestItem o2) {
                                         return o1.getModuleName().compareToIgnoreCase(o2.getModuleName());
                                     }
                                 }.thenComparingInt(o -> getProtocolOrder(o.getProtocol()))
                                  .thenComparing((o1, o2) -> o1.getPackageName().compareToIgnoreCase(o2.getPackageName()))
                         );
                         return entry.getValue();
                     })
                     .flatMap(Collection::stream)
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

    /**
     * @param psiElement
     */
    public static boolean canNavigateToTree(@NotNull PsiElement psiElement) {
        return RequestHelper.getAllScanRequestResolvers(psiElement.getProject())
                            .stream()
                            .anyMatch(requestResolver -> requestResolver.canNavigateToTree(psiElement));
    }

    /**
     * 能否生成line marker图标，用于跳转
     *
     * @param psiElement 鼠标所在的元素
     */
    public static boolean canGenerateLineMarker(@NotNull PsiElement psiElement) {
        return RequestHelper.getAllScanRequestResolvers(psiElement.getProject())
                            .stream()
                            .anyMatch(requestResolver -> requestResolver.canGenerateLineMarker(psiElement));
    }

    /**
     * 生成RestItem
     *
     * @param psiElement 鼠标所在的元素
     */
    public static RestItem generateRestItem(@NotNull PsiElement psiElement) {
        return RequestHelper.getAllScanRequestResolvers(psiElement.getProject())
                            .stream()
                            .map(requestResolver -> requestResolver.tryGenerateRestItem(psiElement))
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null);
    }
}

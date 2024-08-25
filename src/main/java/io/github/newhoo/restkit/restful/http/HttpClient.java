package io.github.newhoo.restkit.restful.http;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.Request;
import io.github.newhoo.restkit.common.RequestInfo;
import io.github.newhoo.restkit.common.RestClientData;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.RequestSetting;
import io.github.newhoo.restkit.config.certificate.Certificate;
import io.github.newhoo.restkit.datasource.DataSource;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.restful.RestClient;
import io.github.newhoo.restkit.restful.ep.RestClientProvider;
import io.github.newhoo.restkit.util.FileUtils;
import io.github.newhoo.restkit.util.HttpUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicHeader;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.common.RestConstant.HTTP_BASE_URL;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_BASE_URL_DEFAULT;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_BASE_URL_PLACEHOLDER;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_DEFAULT_TIMEOUT;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_DOWNLOAD_FILEPATH_PREFIX;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_FILE_DOWNLOAD_DIRECTORY;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_P12_CONTENT;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_P12_PASSWD;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_P12_PATH;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_PROJECT;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_TIMEOUT;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_URL_HTTPS;
import static io.github.newhoo.restkit.common.RestConstant.PROTOCOL_HTTP;

/**
 * http client
 *
 * @author huzunrong
 * @date 2022/3/12 2:12 PM
 * @since 2.0.3
 */
public class HttpClient implements RestClient {

    private static final Logger LOG = Logger.getInstance(HttpClient.class);

    @NotNull
    @Override
    public String getProtocol() {
        return PROTOCOL_HTTP;
    }

    @Override
    public List<KV> getConfig(@NotNull RestItem restItem, @NotNull Project project) {
        RequestSetting setting = DataSourceHelper.getDataSource().selectRequestSetting(restItem.getProject(), project);
        int timeout = setting.getRequestTimeout();
        // 默认最大设置30s
        if (timeout <= 0) {
            timeout = HTTP_DEFAULT_TIMEOUT;
        }
        List<KV> list = new LinkedList<>();
        list.add(new KV(HTTP_BASE_URL, HTTP_BASE_URL_PLACEHOLDER));
        list.add(new KV(HTTP_TIMEOUT, String.valueOf(timeout)));
        return list;
    }

    @NotNull
    @Override
    public Request createRequest(RestClientData restClientData, Project project) {
        Map<String, String> config = restClientData.getConfig();
        config.putIfAbsent(HTTP_PROJECT, restClientData.getProject());
        String url = StringUtils.defaultString(restClientData.getUrl());
        if (!url.contains("://")) {
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            url = StringUtils.defaultIfEmpty(config.get(HTTP_BASE_URL), HTTP_BASE_URL_DEFAULT) + url;
            // 环境变量未设置【baseUrl】时强行替换为localhost:8080
            url = url.replace(HTTP_BASE_URL_PLACEHOLDER, HTTP_BASE_URL_DEFAULT);
        }

        HttpRequest request = new HttpRequest();
        request.setUrl(url);
        request.setMethod(restClientData.getMethod());
        request.setConfig(config);
        request.setHeaders(restClientData.getHeaders());
        request.setParams(restClientData.getParams());
        request.setBody(restClientData.getBody());
        return request;
    }

    @NotNull
    @Override
    public RequestInfo sendRequest(Request request, Project project) {
        DataSource dataSource = DataSourceHelper.getDataSource();
        RequestSetting setting = dataSource.selectRequestSetting(request.getConfig().get(HTTP_PROJECT), project);
        String downloadDirectory = setting.getDownloadDirectory();
        if (StringUtils.isEmpty(downloadDirectory)) {
            downloadDirectory = FileUtils.getDownloadDirectory(project);
        }
        request.getConfig().put(HTTP_FILE_DOWNLOAD_DIRECTORY, FileUtils.expandUserHome(downloadDirectory));

        // 设置证书
        String url = request.getUrl();
        if (StringUtils.startsWith(url, HTTP_URL_HTTPS)) {
            url = url.substring(8);
            String host = url.contains("/") ? url.substring(0, url.indexOf("/")) : url;
            Certificate cert = dataSource.selectEnabledCertificate(host, project);
            if (cert != null) {
                request.getConfig().put(HTTP_P12_PATH, FileUtils.expandUserHome(cert.getPfxFile()));
                request.getConfig().put(HTTP_P12_CONTENT, cert.getPfxFileContent());
                request.getConfig().put(HTTP_P12_PASSWD, cert.getPassphrase());
            }
        }
        return HttpUtils.request((HttpRequest) request);
    }

    @NotNull
    @Override
    public String formatResponseInfo(RequestInfo requestInfo) {
        HttpRequest request = (HttpRequest) requestInfo.getRequest();
        HttpResponse response = (HttpResponse) requestInfo.getResponse();

        StringBuilder sb = new StringBuilder();

        String status = "ERROR";
        if (response != null && response.getOriginal() != null) {
            StatusLine statusLine = response.getOriginal().getStatusLine();
            status = statusLine.getStatusCode() + " " + StringUtils.defaultString(statusLine.getReasonPhrase());
        }
        sb.append("Status: ").append(status).append("    ").append("Time: ").append(requestInfo.getCost()).append("ms").append("\n")
          .append("Remote Address: ").append(requestInfo.getRemoteAddress()).append("\n")
          .append("------------------------------------\n");

        if (request.getOriginal() != null) {
            sb.append(request.getOriginal().getRequestLine()).append("\n");
            List<Header> collect = Arrays.stream(request.getOriginal().getAllHeaders())
                                         .collect(Collectors.toList());
            if (request.getOriginal() instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request.getOriginal()).getEntity();
                collect.add(entity.getContentType());
                collect.add(new BasicHeader("Content-Length", String.valueOf(entity.getContentLength())));
            }
            String reqHeader = formatHeader(collect.toArray(new Header[0]));
            // String reqHeader = formatHeader(request.getOriginal().getAllHeaders());
            if (StringUtils.isNotEmpty(reqHeader)) {
                sb.append(reqHeader).append("\n");
            }
            if (StringUtils.isNotEmpty(request.getBody())) {
                sb.append("\n").append(request.getBody()).append("\n");
            } else if (request.getOriginal() instanceof HttpEntityEnclosingRequest) {
                try {
                    String s = "[multipart form data ...]";
                    HttpEntity entity = ((HttpEntityEnclosingRequest) request.getOriginal()).getEntity();
                    if (entity instanceof UrlEncodedFormEntity) {
                        s = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
                    }
                    sb.append("\n").append(s).append("\n");
                } catch (Exception e) {
                    LOG.warn("HttpEntityEnclosingRequest Content exception: " + e);
                }
            }
        }

        if (response != null && response.getOriginal() != null) {
            sb.append("\n");
            sb.append(response.getOriginal().getStatusLine()).append("\n");
            String respHeader = formatHeader(response.getOriginal().getAllHeaders());
            if (StringUtils.isNotEmpty(respHeader)) {
                sb.append(respHeader).append("\n");
            }
            if (StringUtils.startsWith(response.getBody(), HTTP_DOWNLOAD_FILEPATH_PREFIX)) {
                sb.append("\n").append("[file stream ...]").append("\n");
            } else if (StringUtils.isNotEmpty(response.getBody0())) {
                // 替换response内容\r\n的\r
                sb.append("\n").append(response.getBody0().replace("\r", "")).append("\n");
            }
        }
        return sb.toString();
    }

    @NotNull
    @Override
    public String formatLogInfo(RequestInfo requestInfo) {
        HttpRequest request = (HttpRequest) requestInfo.getRequest();
        HttpResponse response = (HttpResponse) requestInfo.getResponse();

        StringBuilder sb = new StringBuilder();
        sb.append("############################# ").append(LocalDateTime.now()).append(" #############################").append("\n");
        String status = "ERROR";
        if (response != null && response.getOriginal() != null) {
            StatusLine statusLine = response.getOriginal().getStatusLine();
            status = statusLine.getStatusCode() + " " + StringUtils.defaultString(statusLine.getReasonPhrase());
        }
        sb.append("Status: ").append(status).append("    ")
          .append("Time: ").append(requestInfo.getCost()).append("ms").append("    ")
          .append("Remote Address: ").append(requestInfo.getRemoteAddress()).append("\n\n");

        if (request.getOriginal() != null) {
            sb.append(">>> ").append(request.getOriginal().getRequestLine()).append("\n");
            List<Header> collect = Arrays.stream(request.getOriginal().getAllHeaders())
                                         .collect(Collectors.toList());
            if (request.getOriginal() instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request.getOriginal()).getEntity();
                collect.add(entity.getContentType());
                collect.add(new BasicHeader("Content-Length", String.valueOf(entity.getContentLength())));
            }
            String reqHeader = formatHeader(collect.toArray(new Header[0]));
            // String reqHeader = formatHeader(request.getOriginal().getAllHeaders());
            if (StringUtils.isNotEmpty(reqHeader)) {
                sb.append(reqHeader).append("\n");
            }
            if (StringUtils.isNotEmpty(request.getBody())) {
                sb.append("\n").append(request.getBody()).append("\n");
            } else if (request.getOriginal() instanceof HttpEntityEnclosingRequest) {
                try {
                    String s = "[multipart form data ...]";
                    HttpEntity entity = ((HttpEntityEnclosingRequest) request.getOriginal()).getEntity();
                    if (entity instanceof UrlEncodedFormEntity) {
                        s = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
                    }
                    sb.append("\n").append(s).append("\n");
                } catch (Exception e) {
                    LOG.warn("HttpEntityEnclosingRequest Content exception: " + e);
                }
            }
        }
        if (response != null && response.getOriginal() != null) {
            sb.append("\n");
            sb.append("<<< ").append(response.getOriginal().getStatusLine()).append("\n");
            String respHeader = formatHeader(response.getOriginal().getAllHeaders());
            if (StringUtils.isNotEmpty(respHeader)) {
                sb.append(respHeader).append("\n");
            }
            if (StringUtils.startsWith(response.getBody(), HTTP_DOWNLOAD_FILEPATH_PREFIX)) {
                sb.append("\n").append("[file stream ...]").append("\n");
            } else if (StringUtils.isNotEmpty(response.getBody())) {
                sb.append("\n").append(response.getBody()).append("\n");
            }
        }
        if (StringUtils.isNotEmpty(requestInfo.getErrMsg())) {
            sb.append("\n");
            sb.append("<<< ERROR").append("\n");
            sb.append(requestInfo.getErrMsg()).append("\n");
        }
        sb.append("\n\n");
        return sb.toString();
    }

    private String formatHeader(Header[] headers) {
        return Arrays.stream(headers).map(Object::toString).collect(Collectors.joining("\n"));
    }

    public static class HttpClientProvider implements RestClientProvider {
        @Override
        public RestClient createClient() {
            return new HttpClient();
        }
    }
}

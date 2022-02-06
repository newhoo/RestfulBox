package io.github.newhoo.restkit.util;

import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.HttpInfo;
import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.Request;
import io.github.newhoo.restkit.common.Response;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.config.Environment;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

public class HttpUtils {

    private static final String HTTP_HOSTADDRESS = "http.hostAddress";

    public static HttpInfo request(String url, HttpMethod method, Map<String, String> paramMap, String reqBody, Map<String, String> headerMap, Project project) {
        Request req = new Request();
        req.setUrl(url);
        req.setMethod(method.name());
        req.setHeaders(headerMap);
        req.setParams(paramMap);
        req.setBody(reqBody);

        // Pre-request Script
        try {
            handlePreRequestScript(req, project);
        } catch (Exception e) {
            e.printStackTrace();
            return new HttpInfo(req, "Pre-request Script Error: \n\n" + e.toString());
        }

        HttpInfo httpInfo = request(req, project);

        // Post-request Script
        if (httpInfo.getResponse() != null) {
            String body = httpInfo.getResponse().getBody();
            try {
                handlePostRequestScript(httpInfo.getResponse(), project);
            } catch (Exception e) {
                e.printStackTrace();
                httpInfo.setErrMsg("Post-request Script Error: \n\n" + e.toString());
            }
            httpInfo.getResponse().setBody0(body);
        }

        return httpInfo;
    }

    private static void handlePreRequestScript(Request request, Project project) throws Exception {
        String scriptPath = CommonSettingComponent.getInstance(project).getState().getPreRequestScriptPath();
        if (StringUtils.isNotEmpty(scriptPath) && Files.exists(Paths.get(scriptPath))) {
            Map<String, String> environmentMap = Environment.getInstance(project).getCurrentEnabledEnvMap();

            ScriptEngine se = new ScriptEngineManager().getEngineByName("javascript");
            Bindings bindings = se.createBindings();
            bindings.put("request", request);
            bindings.put("environment", environmentMap);
            se.eval(new FileReader(scriptPath), bindings);
        }
    }

    private static void handlePostRequestScript(Response response, Project project) throws Exception {
        String scriptPath = CommonSettingComponent.getInstance(project).getState().getPostRequestScriptPath();
        if (StringUtils.isNotEmpty(scriptPath) && Files.exists(Paths.get(scriptPath))) {
            Map<String, String> environmentMap = Environment.getInstance(project).getCurrentEnabledEnvMap();

            ScriptEngine se = new ScriptEngineManager().getEngineByName("javascript");
            Bindings bindings = se.createBindings();
            bindings.put("response", response);
            bindings.put("environment", environmentMap);
            se.eval(new FileReader(scriptPath), bindings);
        }
    }

    private static HttpInfo request(Request req, Project project) {
        if (req.getMethod() == null) {
            return new HttpInfo(req, "method is null");
        }

        String url = req.getUrl();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        Map<String, String> paramMap = req.getParams();
        if (!paramMap.isEmpty()) {
            // 替换URL 路径参数
            for (String key : paramMap.keySet()) {
                url = url.replaceFirst("\\{(" + key + "[\\s\\S]*?)}", StringUtils.defaultString(paramMap.get(key)));
            }
            String params = ToolkitUtil.getRequestParam(paramMap);
            // URL可能包含了参数
            url += url.contains("?") ? "&" + params : "?" + params;
        }

        req.setUrl(url);

        return doRequest(req, project);
    }

    private static HttpInfo doRequest(Request req, Project project) {
        HttpUriRequest request;
        try {
            request = getRequest(req, project);
            if (request == null) {
                return new HttpInfo(req, String.format("not supported request [%s %s]", req.getMethod(), req.getUrl()));
            }
        } catch (Exception e) {
            return new HttpInfo(req, String.format("not supported request [%s %s]: \n\n%s", req.getMethod(), req.getUrl(), e));
        }

        long startTs = System.currentTimeMillis();

        HttpClientContext context = HttpClientContext.create();
        try (CloseableHttpClient httpClient = createHttpClient();
             CloseableHttpResponse response = httpClient.execute(request, context)) {
            String hostAddress = (String) context.getAttribute(HTTP_HOSTADDRESS);

            String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            long cost = System.currentTimeMillis() - startTs;
            HttpInfo httpInfo = new HttpInfo(req, new Response(response, result), hostAddress, cost);
            FileUtils.logHttpInfo(httpInfo, project);

            return httpInfo;
        } catch (Exception e) {
            final String errMsg = "There was an error accessing to URL: " + req.getUrl() + "\n\n" + e.toString();
            HttpInfo httpInfo = new HttpInfo(req, errMsg);
            FileUtils.logHttpInfo(httpInfo, project);
            return httpInfo;
        }
    }

    private static HttpRequestBase getRequest(Request req, Project project) {
        HttpRequestBase request;
        HttpMethod httpMethod = HttpMethod.nameOf(req.getMethod());
        String url = req.getUrl();
        switch (httpMethod) {
            case GET:
                request = new HttpGet(url);
                break;
            case POST:
                request = new HttpPost(url);
                break;
            case PUT:
                request = new HttpPut(url);
                break;
            case PATCH:
                request = new HttpPatch(url);
                break;
            case DELETE:
                request = new MyHttpDelete(url);
                break;
            case HEAD:
                request = new HttpHead(url);
                break;
            default:
                return null;
        }

        req.getHeaders().forEach(request::addHeader);
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity httpEntity = StringUtils.isBlank(req.getBody())
                    ? new UrlEncodedFormEntity(Collections.emptyList(), StandardCharsets.UTF_8)
                    : new StringEntity(req.getBody(), ContentType.APPLICATION_JSON);
            ((HttpEntityEnclosingRequest) request).setEntity(httpEntity);
        }

        int requestTimeout = CommonSettingComponent.getInstance(project).getState().getRequestTimeout();
        if (requestTimeout > 0) {
            RequestConfig requestConfig = RequestConfig.custom()
                                                       // 从连接池中获取连接的超时时间
                                                       .setConnectionRequestTimeout(requestTimeout)
                                                       // 与服务器连接超时时间：httpclient会创建一个异步线程用以创建socket连接，此处设置该socket的连接超时时间
                                                       .setConnectTimeout(requestTimeout)
                                                       // 请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
                                                       .setSocketTimeout(requestTimeout)
                                                       .build();
            request.setConfig(requestConfig);
        }
        req.setOriginal(request);
        return request;
    }

    private static HttpRequestExecutor getHttpRequestExecutor() {
        return new HttpRequestExecutor() {
            @Override
            public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context) throws IOException, HttpException {
                if (conn instanceof ManagedHttpClientConnection) {
                    ManagedHttpClientConnection managedConn = (ManagedHttpClientConnection) conn;
                    if (managedConn.isOpen()) {
                        Socket socket = managedConn.getSocket();
                        if (socket.getInetAddress() != null) {
                            String hostAddress = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
                            context.setAttribute(HTTP_HOSTADDRESS, hostAddress);
                        }
                    }
                }
                return super.execute(request, conn, context);
            }
        };
    }

    private static CloseableHttpClient createHttpClient() {
        HttpClientBuilder builder = HttpClients.custom()
                                               .setRequestExecutor(getHttpRequestExecutor());
        try {
            // https信任所有
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build();
            builder.setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    private static class MyHttpDelete extends HttpEntityEnclosingRequestBase {

        public MyHttpDelete(String uri) {
            this.setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return "DELETE";
        }
    }
}
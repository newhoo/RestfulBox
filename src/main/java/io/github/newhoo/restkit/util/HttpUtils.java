package io.github.newhoo.restkit.util;

import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.RequestInfo;
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
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class HttpUtils {

    private static final String HTTP_HOSTADDRESS = "http.hostAddress";

    public static RequestInfo request(io.github.newhoo.restkit.restful.http.HttpRequest req) {
        if (req.getMethod() == null || HttpMethod.getByRequestMethod(req.getMethod()) == HttpMethod.UNDEFINED) {
            return new RequestInfo(req, "http method is null");
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

        return doRequest(req);
    }

    private static RequestInfo doRequest(io.github.newhoo.restkit.restful.http.HttpRequest req) {
        HttpUriRequest request;
        try {
            request = getRequest(req);
            if (request == null) {
                return new RequestInfo(req, String.format("not supported request [%s %s]", req.getMethod(), req.getUrl()));
            }
        } catch (Exception e) {
            return new RequestInfo(req, String.format("not supported request [%s %s]: \n\n%s", req.getMethod(), req.getUrl(), e));
        }

        long startTs = System.currentTimeMillis();

        HttpClientContext context = HttpClientContext.create();
        try (CloseableHttpClient httpClient = createHttpClient();
             CloseableHttpResponse response = httpClient.execute(request, context)) {
            String hostAddress = (String) context.getAttribute(HTTP_HOSTADDRESS);

            String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            // unicode 转码
            result = org.apache.commons.lang.StringEscapeUtils.unescapeJava(result);

            long cost = System.currentTimeMillis() - startTs;
            return new RequestInfo(req, new io.github.newhoo.restkit.restful.http.HttpResponse(response, result), hostAddress, cost);
        } catch (Exception e) {
            final String errMsg = "There was an error accessing to URL: " + req.getUrl() + "\n\n" + e.toString();
            return new RequestInfo(req, errMsg);
        }
    }

    private static HttpRequestBase getRequest(io.github.newhoo.restkit.restful.http.HttpRequest req) {
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

        String timeout = StringUtils.defaultIfEmpty(req.getConfig().get("timeout"), "5000");
        int requestTimeout = (int) Double.parseDouble(timeout);
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
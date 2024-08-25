package io.github.newhoo.restkit.util;

import com.intellij.openapi.diagnostic.Logger;
import io.github.newhoo.restkit.common.HttpMethod;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.common.RequestInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.common.RestConstant.HTTP_DEFAULT_TIMEOUT;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_DOWNLOAD_FILEPATH_PREFIX;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_FILE_DOWNLOAD_DIRECTORY;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_FILE_PREFIX;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_P12_PASSWD;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_P12_PATH;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_P12_CONTENT;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_TIMEOUT;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_URL_HTTP;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_URL_HTTPS;

public class HttpUtils {
    private static final Logger LOG = Logger.getInstance(HttpUtils.class);

    private static final String HTTP_HOSTADDRESS = "http.hostAddress";

    @NotProguard
    public static RequestInfo request(io.github.newhoo.restkit.restful.http.HttpRequest req) {
        if (req.getMethod() == null || HttpMethod.getByRequestMethod(req.getMethod()) == HttpMethod.UNDEFINED) {
            return new RequestInfo(req, "http method is null");
        }

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
        try (CloseableHttpClient httpClient = createHttpClient(req);
             CloseableHttpResponse response = httpClient.execute(request, context)) {
            String hostAddress = (String) context.getAttribute(HTTP_HOSTADDRESS);

            // 判断下载文件
            File downloadFile = tryDownloadFile(req, response);
            if (downloadFile != null) {
                return new RequestInfo(req, new io.github.newhoo.restkit.restful.http.HttpResponse(response, HTTP_DOWNLOAD_FILEPATH_PREFIX + " " + downloadFile.getPath()), hostAddress, (System.currentTimeMillis() - startTs));
            }
            String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            // unicode 转码，会自动删除转义的反斜杠：https://github.com/newhoo/RESTKit/issues/35
            // result = org.apache.commons.lang.StringEscapeUtils.unescapeJava(result);

            return new RequestInfo(req, new io.github.newhoo.restkit.restful.http.HttpResponse(response, result), hostAddress, (System.currentTimeMillis() - startTs));
        } catch (Exception e) {
            final String errMsg = "There was an error accessing to URL: " + req.getUrl() + "\n\n" + e.toString();
            RequestInfo requestInfo = new RequestInfo(req, errMsg);
            requestInfo.setCost(System.currentTimeMillis() - startTs);
            return requestInfo;
        }
    }

    private static HttpRequestBase getRequest(io.github.newhoo.restkit.restful.http.HttpRequest req) {
        HttpMethod httpMethod = HttpMethod.nameOf(req.getMethod());

        String url = req.getUrl();
        if (!url.startsWith(HTTP_URL_HTTP) && !url.startsWith(HTTP_URL_HTTPS)) {
            url = HTTP_URL_HTTP + url;
        }
        // 自带的query参数编码
        if (url.contains("?")) {
            String[] split = StringUtils.split(url, "?", 2);
            url = split[0] + "?" + ToolkitUtil.encodeQueryParam(split[1]);
        }

        Map<String, String> paramMap = req.getParams();
        // file params
        Map<String, String> fileParamsMap = paramMap.entrySet()
                                                    .stream()
                                                    .filter(entry -> StringUtils.startsWith(entry.getValue(), HTTP_FILE_PREFIX))
                                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // path variables
        Set<String> pathVariables = new HashSet<>(4);
        // 替换URL
        if (url.contains("{") && url.contains("}") && !paramMap.isEmpty()) {
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                if (StringUtils.isNotEmpty(entry.getValue()) && !fileParamsMap.containsKey(entry.getKey()) && url.contains(placeholder)) {
                    pathVariables.add(entry.getKey());
                    url = url.replace(placeholder, entry.getValue());
//                    url = url.replaceFirst("\\{(" + key + "[\\s\\S]*?)}", v);
                }
            }
        }
        // query/form params
        Map<String, String> queryOrFormParamsMap = paramMap.entrySet()
                                                           .stream()
                                                           .filter(entry -> !fileParamsMap.containsKey(entry.getKey()))
                                                           .filter(entry -> !pathVariables.contains(entry.getKey()))
                                                           .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // query params
        if (HttpMethod.GET == httpMethod || StringUtils.isNotEmpty(req.getBody())) {
            if (!queryOrFormParamsMap.isEmpty()) {
                String params = ToolkitUtil.getRequestParam(queryOrFormParamsMap);
                // URL可能包含了参数
                url += url.contains("?") ? "&" + params : "?" + params;
            }
        }

        HttpRequestBase request;
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
            if (StringUtils.isNotEmpty(req.getBody())) {
                String body = req.getBody().trim();
                StringEntity httpEntity = (body.startsWith("{") || body.startsWith("["))
                        ? new StringEntity(req.getBody(), ContentType.APPLICATION_JSON)
                        : new StringEntity(req.getBody(), StandardCharsets.UTF_8);
                ((HttpEntityEnclosingRequest) request).setEntity(httpEntity);
            } else {
                // form params: Content-Type: application/x-www-form-urlencoded
                if (fileParamsMap.isEmpty()) {
                    List<BasicNameValuePair> nameValuePairList = queryOrFormParamsMap.entrySet()
                                                                                     .stream()
                                                                                     .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                                                                                     .collect(Collectors.toList());
                    HttpEntity httpEntity = new UrlEncodedFormEntity(nameValuePairList, StandardCharsets.UTF_8);
                    ((HttpEntityEnclosingRequest) request).setEntity(httpEntity);
                } else {
                    // form params: Content-Type: multipart/form-data; boundary=------------------------75a1b524af201d5c
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setCharset(StandardCharsets.UTF_8);
                    // 和curl/postman一样，only write Content-Disposition; use content charset
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                    // add form parameter，中文乱码
                    ContentType contentType = ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8);
                    queryOrFormParamsMap.forEach((k, v) -> {
                        builder.addTextBody(k, v, contentType);
                    });

                    // add file parameter
                    fileParamsMap.forEach((k, v) -> {
                        String filepath = ToolkitUtil.getUploadFilepath(v);
                        File file = new File(filepath);
                        builder.addBinaryBody(k, file);
                    });

                    HttpEntity httpEntity = builder.build();
                    ((HttpEntityEnclosingRequest) request).setEntity(httpEntity);
                }
            }
        }

        String timeout = StringUtils.defaultIfEmpty(req.getConfig().get(HTTP_TIMEOUT), HTTP_DEFAULT_TIMEOUT + "");
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

    private static CloseableHttpClient createHttpClient(io.github.newhoo.restkit.restful.http.HttpRequest req) {
        HttpClientBuilder builder = HttpClients.custom()
                                               .setRequestExecutor(getHttpRequestExecutor());
        SSLConnectionSocketFactory socketFactory = null;
        if (req.getUrl().startsWith(HTTP_URL_HTTPS)) {
            String p12Path = StringUtils.defaultString(req.getConfig().get(HTTP_P12_PATH));
            String p12Content = StringUtils.defaultString(req.getConfig().get(HTTP_P12_CONTENT));
            String p12Passwd = StringUtils.defaultString(req.getConfig().get(HTTP_P12_PASSWD));

            // 单向认证
            if (p12Path.isEmpty() && p12Content.isEmpty()) {
                socketFactory = getOnewaySSLFactory();
            }
            // 双向认证
            else {
                socketFactory = getTwowaySSLFactory(p12Path, p12Content, p12Passwd);
            }
        }
        if (socketFactory != null) {
            builder.setSSLSocketFactory(socketFactory);
        }
        return builder.build();
    }

    private static SSLConnectionSocketFactory getOnewaySSLFactory() {
        try {
            // 初始化 SSLContext, 信任所有
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
            return new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            LOG.error("Oneway SSL config exception: " + e.toString());
        }
        return null;
    }

    private static SSLConnectionSocketFactory getTwowaySSLFactory(String p12Path, String p12Content, String passwd) {
        InputStream inputStream = null;
        try {
            if (new File(p12Path).exists()) {
                inputStream = new FileInputStream(p12Path);
            } else if (StringUtils.isNotEmpty(p12Content)) {
                System.out.printf("p12 file not exist: %s. use base64 content as p12\n",p12Path);
                inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(p12Content));
            } else {
                return null;
            }
            // 加载 keyStore
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(inputStream, passwd.toCharArray());

            // 创建密钥管理器
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, passwd.toCharArray());

            // 创建信任链管理器, 信任所有
            TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };

            // 初始化 SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), new TrustManager[]{tm}, null);

            return new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            LOG.error("Two-way SSL config exception: " + e.toString());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return null;
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

    private static File tryDownloadFile(io.github.newhoo.restkit.restful.http.HttpRequest req, CloseableHttpResponse response) throws IOException {
        try {
            return doDownloadFile(req, response);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            LOG.warn("download file ex: " + e);
            return null;
        }
    }

    private static File doDownloadFile(io.github.newhoo.restkit.restful.http.HttpRequest req, CloseableHttpResponse response) throws IOException {
        if (response.getStatusLine() == null || response.getStatusLine().getStatusCode() / 100 != 2) {
            return null;
        }
        String filename = null;
        // Content-Disposition: attachment; filename="2d8e6de174899729ccd12f41230a5510.webp"; filename*=utf-8''2d8e6de174899729ccd12f41230a5510.webp
        // Content-disposition: attachment;filename*=utf-8''aaa_%E5%85%AC%E8%B4%B9%E4%BC%9A%E8%AE%AE%E4%BD%BF%E7%94%A8%E8%AE%B0%E5%BD%9520230523.xlsx
        Header fileHeader = response.getFirstHeader("Content-Disposition");
        Header contentType = response.getFirstHeader("Content-Type");
        if (fileHeader != null) {
            filename = Arrays.stream(fileHeader.getElements())
                             .map(e -> ObjectUtils.defaultIfNull(e.getParameterByName("filename"), e.getParameterByName("filename*")))
                             .filter(Objects::nonNull)
                             .map(NameValuePair::getValue)
                             .findFirst()
                             .filter(StringUtils::isNotEmpty)
                             .map(s -> s.replace("utf-8''", "").replace("UTF-8''", ""))
                             .map(s -> URLDecoder.decode(s, StandardCharsets.UTF_8))
                             .orElse("noname_file");
        } else if (contentType != null && StringUtils.containsAnyIgnoreCase(contentType.getValue(), "application/octet-stream", "application/pdf", "image/", "audio/", "video/")) {
            String url = req.getUrl();
            if (StringUtils.isEmpty(url) || StringUtils.endsWith(url, "/")) {
                filename = "noname_file";
            } else {
                filename = url.substring(url.lastIndexOf('/') + 1);
                if (filename.contains("?")) {
                    filename = filename.substring(0, filename.indexOf("?"));
                }
                if (StringUtils.startsWith(filename, "{") && StringUtils.endsWith(filename, "}")) {
                    String substring = filename.substring(1, filename.length() - 1);
                    filename = req.getParams().getOrDefault(substring, substring);
                }
            }
        }
        if (StringUtils.isNotEmpty(filename)) {
            String downloadDirectory = req.getConfig().get(HTTP_FILE_DOWNLOAD_DIRECTORY);
            if (!StringUtils.endsWith(downloadDirectory, "/")) {
                downloadDirectory += "/";
            }
            File file = new File(downloadDirectory + System.currentTimeMillis() + "_" + filename);
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileUtils.copyToFile(response.getEntity().getContent(), file);
//            if (!filename.contains(".")) {
//                try (FileInputStream inputFile = new FileInputStream(file)) {
//                    String mimeType = URLConnection.guessContentTypeFromStream(new BufferedInputStream(inputFile));
//                    NotifierUtils.infoBalloon("", "Guess file type: " + mimeType, null, ProjectManager.getInstance().getDefaultProject());
//                } catch (Exception e) {
//                }
//            }
            return file;
        }
        return null;
    }
}
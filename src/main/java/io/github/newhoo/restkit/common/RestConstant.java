package io.github.newhoo.restkit.common;

/**
 * Constant
 *
 * @author huzunrong
 * @since 2.0.0
 */
public class RestConstant {

    public static final String WEB_FRAMEWORK_SPRING_MVC = "Spring MVC";
    public static final String WEB_FRAMEWORK_LOCAL = "Local Store";

    public static final String PLACEHOLDER_BASE_URL = "{{baseUrl}}";
    public static final String PLACEHOLDER_URL = "URL";

    public static final String PROTOCOL = "protocol";
    public static final String PROTOCOL_HTTP = "http";

    public static final String HTTP_URL_HTTP = "http://";
    public static final String HTTP_URL_HTTPS = "https://";
    public static final String HTTP_FILE_PREFIX = "file@[";
    public static final String HTTP_FILE_POSTFIX = "]";
    public static final String HTTP_FILE_DEFAULT_DESCRIPTOR = "file@[filepath]";
    public static final String HTTP_FILE_DOWNLOAD_DIRECTORY = "downloadDirectory";
    public static final String HTTP_BASE_URL = "baseUrl";
    public static final String HTTP_BASE_URL_PLACEHOLDER = PLACEHOLDER_BASE_URL;
    public static final String HTTP_BASE_URL_DEFAULT = "http://localhost:8080";
    public static final String HTTP_TIMEOUT = "timeout";
    public static final String HTTP_P12_PATH = "p12Path";
    public static final String HTTP_P12_PATH_PLACEHOLDER = "{{p12Path}}";
    public static final String HTTP_P12_PASSWD = "p12Passwd";
    public static final String HTTP_P12_PASSWD_PLACEHOLDER = "{{p12Passwd}}";

    public static final String PRE_REQUEST_SCRIPT =
            "// You can use custom preset object request and environment, attributes are:\n" +
            "// \n" +
            "// request attributes\n" +
            "// url:         java.lang.String,               request url, can be modified by pre-request script.\n" +
            "// method:      java.lang.String,               request method, can be modified by pre-request script.\n" +
            "// config:      java.util.Map<String, String>,  request config, can be modified by pre-request script.\n" +
            "// headers:     java.util.Map<String, String>,  request headers, can be modified by pre-request script.\n" +
            "// params:      java.util.Map<String, String>,  request params, can be modified by pre-request script.\n" +
            "// body:        java.lang.String,               request body, can be modified by pre-request script.\n" +
            "//\n" +
            "// environment: java.util.Map<String, String>,  current environment, defined in the Environment setting, readonly.\n" +
            "//\n" +
            "// See more usages in nashorn-users-guide: https://docs.oracle.com/en/java/javase/12/nashorn/nashorn-users-guide.pdf\n" +
            "\n" +
            "var env = environment;\n" +
            "var baseUrl = env.baseUrl;\n" +
            "var username = env['username'];\n" +
            "\n" +
            "var req = request;\n" +
            "var url = req.url;\n" +
            "var method = req.method;\n" +
            "var headers = req.headers;\n" +
            "var params = req.params;\n" +
            "var body = req.body;\n" +
            "\n" +
            "req.url = 'http://httpbin.org/ip';\n" +
            "req.method = 'GET';\n" +
            "req.headers = {\n" +
            "    'x-auth-ts': '' + new Date().valueOf(),\n" +
            "    'x-auth-traceid': '83b557cc-366a-4274-8912-078e71216c51',\n" +
            "};\n" +
            "req.headers['x-auth-token'] = '70309f2cc6a6462497f824e77baa77f9';\n" +
            "req.params = { code: 'ABCD' };\n" +
            "req.params.name = 'JavaNashorn';\n" +
            "req.body = JSON.stringify({ reqBody: 'Hello world!' });\n";

    public static final String POST_REQUEST_SCRIPT =
            "// You can use custom preset object request、response and environment, attributes are:\n" +
            "//\n" +
            "// request: see pre-request script comment\n" +
            "//\n" +
            "// response attributes\n" +
            "// original:    org.apache.http.HttpResponse,   original http response, from http-client 4.4.\n" +
            "// body:        java.lang.String,               response body can be modified by post-request script.\n" +
            "//\n" +
            "// environment: java.util.Map<String, String>,  current environment, defined in the Environment setting, readonly.\n" +
            "//\n" +
            "// See more usages in nashorn-users-guide: https://docs.oracle.com/en/java/javase/12/nashorn/nashorn-users-guide.pdf\n" +
            "\n" +
            "var env = environment;\n" +
            "var baseUrl = env.baseUrl;\n" +
            "var username = env['username'];\n" +
            "\n" +
            "var req = request;\n" +
            "var resp = response;\n" +
            "var statusCode = resp.original.getStatusLine().getStatusCode();\n" +
            "\n" +
            "if (statusCode != 200) {\n" +
            "    resp.body = JSON.stringify({ error: 'error occurred!' });\n" +
            "} else {\n" +
            "    resp.body = JSON.parse(resp.body).origin;\n" +
            "}\n";

    public static final String DEFAULT_SCRIPT_CONTENT =
            "public class RestKitScript {\n" +
            "\n" +
            "    /**\n" +
            "     * Your script method, no param, method name must from {{$methodName$}}.\n" +
            "     * Use pre-request script can be more powerful.\n" +
            "     *\n" +
            "     * @return return value should be String\n" +
            "     */\n" +
            "    public static String methodName() {\n" +
            "        return \"\";\n" +
            "    }\n" +
            "\n" +
            "}";

}
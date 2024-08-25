package io.github.newhoo.restkit.common;

/**
 * Constant
 *
 * @author huzunrong
 * @since 2.0.0
 */
public class RestConstant {

    public static final String DATA_SOURCE_IDE = "Ide";
    public static final String DATA_SOURCE_SQLITE = "Sqlite";
    public static final String DATA_SOURCE_MYSQL = "MySQL";

    public static final String WEB_FRAMEWORK_SPRING_MVC = "Spring MVC";
    public static final String WEB_FRAMEWORK_JAX_RS = "JAX-RS";

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
    public static final String HTTP_DOWNLOAD_FILEPATH_PREFIX = "file:";
    public static final String HTTP_BASE_URL = "baseUrl";
    public static final String HTTP_BASE_URL_PLACEHOLDER = PLACEHOLDER_BASE_URL;
    public static final String HTTP_BASE_URL_DEFAULT = "http://localhost:8080";
    public static final String HTTP_TIMEOUT = "timeout";
    public static final String HTTP_PROJECT = "project";
    public static final String HTTP_P12_PATH = "p12Path";
    public static final String HTTP_P12_CONTENT = "p12Content";
    public static final String HTTP_P12_PASSWD = "p12Passwd";
    public static final int HTTP_DEFAULT_TIMEOUT = 30000;

    public static final String EDITOR_FILENAME_PREFIX = "RestfulBox-Editor:";

    public static final String COMMAND_KEYWORD_QUERY = "{query}";
}
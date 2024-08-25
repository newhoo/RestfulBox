package io.github.newhoo.restkit.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.SystemProperties;
import io.github.newhoo.restkit.common.RequestInfo;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.config.RequestSetting;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * RestLog
 *
 * @author huzunrong
 * @since 1.0.0
 */
public class FileUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String getUserHomeBasePath() {
        String restDirectory = StringUtils.defaultIfEmpty(System.getenv("RESTFUL_HOME"), SystemProperties.getUserHome());
        restDirectory = restDirectory + File.separator + ".restkit" + File.separator;
        if ("DEV".equals(System.getenv("RESTFUL_ENV"))) {
            restDirectory = restDirectory + "test" + File.separator;
        }
        try {
            Files.createDirectories(Paths.get(restDirectory));
            return restDirectory;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SystemProperties.getUserHome();
    }

    public static String getRestDirectory(Project project) {
        return getRestDirectory(project.getName());
    }

    public static String getRestDirectory(String project) {
        return getUserHomeBasePath() + project + File.separator;
    }

    public static String getLocationRelativeToUserHome(String path) {
        File userHome = new File(SystemProperties.getUserHome() + File.separator);
        File userHomeBasePath = new File(getUserHomeBasePath());
        File file = new File(path);
        if (FileUtil.isAncestor(userHomeBasePath, file, true)) {
            path = '~' + File.separator + ".restkit" + File.separator + FileUtil.getRelativePath(userHomeBasePath, file);
        } else if (FileUtil.isAncestor(userHome, file, true)) {
            path = '~' + File.separator + FileUtil.getRelativePath(userHome, file);
        }
        return FileUtil.toSystemIndependentName(path);
//        return FileUtil.toSystemIndependentName(FileUtil.getLocationRelativeToUserHome(path, false));
    }

    public static String expandUserHome(String path) {
        String userHome = SystemProperties.getUserHome() + File.separator;
        String userHomeBasePath = getUserHomeBasePath();
        if (path.startsWith("~/.restkit/") || path.startsWith("~\\.restkit\\")) {
            path = userHomeBasePath + path.substring(11);
        } else if (path.startsWith("~/") || path.startsWith("~\\")) {
            path = userHome + path.substring(2);
        }
        return FileUtil.toSystemDependentName(path);
//        return FileUtil.toSystemDependentName(FileUtil.expandUserHome(path));
    }

    public static String getApiFilePath(Project project) {
        return getRestDirectory(project) + "apifile.json";
    }

    public static String getDownloadDirectory(Project project) {
        return getUserHomeBasePath() + "Downloads";
    }

    public static String getLogDirectory() {
        return getUserHomeBasePath() + "logs";
    }

    public static String getTodayLog() {
        return getLogDirectory() + File.separator + "request-" + DATE_FORMATTER.format(LocalDate.now()) + ".log";
    }

    public static String getScriptFilePath(String project, String scriptName) {
        return getRestDirectory(project) + scriptName;
    }
}
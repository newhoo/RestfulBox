package io.github.newhoo.restkit.i18n;

import com.intellij.AbstractBundle;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import io.github.newhoo.restkit.config.ConfigHelper;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * i18n
 *
 * @author huzunrong
 * @since 1.0
 */
public class RestBundle {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages.restkit", getLocale());

    private static Locale getLocale() {
        String lang = ConfigHelper.getGlobalSetting().getLanguage();
        if (Locale.SIMPLIFIED_CHINESE.toString().equals(lang)) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        return Locale.ROOT;
    }

    public static String message(String key) {
        return RESOURCE_BUNDLE.getString(key).trim();
    }

    public static String message(String key, Object... params) {
        return AbstractBundle.message(RESOURCE_BUNDLE, key, params).trim();
    }

    public static String messageWithChineseLangCheck(String key, String fallback) {
        String lang = ConfigHelper.getGlobalSetting().getLanguage();
        if (Locale.SIMPLIFIED_CHINESE.toString().equals(lang)) {
            IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId("com.intellij.zh"));
            if (plugin != null && plugin.isEnabled()) {
                return RESOURCE_BUNDLE.getString(key).trim();
            }
        }
        return fallback;
    }

    public static boolean isChineseLang() {
        return Locale.SIMPLIFIED_CHINESE.toString().equals(ConfigHelper.getGlobalSetting().getLanguage());
    }
}

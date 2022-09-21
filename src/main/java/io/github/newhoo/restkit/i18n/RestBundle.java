package io.github.newhoo.restkit.i18n;

import com.intellij.AbstractBundle;
import com.intellij.CommonBundle;

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
        String lang = Locale.getDefault().getLanguage();
        if (lang.equals(Locale.ENGLISH.getLanguage()) || lang.equals(Locale.CHINESE.getLanguage())) {
            return Locale.getDefault();
        }
        return Locale.ENGLISH;
    }

    public static String message(String key) {
        return RESOURCE_BUNDLE.getString(key).trim();
    }

    public static String message(String key, Object... params) {
        return CommonBundle.message(RESOURCE_BUNDLE, key, params).trim();
    }
}

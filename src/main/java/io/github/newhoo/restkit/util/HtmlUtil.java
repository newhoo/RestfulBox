package io.github.newhoo.restkit.util;

import io.github.newhoo.restkit.common.NotProguard;

@NotProguard
public final class HtmlUtil {

    public static final String BR = "<br/>";

    public static String link(String name, String text) {
        return "<a href=\"" + name + "\">" + text + "</a>";
    }
}

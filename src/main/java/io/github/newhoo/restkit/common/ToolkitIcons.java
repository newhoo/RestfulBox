package io.github.newhoo.restkit.common;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import io.github.newhoo.restkit.util.IdeaUtils;

import javax.swing.*;
import java.awt.*;

public class ToolkitIcons {

    public static final Icon TREE_ROOT = AllIcons.Nodes.ModuleGroup;
    public static final Icon TREE_PROJECT = AllIcons.General.ProjectStructure;
    public static final Icon TREE_MODULE = AllIcons.Nodes.Module;
    public static final Icon TREE_PACKAGE = AllIcons.Nodes.Package;

//    public static final Icon SERVICE = IconLoader.getIcon("/icons/service.png", ToolkitIcons.class); // services.svg/toolWindowServices.svg
    public static final Icon REQUEST = load("/icons/request.svg"); // #4A86E8 -- related.svg
    public static final Icon SYNC_DATA_SOURCE = load("/icons/sync.svg"); // #4A86E8 -- related.svg

    public static final Icon GET = load("/icons/method/get.svg"); // 16x16 GREEN -- groovyLanguage.svg
    public static final Icon PUT = load("/icons/method/put.svg"); // 16x16 ORANGE -- pointcut.svg/parameter.svg
    public static final Icon POST = load("/icons/method/post.svg"); // 16x16 BLUE
    public static final Icon PATCH = load("/icons/method/patch.svg"); // 16x16 GRAY
    public static final Icon DELETE = load("/icons/method/delete.svg"); // 16x16 RED
    public static final Icon UNDEFINED = load("/icons/method/undefined.svg"); // 16x16 GRAY -- emptyFatalError_dark.svg


    private static Icon load(String path) {
        Icon icon = IconLoader.getIcon(path, ToolkitIcons.class);
        Icon expuiIcon = IconLoader.getIcon("/expui" + path, ToolkitIcons.class);
        return new MyIcon(icon, expuiIcon);
    }

    static class MyIcon implements Icon {

        private final Icon icon;
        private final Icon expuiIcon;

        public MyIcon(Icon icon, Icon expuiIcon) {
            this.icon = icon;
            this.expuiIcon = expuiIcon;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (IdeaUtils.isNewUI()) {
                expuiIcon.paintIcon(c, g, x, y);
            } else {
                icon.paintIcon(c, g, x, y);
            }
        }

        @Override
        public int getIconWidth() {
            if (IdeaUtils.isNewUI()) {
                return expuiIcon.getIconWidth();
            } else {
                return icon.getIconWidth();
            }
        }

        @Override
        public int getIconHeight() {
            if (IdeaUtils.isNewUI()) {
                return expuiIcon.getIconHeight();
            } else {
                return icon.getIconHeight();
            }
        }
    }
}

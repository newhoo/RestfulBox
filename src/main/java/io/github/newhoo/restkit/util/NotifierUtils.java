package io.github.newhoo.restkit.util;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.NotProguard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NotProguard
public final class NotifierUtils {

    // com.intellij.notification.impl.NotificationGroupEP
    private static final NotificationGroup RESTFUL_TOOLKIT_BALLOON_GROUP = NotificationGroupManager.getInstance().getNotificationGroup("RESTKit.balloonGroup");

    public static void infoBalloon(@NotNull String title, @NotNull String message, @Nullable NotificationAction action, @NotNull Project project) {
        notify(title, message, NotificationType.INFORMATION, action, project);
    }

    public static void warnBalloon(@NotNull String title, @NotNull String msg, @NotNull Project project) {
        notify(title, msg, NotificationType.WARNING, null, project);
    }

    public static void errorBalloon(@NotNull String title, String msg, @NotNull Project project) {
        if (msg == null) {
            msg = "";
        }
        notify(title, msg, NotificationType.ERROR, null, project);
    }

    public static void errorBalloon(@NotNull String title, @NotNull String message, @Nullable NotificationAction action, @NotNull Project project) {
        notify(title, message, NotificationType.ERROR, action, project);
    }


    private static void notify(@NotNull String title, @NotNull String message, @NotNull NotificationType type, @Nullable NotificationAction action, @NotNull Project project) {
        if (message.isEmpty()) {
            message = title;
            title = "";
        }
        Notification notification = RESTFUL_TOOLKIT_BALLOON_GROUP.createNotification(title, message, type);
        if (action != null) {
            notification.addAction(action);
        }
        notification.notify(project);
    }
}

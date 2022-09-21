package io.github.newhoo.restkit.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.NotificationsManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NotifierUtils {

    // com.intellij.notification.impl.NotificationGroupEP
    private static final NotificationGroup RESTFUL_TOOLKIT_BALLOON_GROUP = new NotificationGroup("RESTKit.balloonGroup", NotificationDisplayType.BALLOON,true);//NotificationGroupManager.getInstance().getNotificationGroup("RESTKit.balloonGroup");

    public static Notification infoBalloon(@NotNull String title, @NotNull String message,
                                           @Nullable NotificationListener listener,
                                           @NotNull Project project) {
        return notify(RESTFUL_TOOLKIT_BALLOON_GROUP, title, message, NotificationType.INFORMATION, listener, project);
    }

    public static Notification warnBalloon(@NotNull String title, @NotNull String msg, @NotNull Project project) {
        return notify(RESTFUL_TOOLKIT_BALLOON_GROUP, title, msg, NotificationType.WARNING, null, project);
    }

    public static Notification errorBalloon(@NotNull String title, @NotNull String msg, @NotNull Project project) {
        return notify(RESTFUL_TOOLKIT_BALLOON_GROUP, title, msg, NotificationType.ERROR, null, project);
    }

    private static Notification notify(@NotNull NotificationGroup notificationGroup,
                                       String title,
                                       @NotNull String message,
                                       @NotNull NotificationType type,
                                       @Nullable NotificationListener listener,
                                       @NotNull Project project) {
        if (title == null) {
            title = "RESTKit Tips";
        }
        Notification notification = createNotification(notificationGroup, title, message, type, listener);
        return notify(notification, project);
    }

    private static Notification notify(@NotNull Notification notification, @NotNull Project project) {
        notification.notify(project);
        return notification;
    }

    private static Notification createNotification(@NotNull NotificationGroup notificationGroup,
                                                   @NotNull String title,
                                                   @NotNull String message,
                                                   @NotNull NotificationType type,
                                                   @Nullable NotificationListener listener) {
        if (StringUtil.isEmptyOrSpaces(message)) {
            message = title;
            title = "";
        }
        Notification notification = new Notification("RESTKit.balloonGroup", title, message, type);
        if (listener != null) {
            notification.setListener(listener);
        }
        return notification;
    }
}

package io.github.newhoo.restkit.toolwindow;

import com.intellij.util.messages.Topic;
import io.github.newhoo.restkit.common.RestItem;

/**
 * RestServiceListener
 *
 * @author newhoo
 * @since 1.0.8
 */
public interface RestServiceListener {

    Topic<RestServiceListener> REST_SERVICE_SELECT = Topic.create("RestServiceSelect", RestServiceListener.class);

    void select(RestItem serviceItem);
}

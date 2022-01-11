package io.github.newhoo.restkit.config;

import com.intellij.util.messages.Topic;

/**
 * EnvUpdateListener
 *
 * @author huzunrong
 * @since 1.0.0
 */
public interface SettingListener {

    Topic<SettingListener> ENV_UPDATE = Topic.create("EnvironmentUpdate", SettingListener.class);

    /**
     * 切换当前环境
     */
    void changeEnv();
}
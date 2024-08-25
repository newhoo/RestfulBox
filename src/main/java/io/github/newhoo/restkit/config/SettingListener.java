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
     * 环境更新
     */
    void changeEnv(String project);
}
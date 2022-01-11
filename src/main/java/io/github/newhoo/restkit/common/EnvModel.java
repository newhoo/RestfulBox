package io.github.newhoo.restkit.common;

import io.github.newhoo.restkit.config.KeyValueTableModel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * EnvModel
 *
 * @author huzunrong
 * @since 1.0.8
 */
@Data
@AllArgsConstructor
public class EnvModel {

    private String env;

    private KeyValueTableModel model;
}

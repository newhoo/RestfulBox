package io.github.newhoo.restkit.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Key Value
 *
 * @author huzunrong
 * @since 1.0.8
 */
@NotProguard
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KV {

    private String key;

    private String value;

    public String getUniqueKey() {
        return key + "_" + value;
    }
}

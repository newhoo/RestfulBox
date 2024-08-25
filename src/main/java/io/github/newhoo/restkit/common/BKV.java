package io.github.newhoo.restkit.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Boolean Key Value
 *
 * @author huzunrong
 * @since 1.0.8
 */
@NotProguard
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BKV {

    private Boolean enabled;

    private String key;

    private String value;
}

package io.github.newhoo.restkit.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * EnvList
 *
 * @author huzunrong
 * @since 1.0.8
 */
@NotProguard
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvList {

    private String env;

    private List<BKV> items;
}

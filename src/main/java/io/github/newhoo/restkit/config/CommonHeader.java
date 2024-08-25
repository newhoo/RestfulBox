package io.github.newhoo.restkit.config;

import io.github.newhoo.restkit.common.BKV;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.NotProguard;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Environment
 *
 * @author huzunrong
 * @since 1.0
 */
@NotProguard
@Data
public class CommonHeader {

    private String project;
    private List<BKV> globalHeaderList = Collections.emptyList();

    public List<KV> getEnabledGlobalHeader() {
        if (CollectionUtils.isEmpty(globalHeaderList)) {
            return Collections.emptyList();
        }
        List<KV> list = new ArrayList<>();

        for (BKV bkv : globalHeaderList) {
            if (bkv.getEnabled() && StringUtils.isNotBlank(bkv.getKey())) {
                list.add(new KV(bkv.getKey(), bkv.getValue()));
            }
        }
        return list;
    }
}
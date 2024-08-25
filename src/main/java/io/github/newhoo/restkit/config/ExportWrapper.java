package io.github.newhoo.restkit.config;

import io.github.newhoo.restkit.common.NotProguard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NotProguard
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportWrapper {
    private RequestSetting setting;
    private Environment environment;
    private CommonHeader header;
}

package io.github.newhoo.restkit.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RestModule {

    private String moduleName;

    private List<RestItem> restItems;

    @Override
    public String toString() {
        return this.moduleName;
    }
}

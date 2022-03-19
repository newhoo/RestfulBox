package io.github.newhoo.restkit.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RestModule {

    private String moduleName;

    private List<RestItem> restItems;

    public String getOrderKey() {
        return restItems.isEmpty() ? "" : restItems.get(0).getFramework();
    }

    @Override
    public String toString() {
        return this.moduleName;
    }
}

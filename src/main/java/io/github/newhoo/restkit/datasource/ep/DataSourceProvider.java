package io.github.newhoo.restkit.datasource.ep;

import com.intellij.openapi.extensions.ExtensionPointName;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.datasource.DataSource;

@NotProguard
public interface DataSourceProvider {

    ExtensionPointName<DataSourceProvider> EP_NAME = ExtensionPointName.create("io.github.newhoo.restkit.datasource");

    DataSource createRepository();
}

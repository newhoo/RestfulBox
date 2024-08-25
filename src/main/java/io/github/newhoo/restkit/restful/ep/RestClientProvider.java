package io.github.newhoo.restkit.restful.ep;

import com.intellij.openapi.extensions.ExtensionPointName;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.restful.RestClient;

/**
 * RestClientProvider
 *
 * @author huzunrong
 * @since 2.0.3
 */
@NotProguard
public interface RestClientProvider {

    ExtensionPointName<RestClientProvider> EP_NAME = ExtensionPointName.create("io.github.newhoo.restkit.restClient");

    RestClient createClient();
}

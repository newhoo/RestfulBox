package io.github.newhoo.restkit.restful.ep;

import com.intellij.openapi.extensions.ExtensionPointName;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.restful.RestItemDetail;

/**
 * RestItemDetailProvider
 *
 * @author huzunrong
 * @since 3.0.0
 */
@NotProguard
public interface RestItemDetailProvider {

    ExtensionPointName<RestItemDetailProvider> EP_NAME = ExtensionPointName.create("io.github.newhoo.restkit.restItemDetail");

    RestItemDetail createRestItemDetail();
}

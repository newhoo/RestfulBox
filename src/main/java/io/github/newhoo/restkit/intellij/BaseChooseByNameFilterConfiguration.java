package io.github.newhoo.restkit.intellij;

import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration;

public abstract class BaseChooseByNameFilterConfiguration<T> extends ChooseByNameFilterConfiguration<T> {

    public boolean isVisible(T type) {
        return false;
    }

    public boolean isFileTypeVisible(T type) {
        return isVisible(type);
    }
}

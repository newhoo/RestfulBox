package io.github.newhoo.restkit.feature.javaimpl.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * FilterParamConfigurable
 *
 * @author huzunrong
 * @since 2.0.3
 */
public class FilterParamConfigurable implements Configurable {

    private final FilterParamComponent filterParamComponent;
    private final FilterParamForm filterParamForm;

    private FilterParamConfigurable(Project project) {
        this.filterParamComponent = FilterParamComponent.getInstance(project).getState();
        this.filterParamForm = new FilterParamForm(project);
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Param Filter";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return filterParamForm;
    }

    @Override
    public boolean isModified() {
        Set<String> typeSet = Arrays.stream(filterParamForm.getTypeModel().toArray())
                                    .map(String::valueOf)
                                    .collect(Collectors.toSet());
        return !CollectionUtils.isEqualCollection(filterParamComponent.getQualifiedNames(), typeSet);

    }

    @Override
    public void apply() {
        Set<String> typeSet = Arrays.stream(filterParamForm.getTypeModel().toArray())
                                    .map(String::valueOf)
                                    .collect(Collectors.toSet());
        filterParamComponent.setQualifiedNames(typeSet);
    }

    @Override
    public void reset() {
        DefaultListModel<String> typeModel = filterParamForm.getTypeModel();
        typeModel.clear();
        typeModel.addAll(filterParamComponent.getQualifiedNames());
    }
}
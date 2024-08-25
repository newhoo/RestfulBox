package io.github.newhoo.restkit.feature.javaimpl.config;

import com.intellij.openapi.options.Configurable;
import io.github.newhoo.restkit.datasource.DataSource;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.util.JsonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JavaFilterConfigurable
 *
 * @author huzunrong
 * @since 2.0.3
 */
public class JavaFilterConfigurable implements Configurable {

    private final JavaFilterForm javaFilterForm;

    private final DataSource repository = DataSourceHelper.getDataSource();
    private JavaFilterSetting javaFilterSetting;

    public JavaFilterConfigurable() {
        this.javaFilterForm = new JavaFilterForm();
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Java Filter";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return javaFilterForm;
    }

    @Override
    public boolean isModified() {
        Set<String> classTypeSet = Arrays.stream(javaFilterForm.getFilterClassTypeModel().toArray())
                                         .map(String::valueOf)
                                         .collect(Collectors.toSet());
        Set<String> typeSet = Arrays.stream(javaFilterForm.getFilterParamTypeModel().toArray())
                                    .map(String::valueOf)
                                    .collect(Collectors.toSet());
        Set<String> fileTypeSet = Arrays.stream(javaFilterForm.getFileParameterTypeModel().toArray())
                                        .map(String::valueOf)
                                        .collect(Collectors.toSet());
        return !CollectionUtils.isEqualCollection(javaFilterSetting.getFilterClassQualifiedNames(), classTypeSet)
                || !CollectionUtils.isEqualCollection(javaFilterSetting.getFilterParamQualifiedNames(), typeSet)
                || !CollectionUtils.isEqualCollection(javaFilterSetting.getFileParameterQualifiedNames(), fileTypeSet);

    }

    @Override
    public void apply() {
        List<String> classTypeList = Arrays.stream(javaFilterForm.getFilterClassTypeModel().toArray())
                                           .map(String::valueOf)
                                           .collect(Collectors.toList());
        List<String> typeList = Arrays.stream(javaFilterForm.getFilterParamTypeModel().toArray())
                                      .map(String::valueOf)
                                      .collect(Collectors.toList());
        List<String> fileTypeSet = Arrays.stream(javaFilterForm.getFileParameterTypeModel().toArray())
                                         .map(String::valueOf)
                                         .collect(Collectors.toList());
        JavaFilterSetting filterParamComponent = new JavaFilterSetting();
        filterParamComponent.setFilterClassQualifiedNames(new LinkedHashSet<>(classTypeList));
        filterParamComponent.setFilterParamQualifiedNames(new LinkedHashSet<>(typeList));
        filterParamComponent.setFileParameterQualifiedNames(new LinkedHashSet<>(fileTypeSet));

        repository.syncSetting("-", JavaFilterSetting.getType(repository.name()), JsonUtils.toJson(filterParamComponent), null);
    }

    @Override
    public void reset() {
        if (javaFilterSetting == null) {
            String content = repository.selectSetting("-", JavaFilterSetting.getType(repository.name()), null);
            javaFilterSetting = JsonUtils.fromJson(content, JavaFilterSetting.class);
            if (javaFilterSetting == null) {
                javaFilterSetting = new JavaFilterSetting();
            }
        }
        DefaultListModel<String> classTypeModel = javaFilterForm.getFilterClassTypeModel();
        classTypeModel.clear();
        classTypeModel.addAll(javaFilterSetting.getFilterClassQualifiedNames());

        DefaultListModel<String> typeModel = javaFilterForm.getFilterParamTypeModel();
        typeModel.clear();
        typeModel.addAll(javaFilterSetting.getFilterParamQualifiedNames());

        DefaultListModel<String> fileParameterTypeModel = javaFilterForm.getFileParameterTypeModel();
        fileParameterTypeModel.clear();
        fileParameterTypeModel.addAll(javaFilterSetting.getFileParameterQualifiedNames());
    }
}
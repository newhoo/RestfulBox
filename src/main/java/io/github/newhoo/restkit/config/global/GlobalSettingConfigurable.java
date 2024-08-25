package io.github.newhoo.restkit.config.global;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import io.github.newhoo.restkit.config.ConfigHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * CertificateConfigurable
 *
 * @author huzunrong
 * @since 2.1.2
 */
public class GlobalSettingConfigurable implements Configurable {

    private final GlobalSetting globalSetting;
    private final GlobalSettingForm globalSettingForm;

    public GlobalSettingConfigurable() {
        this.globalSetting = ConfigHelper.getGlobalSetting();
        this.globalSettingForm = new GlobalSettingForm();
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "RestfulBox";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return globalSettingForm.getMainPanel();
    }

    @Override
    public boolean isModified() {
        GlobalSetting modifiedSetting = globalSettingForm.getModifiedSetting();
        boolean modified = !StringUtils.equals(globalSetting.getLanguage(), modifiedSetting.getLanguage())
                || globalSetting.isShowTreeRoot() != modifiedSetting.isShowTreeRoot()
                || globalSetting.isShowMultiLevelServiceNode() != modifiedSetting.isShowMultiLevelServiceNode()
                || globalSetting.isShowModuleInSearchEvery() != modifiedSetting.isShowModuleInSearchEvery()
                || globalSetting.isShowCompletePackageName() != modifiedSetting.isShowCompletePackageName()
                || globalSetting.isDisplayTreeListUsingApiDesc() != modifiedSetting.isDisplayTreeListUsingApiDesc()
                || globalSetting.isEnableParameterLibrary() != modifiedSetting.isEnableParameterLibrary()
                || globalSetting.isEnableMethodLineMarker() != modifiedSetting.isEnableMethodLineMarker()
                || globalSetting.isEnableMultiRequestClient() != modifiedSetting.isEnableMultiRequestClient();
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        GlobalSetting modifiedSetting = globalSettingForm.getModifiedSetting();
        globalSetting.setLanguage(modifiedSetting.getLanguage());
        globalSetting.setDisplayTreeListUsingApiDesc(modifiedSetting.isDisplayTreeListUsingApiDesc());
        globalSetting.setShowTreeRoot(modifiedSetting.isShowTreeRoot());
        globalSetting.setShowMultiLevelServiceNode(modifiedSetting.isShowMultiLevelServiceNode());
        globalSetting.setShowModuleInSearchEvery(modifiedSetting.isShowModuleInSearchEvery());
        globalSetting.setShowCompletePackageName(modifiedSetting.isShowCompletePackageName());
        globalSetting.setEnableParameterLibrary(modifiedSetting.isEnableParameterLibrary());
        globalSetting.setEnableMethodLineMarker(modifiedSetting.isEnableMethodLineMarker());
        globalSetting.setEnableMultiRequestClient(modifiedSetting.isEnableMultiRequestClient());
    }

    @Override
    public void reset() {
        globalSettingForm.reset(globalSetting);
    }
}
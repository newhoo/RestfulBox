package io.github.newhoo.restkit.config.global;

import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.IdeBorderFactory;
import io.github.newhoo.restkit.i18n.RestBundle;
import lombok.Getter;

import javax.swing.*;
import java.util.Arrays;

/**
 * @author huzunrong
 * @since 2.1.2
 */
public class GlobalSettingForm {

    @Getter
    private JPanel mainPanel;

    private JPanel uiConfigPanel;
    private JLabel languageLabel;
    private JComboBox<String> languageComboBox;
    private JCheckBox showTreeRootCheckBox;
    private JCheckBox showMultiLevelServiceCheckBox;
    private JCheckBox showCompletePackageNameCheckBox;
    private JCheckBox displayTreeListUsingApiDescCheckBox;
    private JCheckBox showModuleCheckBox;
    private JCheckBox enableMultiRequestClientCheckBox;
    private JCheckBox enableParameterLibraryCheckBox;
    private JCheckBox enableMethodLineMarkerCheckBox;

    public GlobalSettingForm() {
        initUI();
    }

    private void initUI() {
        uiConfigPanel.setBorder(IdeBorderFactory.createTitledBorder(RestBundle.message("toolkit.config.global.ui.panel.title"), false));
        languageLabel.setText(RestBundle.message("toolkit.config.global.ui.languageLabel"));
        languageComboBox.setModel(new CollectionComboBoxModel<>(Arrays.asList("English", "中文简体")));
        showTreeRootCheckBox.setText(RestBundle.message("toolkit.config.global.ui.showTreeRootCheckBox"));
        showMultiLevelServiceCheckBox.setText(RestBundle.message("toolkit.config.global.ui.showMultiLevelServiceCheckBox"));
        showCompletePackageNameCheckBox.setText(RestBundle.message("toolkit.config.global.ui.showCompletePackageNameCheckBox"));
        displayTreeListUsingApiDescCheckBox.setText(RestBundle.message("toolkit.config.global.ui.displayTreeListUsingApiDescCheckBox"));
        showModuleCheckBox.setText(RestBundle.message("toolkit.config.global.ui.showModuleCheckBox"));
        enableMultiRequestClientCheckBox.setText(RestBundle.message("toolkit.config.global.ui.enableMultiRequestClientCheckBox"));
        enableParameterLibraryCheckBox.setText(RestBundle.message("toolkit.config.global.ui.enableParameterLibraryCheckBox"));
        enableMethodLineMarkerCheckBox.setText(RestBundle.message("toolkit.config.global.ui.enableMethodLineMarkerCheckBox"));
        showMultiLevelServiceCheckBox.addItemListener(e -> {
            showMultiLevelServiceCheckBox.setText(RestBundle.message("toolkit.config.global.ui.showMultiLevelServiceCheckBox2"));
        });
        enableParameterLibraryCheckBox.addItemListener(e -> {
            enableParameterLibraryCheckBox.setText(RestBundle.message("toolkit.config.global.ui.enableParameterLibraryCheckBox2"));
        });
        enableMultiRequestClientCheckBox.addItemListener(e -> {
            enableMultiRequestClientCheckBox.setText(RestBundle.message("toolkit.config.global.ui.enableMultiRequestClientCheckBox2"));
        });
    }

    public GlobalSetting getModifiedSetting() {
        GlobalSetting setting = new GlobalSetting();
        setting.setLanguage("中文简体".equals(languageComboBox.getSelectedItem()) ? "zh_CN" : "en");
        setting.setDisplayTreeListUsingApiDesc(displayTreeListUsingApiDescCheckBox.isSelected());
        setting.setShowTreeRoot(showTreeRootCheckBox.isSelected());
        setting.setShowMultiLevelServiceNode(showMultiLevelServiceCheckBox.isSelected());
        setting.setShowModuleInSearchEvery(showModuleCheckBox.isSelected());
        setting.setShowCompletePackageName(showCompletePackageNameCheckBox.isSelected());
        setting.setEnableParameterLibrary(enableParameterLibraryCheckBox.isSelected());
        setting.setEnableMethodLineMarker(enableMethodLineMarkerCheckBox.isSelected());
        setting.setEnableMultiRequestClient(enableMultiRequestClientCheckBox.isSelected());
        return setting;
    }

    public void reset(GlobalSetting globalSetting) {
        languageComboBox.setSelectedItem("zh_CN".equals(globalSetting.getLanguage()) ? "中文简体" : "English");
        displayTreeListUsingApiDescCheckBox.setSelected(globalSetting.isDisplayTreeListUsingApiDesc());
        showTreeRootCheckBox.setSelected(globalSetting.isShowTreeRoot());
        showMultiLevelServiceCheckBox.setSelected(globalSetting.isShowMultiLevelServiceNode());
        showMultiLevelServiceCheckBox.setText(RestBundle.message("toolkit.config.global.ui.showMultiLevelServiceCheckBox"));
        showModuleCheckBox.setSelected(globalSetting.isShowModuleInSearchEvery());
        showCompletePackageNameCheckBox.setSelected(globalSetting.isShowCompletePackageName());
        enableParameterLibraryCheckBox.setSelected(globalSetting.isEnableParameterLibrary());
        enableMethodLineMarkerCheckBox.setSelected(globalSetting.isEnableMethodLineMarker());
        enableParameterLibraryCheckBox.setText(RestBundle.message("toolkit.config.global.ui.enableParameterLibraryCheckBox"));
        enableMultiRequestClientCheckBox.setSelected(globalSetting.isEnableMultiRequestClient());
        enableMultiRequestClientCheckBox.setText(RestBundle.message("toolkit.config.global.ui.enableMultiRequestClientCheckBox"));
    }
}

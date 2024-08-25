package io.github.newhoo.restkit.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Locale;

public class LicenseDialog extends DialogWrapper {

    private JPanel mainPanel;
    private JLabel premiumLabel;
    private JButton premiumBtn;

    public LicenseDialog(Project project) {
        super(project, false);

        setTitle("[RestfulBox] " + (Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage()) ? "高级版激活提示" : "Require License"));
        setResizable(false);
        setSize(600, 400);

        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        String tip = Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage())
                ? "<p>激活高级版，畅享更多实用便捷功能！</p>" +
                "<ol>" +
                "<li>自定义插件数据存储，更安全更可控；</li>" +
                "<li>全局项目服务管理，跨IDE跨项目浏览服务接口；</li>" +
                "<li>无限制地多层级树节点浏览，与项目接口紧密贴合；</li>" +
                "<li>无限制地支持展示多个请求面板，同时发起多个请求；</li>" +
                "<li>拥有便捷的工具箱（时间转换、加解密、进制转换等）；</li>" +
                "<li>独创命令中心，支持丰富的自定义命令；</li>" +
                "<li>支持自动识别并打开远程git仓库；</li>" +
                "<li>支持Jvm启动参数管理(IDEA/Android Studio中可用)；</li>" +
                "<li>更好的使用体验继续；</li>" +
                "<li>...</li>" +
                "</ol>"
                : "<p>Activate premium version for more powerful feature:<p/>" +
                "<ol>" +
                "<li>Support multiple data sources. Local/Cloud/Custom.</li>" +
                "<li>View and manage services when crossing IDE and crossing project.</li>" +
                "<li>Support unlimited multi-level service tree from projects.</li>" +
                "<li>Support unlimited displaying or sending multiple requests at the same time.</li>" +
                "<li>Have a convenient toolbox (Time conversion, Encryption & Decryption and etc.)</li>" +
                "<li>Original command center, support rich custom commands.</li>" +
                "<li>Support finding and opening of remote git repositories automatically.</li>" +
                "<li>Support manage jvm parameters for IDEA/Android Studio.</li>" +
                "<li>Better user experience will go on.</li>" +
                "<li>...</li>" +
                "</ol>";
        premiumLabel.setText("<html><body>" + tip + "</body></html>");
        premiumBtn.setText(Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage()) ? "立即激活" : "Activate");
        premiumBtn.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                close(OK_EXIT_CODE);
            }
        });
        return mainPanel;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[0];
    }
}

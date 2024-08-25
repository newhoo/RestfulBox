package io.github.newhoo.restkit.toolwindow.parameter.library;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.ClickListener;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.RelativeFont;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.accessibility.AccessibleContextUtil;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.RestClientEditorInfo;
import io.github.newhoo.restkit.common.RestDataKey;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.intellij.BaseAnAction;
import io.github.newhoo.restkit.intellij.CompactHelper;
import io.github.newhoo.restkit.util.IdeaUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.LabelUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static io.github.newhoo.restkit.toolwindow.parameter.library.RestParameterListener.REST_PARAMETER_UPDATE;
import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;

/**
 * ShowParameterAction
 *
 * @author huzunrong
 * @see com.intellij.openapi.wm.impl.welcomeScreen.RecentProjectPanel
 * @since 1.0.8
 */
public class ShowParameterAction extends BaseAnAction implements CustomComponentAction {

    private final Editor editor;

    public ShowParameterAction(Editor editor) {
        super(() -> "", () -> RestBundle.message("toolkit.toolwindow.client.parameter.show.text"), AllIcons.Actions.ListFiles);
        this.editor = editor;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = editor.getProject();
        if (project == null || project.isDefault()) {
            return;
        }
        RestClientEditorInfo restClientEditorInfo = RestDataKey.CLIENT_EDITOR_INFO.getData(e.getDataContext());
        if (restClientEditorInfo == null) {
            e.getPresentation().setEnabled(false);
            return;
        }

//        Map<String, List<KV>> param = parameterLibrary.getParameterMap().getOrDefault(restClientEditorInfo.getEditor(), Collections.emptyMap());
//        List<KV> paramList = param.get(restClientEditorInfo.getParameterKey());
//        if (CollectionUtils.isNotEmpty(paramList)) {
//            presentation.setText(() -> paramList.size() + "");
//        }
    }

    @Override
    public JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        // JBUI.size(16)
        ActionButtonWithText actionButtonWithText = new ActionButtonWithText(this, presentation, place, JBUI.size(18)) {
            @Override
            protected int iconTextSpace() {
                return JBUI.scale(2);
            }

            @Override
            public Insets getInsets() {
                return JBUI.insets(2);
            }

            @Override
            protected Insets getMargins() {
//                    if (myPresentation.icon == AllIcons.General.ReaderMode) JBUI.emptyInsets() else JBUI.insetsRight(5)
                return JBUI.emptyInsets();
            }
        };
        return actionButtonWithText;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getRequiredData(CommonDataKeys.PROJECT);

        RestClientEditorInfo restClientEditorInfo = RestDataKey.CLIENT_EDITOR_INFO.getData(e.getDataContext());
        if (restClientEditorInfo == null) {
            return;
        }

        List<KV> parameterList = DataSourceHelper.getDataSource().selectParameter(restClientEditorInfo, editor.getProject());
        if (parameterList == null || parameterList.isEmpty()) {
            parameterList = new LinkedList<>();
        }
        MyList myList = new MyList(JBUI.size(250, 300), restClientEditorInfo, parameterList, editor);
        myList.setBackground(editor.getColorsScheme().getColor(EditorColors.GUTTER_BACKGROUND));
        myList.setCellRenderer(new RecentProjectItemRenderer());

        new ClickListener() {
            @Override
            public boolean onClick(@NotNull MouseEvent event, int clickCount) {
                int selectedIndex = myList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    Rectangle cellBounds = myList.getCellBounds(selectedIndex, selectedIndex);
                    if (cellBounds.contains(event.getPoint())) {
                        KV selectedValue = myList.getSelectedValue();
                        project.getMessageBus().syncPublisher(REST_PARAMETER_UPDATE).update(restClientEditorInfo.getEditor(), selectedValue.getValue());
                        IdeEventQueue.getInstance().getPopupManager().closeAllPopups(false);
                    }
                }
                return true;
            }
        }.installOn(myList);

        myList.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KV selectedValue = myList.getSelectedValue();
                project.getMessageBus().syncPublisher(REST_PARAMETER_UPDATE).update(restClientEditorInfo.getEditor(), selectedValue.getValue());
                IdeEventQueue.getInstance().getPopupManager().closeAllPopups(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        addMouseMotionListener(myList);

        myList.setSelectedIndex(0);

        JPanel jPanel = new JPanel(new BorderLayout());
        JBScrollPane scroll
                = new JBScrollPane(myList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(JBUI.Borders.empty());
        jPanel.add(scroll, BorderLayout.CENTER);
//        jPanel.setBorder(new LineBorder(JBColor.namedColor("WelcomeScreen.borderColor", new JBColor(Gray._190, Gray._85))));
        jPanel.setBorder(JBUI.Borders.empty());
//        JList list = UIUtil.findComponentOfType(jPanel, JList.class);
        JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(jPanel, null)
//                                      .setTitle(String.format("%s Parameters (%s %s)", restClientEditorInfo.getEditor(), restClientEditorInfo.getMethod(), restClientEditorInfo.getUrl()))
                                      .setTitle(String.format("%s List", restClientEditorInfo.getEditor()))
                                      .setFocusable(true)
                                      .setRequestFocus(true)
                                      .setMayBeParent(true)
                                      .setMovable(true)
                                      .setResizable(true)
                                      .setNormalWindowLevel(true)
                                      .createPopup();
        Disposer.register(popup, Disposer.newDisposable());

        popup.showInBestPositionFor(e.getDataContext());
    }

    protected void addMouseMotionListener(MyList myList) {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            boolean myIsEngaged = false;

            @Override
            public void mouseMoved(MouseEvent e) {
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focusOwner == null) {
                    IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(myList, true));
                }
                if (myList.getSelectedIndices().length > 1) {
                    return;
                }

                if (!myIsEngaged || UIUtil.isSelectionButtonDown(e) || focusOwner instanceof JRootPane) {
                    myIsEngaged = true;
                    return;
                }

                Point point = e.getPoint();
                int index = myList.locationToIndex(point);
                myList.setSelectedIndex(index);

                Rectangle cellBounds = myList.getCellBounds(index, index);
                if (cellBounds != null && cellBounds.contains(point)) {
                    UIUtil.setCursor(myList, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    myHoverIndex = index;
                    myList.repaint(cellBounds);
                } else {
                    UIUtil.setCursor(myList, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    myHoverIndex = -1;
                    myList.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                myHoverIndex = -1;
                myList.repaint();
            }
        };

        myList.addMouseMotionListener(mouseAdapter);
        myList.addMouseListener(mouseAdapter);
    }

    private final static class MyList extends JBList<KV> {
        private final Dimension mySize;
        private Point myMousePoint;
        private final List<KV> parameterList;
        private final RestClientEditorInfo restClientEditorInfo;
        private final Editor editor;

        private MyList(Dimension size, RestClientEditorInfo restClientEditorInfo, List<KV> parameterList, Editor editor) {
            super(parameterList.toArray(new KV[0]));
            this.mySize = size;
            this.parameterList = parameterList;
            this.restClientEditorInfo = restClientEditorInfo;
            this.editor = editor;
            setExpandableItemsEnabled(false);
            setEmptyText("No content saved yet");
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            getAccessibleContext().setAccessibleName("Request Parameter");
            final PopupHandler handler = new MyPopupMouseHandler();
            addMouseListener(handler);
            addMouseMotionListener(handler);
        }

        public Rectangle getCloseIconRect(int index) {
            final Rectangle bounds = getCellBounds(index, index);
            Icon icon = toSize(AllIcons.Ide.Notification.Gear);
            return new Rectangle(bounds.width - icon.getIconWidth() - JBUIScale.scale(10),
                    bounds.y + (bounds.height - icon.getIconHeight()) / 2,
                    icon.getIconWidth(), icon.getIconHeight());
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (myMousePoint != null) {
                final int index = locationToIndex(myMousePoint);
                if (index != -1) {
                    final Rectangle iconRect = getCloseIconRect(index);
                    Icon actionIcon = detectActionIcon(index, iconRect.contains(myMousePoint));
                    Icon icon = toSize(actionIcon);
                    icon.paintIcon(this, g, iconRect.x, iconRect.y);
                }
            }
        }

        private Icon detectActionIcon(int rowIndex, boolean hovered) {
            return hovered ? AllIcons.Ide.Notification.GearHover : AllIcons.Ide.Notification.Gear;
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            final int i = event != null ? locationToIndex(event.getPoint()) : -1;
            if (i != -1) {
                final KV elem = getModel().getElementAt(i);
                return "<html><body><pre>" + elem.getValue() + "</pre></body></html>";
            }
            return super.getToolTipText(event);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return mySize == null ? super.getPreferredScrollableViewportSize() : mySize;
        }

        class MyPopupMouseHandler extends PopupHandler {

            @Override
            public void mouseEntered(MouseEvent e) {
                myMousePoint = e != null ? e.getPoint() : null;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                myMousePoint = null;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                myMousePoint = e != null ? e.getPoint() : null;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (e.isConsumed()) return;

                Point point = e.getPoint();
                int index = locationToIndex(point);
                if (index == -1 || !getCloseIconRect(index).contains(point)) return;
                invokePopup(e.getComponent(), e.getX(), e.getY());
                e.consume();
            }

            @Override
            public void invokePopup(Component comp, int x, int y) {
                final int index = locationToIndex(new Point(x, y));
                if (index != -1 && Arrays.binarySearch(getSelectedIndices(), index) < 0) {
                    setSelectedIndex(index);
                }
//                final ActionGroup group = (ActionGroup) ActionManager.getInstance().getAction("WelcomeScreenRecentProjectActionGroup");
                final ActionGroup group = new DefaultActionGroup(
                        new BaseAnAction(RestBundle.message("toolkit.common.btn.replace")) {

                            @Override
                            public void actionPerformed(@NotNull AnActionEvent e) {
                                // editor content
                                KV selectedValue = MyList.super.getSelectedValue();

                                String newValue = editor.getDocument().getText();
                                parameterList.stream()
                                             .filter(kv -> selectedValue.getKey().equals(kv.getKey()))
                                             .forEach(kv -> kv.setValue(newValue));
                                selectedValue.setValue(newValue);
                                DataSourceHelper.getDataSource().syncParameter(restClientEditorInfo, parameterList, editor.getProject());
                            }
                        },
                        new BaseAnAction(RestBundle.message("toolkit.common.btn.rename")) {

                            @Override
                            public void actionPerformed(@NotNull AnActionEvent e) {
                                // rename
//                                Component component = e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
//                        if (component instanceof MyList) {
                                KV selectedValue = MyList.super.getSelectedValue();

                                String paramName = CompactHelper.showInputDialog(e.getProject(), RestBundle.message("toolkit.toolwindow.client.parameter.save.input.msg"), RestBundle.message("toolkit.toolwindow.client.parameter.show.update.name", restClientEditorInfo.getEditor()), null, selectedValue.getKey(), new InputValidator() {

                                    @Override
                                    public boolean checkInput(String inputString) {
                                        return StringUtils.isNotEmpty(inputString) && parameterList.stream().noneMatch(kv -> inputString.equals(kv.getKey()));
                                    }

                                    @Override
                                    public boolean canClose(String inputString) {
                                        return true;
                                    }
                                });
                                if (StringUtils.isEmpty(paramName)) {
                                    return;
                                }

                                parameterList.stream()
                                             .filter(kv -> selectedValue.getKey().equals(kv.getKey()))
                                             .forEach(kv -> kv.setKey(paramName));
                                selectedValue.setKey(paramName);
                                DataSourceHelper.getDataSource().syncParameter(restClientEditorInfo, parameterList, editor.getProject());
//                        }
                            }
                        },
                        new BaseAnAction(RestBundle.message("toolkit.common.btn.delete")) {
                            @Override
                            public void actionPerformed(@NotNull AnActionEvent e) {
                                // delete
//                                Component component = e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
//                        if (component instanceof MyList) {
//                            MyList list = (MyList) component;
//                            list.remove(list.getSelectedIndex());
//                                MyList.super.remove(MyList.super.getSelectedIndex());

                                parameterList.removeIf(kv -> kv.getKey().equals(MyList.super.getSelectedValue().getKey()));
                                MyList.super.setListData(parameterList.toArray(new KV[0]));
                                DataSourceHelper.getDataSource().syncParameter(restClientEditorInfo, parameterList, editor.getProject());
//                        }
                            }
                        });
                ActionManager.getInstance().createActionPopupMenu(ActionPlaces.WELCOME_SCREEN, group).getComponent().show(comp, x, y);
            }
        }
    }

    private int myHoverIndex = -1;

    protected class RecentProjectItemRenderer extends JPanel implements ListCellRenderer<KV> {
        protected final JLabel myName = new JLabel();
        protected final JLabel myPath = new CommentLabel(""); //ComponentPanelBuilder.createNonWrappingCommentComponent("");
        protected boolean myHovered;

        protected RecentProjectItemRenderer() {
            super(new VerticalFlowLayout());
            setFocusable(true);
            layoutComponents();
        }

        protected void layoutComponents() {
            add(myName);
            add(myPath);
        }

        protected Color getListBackground(boolean isSelected, boolean hasFocus) {
            return UIUtil.getListBackground(isSelected, true);
        }

        protected Color getListForeground(boolean isSelected, boolean hasFocus) {
            return UIUtil.getListForeground(isSelected, true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends KV> list, KV value, int index, boolean selected, boolean focused) {
            myHovered = myHoverIndex == index;
            Color fore = getListForeground(selected, list.hasFocus());
            Color back = getListBackground(selected, list.hasFocus());

            myName.setForeground(fore);
            myPath.setForeground(UIUtil.getInactiveTextColor());

            setBackground(back);

            myName.setText(value.getKey());
            myPath.setText(value.getValue());

            AccessibleContextUtil.setCombinedName(this, myName, " - ", myPath);
            AccessibleContextUtil.setCombinedDescription(this, myName, " - ", myPath);
            return this;
        }


        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            return new Dimension(Math.min(size.width, JBUIScale.scale(245)), size.height);
        }

        @NotNull
        @Override
        public Dimension getSize() {
            return getPreferredSize();
        }
    }

    private static class CommentLabel extends JBLabel {
        private CommentLabel(@NotNull String text) {
            super(text);
            setForeground(JBUI.CurrentTheme.ContextHelp.FOREGROUND);
        }

        @Override
        public void setUI(LabelUI ui) {
            super.setUI(ui);
            if (IdeaUtils.isNewUI()) {
                setFont(JBFont.medium());
            } else {
                setFont(new FontUIResource(RelativeFont.NORMAL.fromResource("ContextHelp.fontSizeOffset", -2).derive(getFont())));
            }
        }
    }

    @NotNull
    private static Icon toSize(@NotNull Icon icon) {
        return IconUtil.toSize(icon,
                (int) ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE.getWidth(),
                (int) ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE.getHeight());
    }
}

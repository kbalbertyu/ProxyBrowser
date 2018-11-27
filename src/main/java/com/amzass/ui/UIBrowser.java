package com.amzass.ui;

import com.amzass.common.Application;
import com.amzass.enums.common.Directory;
import com.amzass.service.common.ProxyWebDriverContainer;
import com.amzass.ui.common.AbstractApplicationUI;
import com.amzass.ui.utils.SplashHelper;
import com.amzass.ui.utils.UITools;
import com.amzass.util.proxy.Constant;
import com.amzass.utils.common.Exceptions.BusinessException;
import com.amzass.utils.common.ProcessCleaner;
import com.amzass.utils.common.RegexUtils;
import com.amzass.utils.common.Tools;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/24/2018 8:03 AM
 */
@Singleton
public class UIBrowser extends AbstractApplicationUI {

    private static final Logger LOGGER = LoggerFactory.getLogger(UIBrowser.class);
    private static final long serialVersionUID = 6482431644646675142L;
    private static final int DEFAULT_EMAILS_NUM = 100;
    private static final int MAX_EMAILS_MUM = NumberUtils.toInt(Tools.getCustomizingValue("MAX_EMAILS_MUM"), DEFAULT_EMAILS_NUM);
    private static final int FIELD_WIDTH = 250;
    private static final Color BUTTON_STOPPED_COLOR = Color.decode("#dc3545");
    private static final Color BUTTON_STARTED_COLOR = Color.decode("#57a900");
    private static final int BUTTON_WIDTH = 90;
    private static final Dimension FIELD_SIZE = new Dimension(FIELD_WIDTH, 30);

    private List<String> emails = new ArrayList<>();
    private final List<JTextField> emailFields = new ArrayList<>();
    private final List<JButton> actionButtons = new ArrayList<>();
    private int gap = 24;
    private static final String BUTTON_START = "Start";
    private static final String BUTTON_CONTROL = "Manage";
    @Inject private ProxyWebDriverContainer proxyWebDriverContainer;

    void init() {
        emails = Tools.readFile(this.getEmailsFile());
        if (CollectionUtils.isEmpty(emails)) {
            throw new BusinessException("No emails is configured yet.");
        }
        if (!this.validateEmails(emails)) {
            throw new BusinessException("No emails is configured yet.");
        }

        this.loadEmailAndBottomLines();
        initComponents();
        UITools.addListener2Textfields(this.getContentPane());
        UITools.addListener2Textfields(this.getContentPane());
        UITools.setIconAndPosition(this);
        SplashHelper.getInstance().close();
        this.setVisible(true);
    }

    private File getEmailsFile() {
        return FileUtils.getFile(Directory.Customize.path(), "emails.txt");
    }

    private boolean validateEmails(List<String> emails) {
        for (String email : emails) {
            if (!RegexUtils.isEmail(email)) {
                LOGGER.error("Email invalid: {}", email);
                return false;
            }
        }
        return true;
    }

    private void loadEmailAndBottomLines() {
        for (String email : emails) {
            JTextField emailField = new JTextField();
            emailField.setText(email);
            emailField.setToolTipText(email);
            emailField.setPreferredSize(FIELD_SIZE);
            emailFields.add(emailField);

            JButton button = new JButton(BUTTON_START);
            button.setForeground(Color.WHITE);
            button.setBackground(BUTTON_STOPPED_COLOR);
            button.addActionListener(e -> handleControllerButtonClick(emailField, button));
            actionButtons.add(button);
        }

        int num = MAX_EMAILS_MUM - emailFields.size();
        for (int i = 0; i < num; i++) {
            JTextField blankField = new JTextField();
            blankField.setVisible(false);
            blankField.setPreferredSize(FIELD_SIZE);
            emailFields.add(blankField);

            JButton button = new JButton(BUTTON_START);
            button.setForeground(Color.WHITE);
            button.setBackground(BUTTON_STOPPED_COLOR);
            button.setVisible(false);
            button.addActionListener(e -> handleControllerButtonClick(blankField, button));
            actionButtons.add(button);
        }
    }

    private void handleControllerButtonClick(JTextField emailField, JButton button) {
        String email = emailField.getText();
        if (StringUtils.isBlank(email) || !RegexUtils.isEmail(email)) {
            UITools.error(String.format("Email provided is invalid.", email));
            return;
        }
        if (StringUtils.equals(button.getText(), BUTTON_START)) {
            proxyWebDriverContainer.startWebDriver(email);
            button.setText(BUTTON_CONTROL);
            button.setBackground(BUTTON_STARTED_COLOR);
        } else {
            if (UITools.confirmed("Click \"Yes\"Highlight the browser.\n" +
                "Click \"No\" to stop the browser.")) {
                activateBrowser(email);
                return;
            }
            proxyWebDriverContainer.stopWebDriver(email);
            button.setText(BUTTON_START);
            button.setBackground(BUTTON_STOPPED_COLOR);
        }
    }

    private void activateBrowser(String email) {
        proxyWebDriverContainer.highlightWebDriver(email);
    }

    private void initComponents() {
        this.setTitle(Constant.i18N.getText("title.emails.settings"));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel emailsPane = initEmailsPane();
        add(new JScrollPane(emailsPane));
        setPreferredSize(new Dimension(450, 400));
        pack();
    }

    private JPanel initEmailsPane() {
        JButton addButton = initAddButton();
        JButton saveButton = initSaveButton();

        JPanel panel = new JPanel();
        panel.setBorder(UITools.createTitledBorder(Constant.i18N.getText("title.buyer.emails")));
        GroupLayout layout = new GroupLayout(panel);

        panel.setLayout(layout);
        Group btnGroup = layout.createParallelGroup(Alignment.LEADING);
        Group horizontalGroup = layout.createParallelGroup(Alignment.LEADING);
        Group verticalGroup = layout.createSequentialGroup();

        for (int i = 0; i < emailFields.size(); i++) {
            JButton actionBtn = actionButtons.get(i);
            btnGroup = btnGroup.addComponent(actionBtn,
                BUTTON_WIDTH, BUTTON_WIDTH, BUTTON_WIDTH);

            JTextField emailField = emailFields.get(i);
            horizontalGroup = horizontalGroup.addGroup(layout.createSequentialGroup()
                .addComponent(emailField, FIELD_WIDTH, FIELD_WIDTH, FIELD_WIDTH));

            verticalGroup = verticalGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
                .addComponent(emailField).addComponent(actionBtn));
        }

        horizontalGroup = horizontalGroup.addGroup(layout.createSequentialGroup()
            .addComponent(saveButton, BUTTON_WIDTH, BUTTON_WIDTH, BUTTON_WIDTH));

        verticalGroup = verticalGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
            .addComponent(saveButton).addComponent(addButton));

        btnGroup = btnGroup.addComponent(addButton,
                BUTTON_WIDTH, BUTTON_WIDTH, BUTTON_WIDTH);

        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup().addContainerGap()
                .addGroup(horizontalGroup)
                .addGap(gap)
                .addGroup(btnGroup)));

        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(verticalGroup));
        return panel;
    }

    private JButton initSaveButton() {
        JButton saveButton = new JButton("Save");
        saveButton.setBackground(Color.decode("#17a2b8"));
        saveButton.setForeground(Color.WHITE);
        saveButton.setPreferredSize(new Dimension(90, 25));
        saveButton.addActionListener(e -> saveEmails());
        return saveButton;
    }

    private JButton initAddButton() {
        JButton addButton = new JButton("+ Add");
        addButton.setBackground(Color.decode("#4dc86f"));
        addButton.setForeground(Color.WHITE);
        addButton.setPreferredSize(new Dimension(90, 25));
        addButton.addActionListener(e -> showBlankEmailFields(addButton));
        return addButton;
    }

    private void showBlankEmailFields(JButton addButton) {
        for (int i = 0; i < emailFields.size(); i++) {
            JTextField emailField = emailFields.get(i);
            if (emailField.isVisible()) {
                continue;
            }
            actionButtons.get(i).setVisible(true);
            emailField.setVisible(true);
            return;
        }
        addButton.setVisible(false);
    }

    private void saveEmails() {
        emails.clear();
        for (JTextField emailField : emailFields) {
            String email = emailField.getText();
            if (StringUtils.isBlank(email) || !RegexUtils.isEmail(email)) {
                continue;
            }
            emails.add(email);
        }
        Tools.writeLinesToFile(getEmailsFile(), emails);
        UITools.info("Emails saved!");
    }

    @Override
    public void cleanUp() {
        ProcessCleaner.cleanWebDriver();
    }

    @Override
    public Application getApplication() {
        return null;
    }
}

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

    private List<String> emails = new ArrayList<>();
    private final List<JTextField> emailFields = new ArrayList<>();
    private final List<JButton> actionBtns = new ArrayList<>();
    private int verticalGap = 2;
    private int gap = 24;
    private static final String BUTTON_START = "Start";
    private static final String BUTTON_STOP = "Stop";
    @Inject private ProxyWebDriverContainer proxyWebDriverContainer;

    public void init() {
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
        this.setMinimumSize(new Dimension(1000, 600));
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
            emailFields.add(emailField);

            JButton button = new JButton(BUTTON_START);
            button.addActionListener(e -> handleButtonClick(email, button));
            actionBtns.add(button);
        }
    }

    private void handleButtonClick(String email, JButton button) {
        if (StringUtils.equals(button.getText(), BUTTON_START)) {
            proxyWebDriverContainer.startWebDriver(email);
            button.setText(BUTTON_STOP);
        } else {
            proxyWebDriverContainer.stopWebDriver(email);
            button.setText(BUTTON_START);
        }
    }

    private void initComponents() {
        this.setTitle(Constant.i18N.getText("title.emails.settings"));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel countriesPane = initEmailsPane();

        GroupLayout layout = new GroupLayout(getContentPane());
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(countriesPane, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)));

        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING,layout.createSequentialGroup()
                .addComponent(countriesPane)));
        getContentPane().setLayout(layout);
        pack();
    }

    private JPanel initEmailsPane() {
        JPanel panel = new JPanel();
        panel.setBorder(UITools.createTitledBorder(Constant.i18N.getText("title.buyer.emails")));
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        Group btnGroup = layout.createParallelGroup(Alignment.LEADING);
        Group horizontalGroup = layout.createParallelGroup(Alignment.LEADING);
        Group verticalGroup = layout.createSequentialGroup();
        for (int i = 0; i < emailFields.size(); i++) {
            btnGroup = btnGroup.addComponent(actionBtns.get(i),
                UITools.BUTTON_WIDTH, UITools.BUTTON_WIDTH, UITools.BUTTON_WIDTH);

            horizontalGroup = horizontalGroup.addGroup(layout.createSequentialGroup()
                .addComponent(emailFields.get(i), UITools.MIDDLE_TEXT_FIELD_WIDTH,
                    UITools.MIDDLE_TEXT_FIELD_WIDTH, Short.MAX_VALUE));

            verticalGroup = verticalGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
                .addComponent(emailFields.get(i)).addComponent(actionBtns.get(i)))
                .addGap(verticalGap);
        }

        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup().addContainerGap().addGroup(horizontalGroup)
            .addGap(gap).addGroup(btnGroup)));

        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(verticalGroup));
        return panel;
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

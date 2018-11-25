package com.amzass.ui;

import com.amzass.enums.common.ConfigEnums.LogMode;
import com.amzass.enums.common.Customize;
import com.amzass.service.common.ApplicationContext;
import com.amzass.utils.common.Tools;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/25/2018 1:32 AM
 */
public class ProxyBrowser {

    public static void main(String[] args) {
        UIBrowser uiBrowser = ApplicationContext.getBean(UIBrowser.class);
        uiBrowser.init();
        uiBrowser.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                uiBrowser.cleanUp();
                System.exit(0);
            }
        });
        uiBrowser.cleanUp();
        if (Customize.Debug.exist()) {
            Tools.switchLogMode(LogMode.Development);
        }
    }
}

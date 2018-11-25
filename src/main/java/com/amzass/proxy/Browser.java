package com.amzass.proxy;

import com.amzass.service.common.ApplicationContext;
import com.amzass.service.common.ProxyWebDriverContainer;
import com.amzass.utils.common.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/23/2018 5:06 AM
 */
public class Browser {
    private static final Logger LOGGER = LoggerFactory.getLogger(Browser.class);

    public static void main(String[] args) {
        ProxyWebDriverContainer container = ApplicationContext.getBean(ProxyWebDriverContainer.class);
        try {
            String profile = Tools.getCustomizingValue("PROFILE");
            container.getWebDriver(profile);
        } catch (Exception e) {
            LOGGER.error("Unable to start proxy browser: ", e);
        }
        System.exit(0);
    }
}

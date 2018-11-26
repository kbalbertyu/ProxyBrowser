package com.amzass.ui;

import com.amzass.service.common.ApplicationContext;
import com.amzass.service.common.ResourcePage;
import com.amzass.service.common.WebDriverManager;
import com.google.inject.Inject;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/23/2018 9:51 AM
 */
public class Fetcher {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Inject private WebDriverManager webDriverManager;

    private void execute() {
        WebDriver driver = webDriverManager.initChromeDriver();
        try {
            for (ResourcePage page : ResourcePage.values()) {
                LOGGER.info("Fetching proxy resources from: {}", page.name());
                page.abstractResource.fetchResources(driver);
            }
        } catch (Exception e) {
            LOGGER.error("Error found on fetching proxy resources.", e);
        } finally {
            driver.close();
        }
    }

    public static void main(String[] args) {
        ApplicationContext.getBean(Fetcher.class).execute();
        System.exit(0);
    }
}

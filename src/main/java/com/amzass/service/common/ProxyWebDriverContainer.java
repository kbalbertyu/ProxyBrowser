package com.amzass.service.common;

import com.amzass.enums.common.ConfigEnums;
import com.amzass.ui.utils.UITools;
import com.google.inject.Inject;
import com.mailman.model.common.Settings;
import com.mailman.service.common.AbstractWebDriverContainer;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/24/2018 8:15 AM
 */
public class ProxyWebDriverContainer extends AbstractWebDriverContainer {
    @Inject private ProxyManager proxyManager;
    @Inject private ProxyWebDriverManager proxyWebDriverManager;
    final Map<String, WebDriver> drivers = new HashMap<>();

    @Override
    public ConfigEnums.Browser getDriverType(Settings settings) {
        return null;
    }

    public WebDriver startWebDriver(String id) {
        String key = id.toLowerCase();
        WebDriver driver = drivers.get(key);
        if (driver == null) {
            driver = this.createDriver(id);
        }
        this.markUser();
        return driver;
    }

    private WebDriver createDriver(String id) {
        WebDriver driver = proxyManager.startProxyDriver(id);
        drivers.put(id.toLowerCase(), driver);
        return driver;
    }

    public void stopWebDriver(String id) {
        String key = id.toLowerCase();
        WebDriver driver = drivers.get(key);
        if (driver != null) {
            proxyWebDriverManager.closeDriver(driver);
            drivers.remove(key);
        }
    }

    public void highlightWebDriver(String id) {
        String key = id.toLowerCase();
        WebDriver driver = drivers.get(key);
        if (driver != null) {
            ((JavascriptExecutor) driver).executeScript("alert(\"" + id + "\")");
            return;
        }
        UITools.error(String.format("Unable to find the browser associated with: %s", id));
    }
}

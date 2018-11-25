package com.amzass.service.common;

import com.amzass.enums.common.ConfigEnums;
import com.amzass.utils.common.ProcessCleaner;
import com.google.inject.Inject;
import com.mailman.model.common.Settings;
import com.mailman.service.common.AbstractWebDriverContainer;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/24/2018 8:15 AM
 */
public class ProxyWebDriverContainer extends AbstractWebDriverContainer {
    @Inject private ProxyManager proxyManager;
    final Map<String, WebDriver> drivers = new HashMap<>();

    @Override
    public ConfigEnums.Browser getDriverType(Settings settings) {
        return null;
    }

    public WebDriver getWebDriver(String id) {
        String key = id.toLowerCase();
        WebDriver driver = drivers.get(key);
        if (driver == null) {
            driver = this.createDriver(id);
        }
        return driver;
    }

    /**
     * 终止容器中的所有WebDriver的进程
     */
    void terminateWebDrivers() {
        ProcessCleaner.cleanWebDriver();
    }

    private WebDriver createDriver(String id) {
        WebDriver driver = proxyManager.startProxyDriver(id);
        drivers.put(id.toLowerCase(), driver);
        return driver;
    }
}

package com.amzass.service.common;

import com.alibaba.fastjson.JSON;
import com.amzass.enums.common.ConfigEnums.ChromeDriverVersion;
import com.amzass.proxy.model.ProxyResource;
import com.amzass.utils.common.Constants;
import com.amzass.utils.common.PageUtils;
import com.amzass.utils.common.Tools;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/25/2018 12:01 AM
 */
class ProxyWebDriverManager extends WebDriverManager {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    WebDriver initChromeDriver(ProxyResource proxyResource) {
        ChromeDriverVersion driverVersion = Tools.defaultChromeDriver();
        if (driverVersion == null) {
            driverVersion = ChromeDriverVersion.values()[0];
        }
        System.setProperty(WebDriverManager.CHROME_DRIVER_KEY, driverVersion.filePath());
        DesiredCapabilities dCaps = DesiredCapabilities.chrome();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=Profile\\" + proxyResource.getProfile());
        dCaps.setCapability(ChromeOptions.CAPABILITY, options);

        LOGGER.info("Using proxy: {}", JSON.toJSONString(proxyResource));
        Proxy proxy = this.initProxy(proxyResource);

        dCaps.setCapability(CapabilityType.ForSeleniumServer.AVOIDING_PROXY, true);
        dCaps.setCapability(CapabilityType.ForSeleniumServer.ONLY_PROXYING_SELENIUM_TRAFFIC, true);
        System.setProperty("http.nonProxyHosts", "localhost");
        dCaps.setCapability(CapabilityType.PROXY, proxy);

        ChromeDriver driver = new ChromeDriver(dCaps);

        this.setTimeOut(Constants.DEFAULT_DRIVER_TIME_OUT, driver);
        PageUtils.maximize(driver);
        return driver;
    }

    private Proxy initProxy(ProxyResource proxyResource) {
        Proxy proxy = new Proxy();
        String proxyIpAndPort = proxyResource.getHost() + ":" + proxyResource.getPort();
        if (proxyResource.http()) {
            proxy.setHttpProxy(proxyIpAndPort);
        } else {
            proxy.setSslProxy(proxyIpAndPort);
        }
        return proxy;
    }
}

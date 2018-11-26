package com.amzass.service.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.amzass.proxy.model.ProxyResource;
import com.amzass.ui.utils.UITools;
import com.amzass.utils.PageLoadHelper.WaitTime;
import com.amzass.utils.common.Exceptions.BusinessException;
import com.amzass.utils.common.Tools;
import com.google.inject.Inject;
import com.mailman.model.common.WebApiResult;
import com.mailman.service.common.WebApiRequest;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/23/2018 5:08 AM
 */
class ProxyManager {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Inject private ProxyWebDriverManager proxyWebDriverManager;
    @Inject private WebApiRequest webApiRequest;
    private final static String FETCH_PROXY_PATH = "/proxy/fetch";
    private final static String DISABLE_PROXY_PATH = "/proxy/disable";
    private final static String VERIFY_PROXY_PATH = "/proxy/verify-proxy";
    private static String localIP = null;

    WebDriver startProxyDriver(String profile) {
        while (true) {
            if (StringUtils.isBlank(localIP)) {
                localIP = this.fetchLocalIP();
            }
            ProxyResource proxyResource = this.fetchProxyResource(profile);
            if (proxyResource == null) {
                String message = String.format("No available proxy for profile: %s", profile);
                UITools.error(message);
                throw new BusinessException(message);
            }
            WebDriver driver;
            try {
                driver = proxyWebDriverManager.initChromeDriver(proxyResource);
            } catch (WebDriverException e) {
                LOGGER.error("WebDriver exception: ", e);
                throw new BusinessException("Unable to start proxy browser.");
            }

            try {
                this.verifyProxy(driver, proxyResource);
                return driver;
            } catch (ProxyException | JSONException e) {
                LOGGER.error("Proxy unavailable: {}", JSON.toJSONString(proxyResource));
                this.disableProxy(proxyResource);
            }
            proxyWebDriverManager.closeDriver(driver);
        }
    }

    private String fetchLocalIP() {
        WebApiResult result = webApiRequest.get(VERIFY_PROXY_PATH + "?proxyHost=127.0.0.1");
        if (result == null) {
            return null;
        }
        return parseLocalIP(result);
    }

    private String parseLocalIP(WebApiResult result) {
        Map<String, String> data = JSON.parseObject(result.getData(), new TypeReference<Map<String, String>>() {});
        return data.get("ip");
    }

    private ProxyResource fetchProxyResource(String profile) {
        WebApiResult result = webApiRequest.get(FETCH_PROXY_PATH + "?profile=" + profile);
        if (result == null) {
            return null;
        }
        try {
            ProxyResource pr = JSON.parseObject(result.getData(), ProxyResource.class);
            pr.setProfile(profile);
            return pr;
        } catch (JSONException e) {
            LOGGER.error("Unable to parse proxy resource from text: {}", result.getData());
            return null;
        }
    }

    private void disableProxy(ProxyResource proxyResource) {
        Map<String, String> map = Tools.map(new String[] {"proxyHost"},
            new String[] {proxyResource.getHost()});
        webApiRequest.post(DISABLE_PROXY_PATH, JSON.toJSONString(map));
    }

    private void verifyProxy(WebDriver driver, ProxyResource proxyResource) {
        String url = WebApiRequest.getFullUrl(VERIFY_PROXY_PATH + "?proxyHost=" + proxyResource.getHost());
        driver.get(url);
        WaitTime.Normal.execute();
        Document doc = Jsoup.parse(driver.getPageSource());
        WebApiResult result = JSON.parseObject(StringUtils.trim(doc.body().text()), WebApiResult.class);

        String ip = parseLocalIP(result);
        if (StringUtils.equals(ip, localIP)) {
            throw new ProxyException();
        }
    }

    private static class ProxyException extends RuntimeException {
        private static final long serialVersionUID = -1612424532173515242L;
    }
}

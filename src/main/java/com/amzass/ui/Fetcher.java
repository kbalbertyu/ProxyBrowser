package com.amzass.ui;

import com.alibaba.fastjson.JSON;
import com.amzass.proxy.model.ProxyResource;
import com.amzass.service.common.ApplicationContext;
import com.amzass.service.common.WebDriverManager;
import com.amzass.utils.PageLoadHelper.WaitTime;
import com.google.inject.Inject;
import com.mailman.model.common.WebApiResult;
import com.mailman.service.common.WebApiRequest;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/23/2018 9:51 AM
 */
public class Fetcher {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private static final String IMPORT_PROXY_PATH = "/proxy/import";
    @Inject private WebDriverManager webDriverManager;
    @Inject private WebApiRequest webApiRequest;

    private void execute() {
        WebDriver driver = webDriverManager.initChromeDriver();
        try {
            driver.get("http://www.xicidaili.com/nt/");
            WaitTime.Normal.execute();
            Document doc = Jsoup.parse(driver.getPageSource());
            List<ProxyResource> proxyResources = this.parseProxyList(doc);
            int count = proxyResources.size();
            LOGGER.info("Found {} proxy IP resources.", count);
            if (count > 0) {
                WebApiResult result = webApiRequest.post(IMPORT_PROXY_PATH, JSON.toJSONString(proxyResources));
                if (result == null) {
                    LOGGER.error("Proxy resources import failed");
                } else {
                    LOGGER.info("Proxy resources import success: {}", JSON.toJSONString(result));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error found on fetching proxy resources.");
        } finally {
            driver.close();
        }
    }

    private List<ProxyResource> parseProxyList(Document doc) {
        List<ProxyResource> resources = new ArrayList<>();
        Elements trs = doc.select("#ip_list > tbody > tr:gt(1)");
        for (Element tr : trs) {
            ProxyResource resource = new ProxyResource();
            resource.setHost(StringUtils.trim(tr.select("td:eq(1)").text()));
            resource.setPort(StringUtils.trim(tr.select("td:eq(2)").text()));
            resource.setScheme(StringUtils.trim(tr.select("td:eq(5)").text()));
            if (!resource.valid()) {
                LOGGER.error("Proxy resource invalid: {}", JSON.toJSONString(resource));
                continue;
            }
            resources.add(resource);
        }

        return resources;
    }

    public static void main(String[] args) {
        ApplicationContext.getBean(Fetcher.class).execute();
        System.exit(0);
    }
}

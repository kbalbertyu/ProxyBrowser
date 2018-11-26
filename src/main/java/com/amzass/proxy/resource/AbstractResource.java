package com.amzass.proxy.resource;

import com.alibaba.fastjson.JSON;
import com.amzass.proxy.model.ProxyResource;
import com.amzass.service.common.ApplicationContext;
import com.amzass.service.sellerhunt.HtmlParser;
import com.amzass.utils.PageLoadHelper;
import com.amzass.utils.PageLoadHelper.WaitTime;
import com.amzass.utils.common.Constants;
import com.amzass.utils.common.DateHelper;
import com.amzass.utils.common.Exceptions.BusinessException;
import com.amzass.utils.common.JsoupWrapper.WebRequest;
import com.mailman.model.common.WebApiResult;
import com.mailman.service.common.WebApiRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/25/2018 8:49 PM
 */
public abstract class AbstractResource {
    private static final int MAX_DAYS_BEFORE = 3;
    static final int NOT_EXIST_INDEX = -1;
    public static final String NONE_EXIST_VALUE = "none";
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private static final String IMPORT_PROXY_PATH = "/proxy/import";
    private WebApiRequest webApiRequest = ApplicationContext.getBean(WebApiRequest.class);

    protected abstract String getDateFormat();
    protected abstract String getRowSelector();
    protected abstract String getUrl(int pageNo);
    protected abstract int getHostIndex();
    protected abstract int getPortIndex();
    protected abstract int getSchemeIndex();
    protected abstract int getDateIndex();

    public void fetchResources(WebDriver driver) {
        int pageNo = 1;
        while (true) {
            try {
                this.fetchResources(pageNo, driver);
                pageNo++;
            } catch (PageEndException e) {
                LOGGER.info("Page ended on {}: {}", pageNo, e.getMessage());
                break;
            } catch (BusinessException e) {
                LOGGER.error("Error found on page: {}", pageNo, e);
                break;
            }
        }
    }

    private Date parseDate(String dateText, String format) throws ParseException {
        String[] parts = StringUtils.split(dateText, StringUtils.SPACE);
        FastDateFormat dateFormat = FastDateFormat.getInstance(format);
        try {
            return dateFormat.parse(parts[0]);
        } catch (ParseException e) {
            LOGGER.error("Unable to parse date: {}", dateText);
            throw e;
        }
    }

    private void fetchResources(int pageNo, WebDriver driver) {
        Document doc = this.getDocument(pageNo, driver);
        List<ProxyResource> list = this.parseProxyList(doc);
        this.validateList(list);
        LOGGER.info("Found {} proxy IP resources on page {}.", list.size(), pageNo);
        WebApiResult result = webApiRequest.post(IMPORT_PROXY_PATH, JSON.toJSONString(list));
        if (result == null) {
            LOGGER.error("Proxy resources import failed");
        } else {
            LOGGER.info("Proxy resources import success: {}", JSON.toJSONString(result));
        }
    }

    private void validateList(List<ProxyResource> list) {
        if (list.size() == 0) {
            throw new BusinessException("No proxy resources found on page");
        }
        list.removeIf(resource -> Math.abs(DateHelper.daysBetween(new Date(), resource.getDate())) > MAX_DAYS_BEFORE);
        if (list.size() == 0) {
            throw new PageEndException();
        }
    }

    private Document getDocument(int pageNo, WebDriver driver) {
        String url = this.getUrl(pageNo);
        Document doc = this.getDocumentByJsoup(url);
        if (this.validateDocument(doc)) {
            return doc;
        }
        if (driver == null) {
            throw new BusinessException(String.format("Proxy resource page is invalid: %s", url));
        }
        driver.get(url);
        WaitTime.Normal.execute();
        doc = Jsoup.parse(driver.getPageSource());
        if (this.validateDocument(doc)) {
            return doc;
        }
        throw new BusinessException(String.format("Proxy resource page is invalid: %s", url));
    }

    private Document getDocumentByJsoup(String url) {
        Document doc = null;
        for (int i = 0; i < Constants.MAX_REPEAT_TIMES; i++) {
            try {
                doc = new WebRequest(url).submit().document;
                break;
            } catch (Exception e) {
                LOGGER.error("第{}次读取页面信息失败: {}", i + 1, url, e.getMessage());
                if (i < Constants.MAX_REPEAT_TIMES - 1) {
                    PageLoadHelper.WaitTime.Shorter.execute();
                }
            }
        }
        return doc;
    }

    private List<ProxyResource> parseProxyList(Document doc) {
        List<ProxyResource> resources = new ArrayList<>();
        Elements trs = doc.select(getRowSelector());
        for (Element tr : trs) {
            ProxyResource resource = new ProxyResource();
            resource.setHost(this.parseTextInRow(tr, getHostIndex()));
            resource.setPort(this.parseTextInRow(tr, getPortIndex()));
            resource.setScheme(this.parseTextInRow(tr, getSchemeIndex()));

            String dateText = this.parseTextInRow(tr, getDateIndex());
            try {
                resource.setDate(this.parseDate(dateText, getDateFormat()));
            } catch (ParseException e) {
                break;
            }
            if (!resource.valid()) {
                LOGGER.error("Proxy resource invalid: {}", JSON.toJSONString(resource));
                continue;
            }
            resources.add(resource);
        }

        return resources;
    }

    private String parseTextInRow(Element tr, int index) {
        if (index == NOT_EXIST_INDEX) {
            return NONE_EXIST_VALUE;
        }
        return StringUtils.trim(tr.select("td:eq(" + index + ")").text());
    }

    private boolean validateDocument(Document doc) {
        return HtmlParser.anyExist(doc, getRowSelector());
    }

    private class PageEndException extends RuntimeException {
        private static final long serialVersionUID = 1234564065240518738L;

        private PageEndException() {

        }
    }
}

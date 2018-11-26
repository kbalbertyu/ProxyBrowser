package com.amzass.proxy.resource;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/25/2018 10:49 PM
 */
public class KuaiDaili extends AbstractResource {
    private static final String URL = "https://www.kuaidaili.com/free/inha/%d";

    @Override
    protected String getDateFormat() {
        return "yyyy-M-d";
    }

    @Override
    protected String getRowSelector() {
        return "div#list > table.table > tbody > tr";
    }

    @Override
    protected String getUrl(int pageNo) {
        return String.format(URL, pageNo);
    }

    @Override
    protected int getHostIndex() {
        return 0;
    }

    @Override
    protected int getPortIndex() {
        return 1;
    }

    @Override
    protected int getSchemeIndex() {
        return 3;
    }

    @Override
    protected int getDateIndex() {
        return 6;
    }
}

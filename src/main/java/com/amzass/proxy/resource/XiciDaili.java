package com.amzass.proxy.resource;

import com.mailman.service.common.DateFormatHolder;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/25/2018 8:47 PM
 */
public class XiciDaili extends AbstractResource {
    private static final String URL = "http://www.xicidaili.com/nt/%d";

    @Override
    protected String getDateFormat() {
        return DateFormatHolder.YY_M_D;
    }

    @Override
    protected String getRowSelector() {
        return "#ip_list > tbody > tr:gt(1)";
    }

    @Override
    protected String getUrl(int pageNo) {
        return String.format(URL, pageNo);
    }

    @Override
    protected int getHostIndex() {
        return 1;
    }

    @Override
    protected int getPortIndex() {
        return 2;
    }

    @Override
    protected int getSchemeIndex() {
        return 5;
    }

    @Override
    protected int getDateIndex() {
        return 9;
    }
}
package com.amzass.proxy.resource;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/25/2018 11:01 PM
 */
public class CN89IP extends AbstractResource {
    private static final String URL = "http://www.89ip.cn/index_%d.html";

    @Override
    protected String getDateFormat() {
        return "yyyy/M/d";
    }

    @Override
    protected String getRowSelector() {
        return "table.layui-table > tbody > tr";
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
        return NOT_EXIST_INDEX;
    }

    @Override
    protected int getDateIndex() {
        return 4;
    }
}

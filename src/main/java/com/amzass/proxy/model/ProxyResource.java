package com.amzass.proxy.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.amzass.utils.common.RegexUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Date;

import static com.amzass.proxy.resource.AbstractResource.NONE_EXIST_VALUE;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/23/2018 3:10 AM
 */
@Data
public class ProxyResource {
    private String scheme;
    private String host;
    private String port;
    private String profile;
    @JSONField(serialize = false)
    private Date date;

    public boolean http() {
        return StringUtils.equalsIgnoreCase(scheme, "http") ||
            StringUtils.equalsIgnoreCase(scheme, NONE_EXIST_VALUE);
    }

    public boolean https() {
        return StringUtils.equalsIgnoreCase(scheme, "https") ||
            StringUtils.equalsIgnoreCase(scheme, NONE_EXIST_VALUE);
    }

    private boolean portValid() {
        int length = StringUtils.length(port);
        return length >= 2 && length <= 5 && NumberUtils.isNumber(port);
    }

    private boolean hostValid() {
        String regex = "\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}";
        return RegexUtils.match(host, regex);
    }

    public boolean valid() {
        return (http() || https()) &&
            portValid() &&
            hostValid();
    }

    public static void main(String[] args) {
        ProxyResource pr = new ProxyResource();
        pr.setScheme("HTTPS");
        pr.setPort("23");
        pr.setHost("110.53.61.64");
        System.out.println(pr.valid());
    }
}

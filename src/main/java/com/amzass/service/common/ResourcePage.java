package com.amzass.service.common;

import com.amzass.proxy.resource.AbstractResource;
import com.amzass.proxy.resource.CN89IP;
import com.amzass.proxy.resource.KuaiDaili;
import com.amzass.proxy.resource.XiciDaili;

/**
 * @author <a href="mailto:kbalbertyu@gmail.com">Albert Yu</a> 11/25/2018 9:25 PM
 */
public enum ResourcePage {
    XiciDali(new XiciDaili()),
    KuaiDaili(new KuaiDaili()),
    CN89IP(new CN89IP());

    ResourcePage(AbstractResource abstractResource) {
        this.abstractResource = abstractResource;
    }

    public AbstractResource abstractResource;
}

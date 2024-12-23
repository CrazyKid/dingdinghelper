package com.crazykid.utils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author arthur
 * @date 2024/12/23 17:54
 */
public class AlarmProxyAddressUtils {
    /**
     * 获取正向代理的ip池子
     *
     * @param proxyIpList 配置文件中读取的ip池子, 格式 "192.168.100.100:11328,192.168.100.101:11328"
     * @return
     */
    public static List<InetSocketAddress> getProxyAddress(String proxyIpList) {
        List<InetSocketAddress> list = new ArrayList<>();
        if (proxyIpList == null || proxyIpList.isEmpty()) {
            return list;
        }

        String[] split = proxyIpList.split(",");
        for (String s : split) {
            String[] one = s.split(":");
            list.add(new InetSocketAddress(one[0], Integer.parseInt(one[1])));
        }
        return list;
    }
}

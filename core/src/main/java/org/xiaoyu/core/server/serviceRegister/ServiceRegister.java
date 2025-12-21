package org.xiaoyu.core.server.serviceRegister;

import java.net.InetSocketAddress;

public interface ServiceRegister {
    void register(String serviceName, InetSocketAddress address, boolean canRetry);
}

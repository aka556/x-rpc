# x-rpc
基于Netty+Zookeeper实现的RPC框架

## 项目介绍

x-rpc是一个基于Netty和Zookeeper实现的RPC框架，提供了服务注册发现、负载均衡、容错机制等功能。

## 核心功能

### 负载均衡策略
- 随机负载均衡（RandomLoadBalance）
- 轮询负载均衡（RoundLoadBalance）
- 一致性哈希负载均衡（ConsistencyHashLoadBalance）
- 最近最久未使用（LRU）负载均衡（LRULoadBalance）

### 服务注册与发现
使用Zookeeper作为服务注册中心，实现服务的动态注册与发现。

### 容错机制
- **重试机制**：通过`@Retryable`注解标识需要重试的方法
- **熔断器**：防止服务雪崩
- **限流器**：控制服务访问频率
- **故障降级**：在服务异常时提供降级响应，保证系统稳定性

### 网络通信
- 基于Netty实现高效的网络通信
- 支持心跳机制，维持长连接
- 异步非阻塞处理请求

## 使用示例

### 1. 定义服务接口
```java
public interface UserService {
    @Retryable
    User getUserById(Integer id);

    @Retryable
    Integer insertUser(User user);
}
```

### 2. 实现服务接口
```java
@Slf4j
public class UserServiceImpl implements UserService {
    @Override
    public User getUserById(Integer id) {
        // 实现逻辑
        return user;
    }

    @Override
    public Integer insertUser(User user) {
        // 实现逻辑
        return user.getId();
    }
}
```

### 3. 启动服务提供者
```java
public class ProviderTest {
    public static void main(String[] args) throws InterruptedException {
        RpcApplication.initialize();
        String ip = RpcApplication.getRpcConfig().getHost();
        int port = RpcApplication.getRpcConfig().getPort();

        UserService userService = new UserServiceImpl();
        ServiceProvider serviceProvider = new ServiceProvider(ip, port);
        serviceProvider.provideServiceInterface(userService);

        RpcServer rpcServer = new NettyRpcServer(serviceProvider);
        rpcServer.start(port);
        log.info("RPC 服务已启动，监听端口为 {}", port);
    }
}
```

### 4. 启动服务消费者
```java
public class ConsumerTest {
    public static void main(String[] args) {
        RpcApplication.initialize();

        // 获取代理对象
        ClientProxyFactory factory = new ClientProxyFactory();
        UserService userService = factory.getProxy(UserService.class);

        // 调用远程方法
        User user = userService.getUserById(1);
        System.out.println(user);
    }
}
```

## 模块说明

- **api**：定义服务接口和数据传输对象
- **common**：提供通用组件，如序列化器、异常处理、SPI加载等
- **core**：核心功能实现，包括客户端/服务端、代理、负载均衡、熔断等
- **provider**：服务提供者实现
- **consumer**：服务消费者实现

## 技术栈

- **网络通信**：Netty
- **服务注册中心**：Zookeeper
- **序列化**：Hessian、Kryo、Protostuff、JSON等
- **负载均衡**：随机、轮询、一致性哈希、LRU
- **容错机制**：重试、熔断、限流、故障降级

## 特性

- 支持多种序列化方式
- 支持多种负载均衡策略
- 完善的容错机制
- 分布式服务注册发现
- 高性能网络通信
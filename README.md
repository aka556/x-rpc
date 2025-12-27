# x-rpc

x-rpc 是一个轻量级的远程过程调用（RPC）框架，用于实现分布式系统中的服务调用。该项目基于Java开发，使用Netty作为网络通信框架，并集成了多种序列化方式、负载均衡策略和注册中心。

## 项目特点

- **高性能**：基于Netty实现异步非阻塞网络通信
- **多种序列化支持**：支持JDK原生序列化、JSON、Kryo、Hessian、Protostuff等多种序列化方式
- **负载均衡**：支持随机、轮询和一致性哈希等负载均衡策略
- **服务注册与发现**：集成Zookeeper作为服务注册中心
- **容错机制**：包含熔断器、重试机制等容错策略
- **分布式追踪**：支持分布式链路追踪功能

## 项目架构

x-rpc项目采用模块化设计，主要包含以下模块：

### 1. api模块
定义了服务接口和数据传输对象（DTO），为服务提供者和消费者提供统一的接口规范。

### 2. common模块
包含项目中通用的工具类、异常处理、消息定义和序列化器等基础组件。

### 3. core模块
RPC框架的核心实现，包括：
- 客户端和服务端的实现
- 代理机制
- 负载均衡算法
- 服务注册与发现
- 熔断器和限流器
- 网络通信处理

### 4. consumer模块
服务消费者，通过代理机制调用远程服务。

### 5. provider模块
服务提供者，实现具体的服务接口并对外提供服务。

## 核心功能

### 序列化支持
框架支持多种序列化方式：
- JDK原生序列化（ObjectSerializer）
- JSON序列化（JsonSerializer）
- Kryo序列化（KryoSerializer）
- Hessian序列化（HessianSerializer）
- Protostuff序列化（ProtostuffSerializer）

### 负载均衡策略
- 随机负载均衡（RandomLoadBalance）
- 轮询负载均衡（RoundLoadBalance）
- 一致性哈希负载均衡（ConsistencyHashLoadBalance）

### 服务注册与发现
使用Zookeeper作为服务注册中心，实现服务的动态注册与发现。

### 容错机制
- **重试机制**：通过`@Retryable`注解标识需要重试的方法
- **熔断器**：防止服务雪崩
- **限流器**：控制服务访问频率

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

### 4. 调用远程服务
```java
public class ConsumerTest {
    public static void main(String[] args) {
        ClientProxy clientProxy = new ClientProxy();
        UserService proxy = clientProxy.getProxy(UserService.class);

        User user = proxy.getUserById(1);
        log.info("从服务端得到的user={}", user);
    }
}
```

## 配置说明

RPC框架支持以下配置项：

- `name`: 服务名称，默认为"rpc"
- `port`: 服务端口，默认为9999
- `host`: 主机地址，默认为"localhost"
- `version`: 服务版本，默认为"1.0.0"
- `registry`: 注册中心，默认使用Zookeeper
- `serializer`: 序列化器，默认使用Hessian
- `balance`: 负载均衡策略，默认使用一致性哈希

## 技术栈

- Java 17
- Netty 4.1.51.Final
- Zookeeper
- Spring Boot
- Maven
- Lombok
- Hutool
- Kryo
- Hessian
- Protostuff

## 项目启动

1. 启动Zookeeper服务
2. 运行ProviderTest启动服务提供者
3. 运行ConsumerTest启动服务消费者

## 扩展性

x-rpc框架设计具有良好的扩展性，可以通过SPI机制轻松扩展：
- 新增序列化方式
- 实现新的负载均衡策略
- 集成其他注册中心
- 添加新的网络通信协议

## 许可证

本项目采用MIT许可证。
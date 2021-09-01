# dynamic-arthas-spring-boot-starter
参考在[SpringBoot Admin集成Arthas实践](https://github.com/alibaba/arthas/issues/1601) ,在`com.taobao.arthas:arthas-spring-boot-starter`的基础上提供Arthas动态开关的效果
## Maven坐标
```xml
<dependency>
    <groupId>com.wf2311</groupId>
    <artifactId>dynamic-arthas-spring-boot-starter</artifactId>
    <version>2021.07-SNAPSHOT</version>
</dependency>
```
## 动态开关配置
是否启用arthas是通过`spring.arthas.enabled`属性进行来控制的，默认为false，即默认不启用Arthas

可以通过Nacos等配置中心设置`spring.arthas.enabled`来动态开启或关闭Arthas

## 参数配置
项目中引入`dynamic-arthas-spring-boot-starter`后，通过以下参数配置连接至 Arthas Tunnel Server

> 以下参数就是`com.taobao.arthas:arthas-spring-boot-starter`中的配置参数。如果不需要动态开关功能也可以直接引用`com.taobao.arthas:arthas-spring-boot-starter`
> 
_注意：为保证在Arthas Tunnel Server的页面上能够显示应用名，参数`arthas.agent-id=${spring.application.name}@${random.value}`中的格式需与[应用名分隔符配置](../README.md/#应用名分隔符配置)中说明的配置保持一致_

```yaml
arthas:
  tunnel-server: ws://<ip>:<port>/ws  #<ip>、<port>分别Arthas Tunnel Server的IP和websocket端口号
  #客户端id,应用名@随机值，tunnel server会截取分隔符@前面的字符串作为应用名
  agent-id: ${spring.application.name}@${random.value}
  http-port: 0  #为0表示随机
  telnet-port: 0  #为0表示随机
```
或
```properties
#<ip>、<port>分别Arthas Tunnel的IP和websocket端口号
arthas.tunnel-server=ws://<ip>:<port>/ws
#客户端id,应用名@随机值，tunnel server会截取分隔符@前面的字符串作为应用名
arthas.agent-id=${spring.application.name}@${random.value}
#为0表示随机
arthas.http-port=0
#为0表示随机
arthas.telnet-port=0
```

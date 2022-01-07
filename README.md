# ftpclient-spring-boot-starter 
FTP Client 操作包 

#使用方式
自己下载install引入使用

```xml
<dependency>
    <groupId>cn.darkjrong</groupId>
    <artifactId>ftpclient-spring-boot-starter</artifactId>
    <version>1.0</version>
</dependency>
```

yml配置，必须配置enabled: true，否则默认false不起作用
```yaml
ftp:
    enabled: true
    host: 192.168.205.105
    port: 21
    baseDir: /data
    username: admin
    password: 123456
```

API 操作

```java

@Autowired
private FtpClientTemplate ftpClientTemplate;
```

































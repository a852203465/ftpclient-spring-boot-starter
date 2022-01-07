package cn.darkjrong.spring.boot.autoconfigure;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ftp客户端自动配置
 *
 * @author Rong.Jia
 * @date 2022/01/06
 */
@Configuration
@EnableConfigurationProperties(FtpClientProperties.class)
@ConditionalOnProperty(prefix = "ftp", name = "enabled", havingValue = "true")
public class FtpClientAutoConfiguration {

    private final FtpClientProperties ftpClientProperties;

    public FtpClientAutoConfiguration(final FtpClientProperties ftpClientProperties) {
        this.ftpClientProperties = ftpClientProperties;
    }

    @Bean
    public FtpClientFactoryBean ftpClientFactoryBean(GenericObjectPool<FTPClient> pool) {
        return new FtpClientFactoryBean(ftpClientProperties, pool);
    }



}

package cn.darkjrong.ftpclient.pool;

import cn.darkjrong.spring.boot.autoconfigure.FtpClientProperties;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ftp客户端池配置
 *
 * @author Rong.Jia
 * @date 2022/01/06
 */
@Configuration
public class FtpClientPoolConfig {

    private final FtpClientProperties ftpClientProperties;
    private final FtpClientPoolFactory ftpClientPoolFactory;

    public FtpClientPoolConfig(FtpClientProperties ftpClientProperties, FtpClientPoolFactory ftpClientPoolFactory) {
        this.ftpClientProperties = ftpClientProperties;
        this.ftpClientPoolFactory = ftpClientPoolFactory;
    }

    @Bean
    public GenericObjectPool<FTPClient> ftpClientPool() {

        GenericObjectPoolConfig<FTPClient> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxTotal(ftpClientProperties.getPool().getMaxActive());
        genericObjectPoolConfig.setMaxIdle(ftpClientProperties.getPool().getMaxIdle());
        genericObjectPoolConfig.setMinIdle(ftpClientProperties.getPool().getMinIdle());
        genericObjectPoolConfig.setMaxWaitMillis(ftpClientProperties.getPool().getMaxWait());
        return new GenericObjectPool<>(ftpClientPoolFactory, genericObjectPoolConfig);
    }




}

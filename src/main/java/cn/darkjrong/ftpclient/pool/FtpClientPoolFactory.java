package cn.darkjrong.ftpclient.pool;

import cn.darkjrong.ftpclient.exceptions.FtpClientException;
import cn.darkjrong.spring.boot.autoconfigure.FtpClientProperties;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * ftp客户端池工厂
 *
 * @author Rong.Jia
 * @date 2022/01/06
 */
@Configuration
public class FtpClientPoolFactory extends BasePooledObjectFactory<FTPClient> {

    private static final Logger logger = LoggerFactory.getLogger(FtpClientPoolFactory.class);
    private final FtpClientProperties ftpClientProperties;

    public FtpClientPoolFactory(FtpClientProperties ftpClientProperties) {
        this.ftpClientProperties = ftpClientProperties;
    }

    /**
     * 创建对象
     * @return FTPClient
     * @throws Exception
     */
    @Override
    public FTPClient create() throws Exception {
        return connectFtpServer(ftpClientProperties);
    }

    /**
     * 封装对象放入池中
     * @param ftpClient 对象
     * @return 池
     */
    @Override
    public PooledObject<FTPClient> wrap(FTPClient ftpClient) {
        return new DefaultPooledObject<>(ftpClient);
    }

    /**
     * 销毁FtpClient对象
     * @param ftpPooled
     * @throws Exception
     */
    @Override
    public void destroyObject(PooledObject<FTPClient> ftpPooled) throws Exception {
        if (ftpPooled == null) {
            return;
        }
        FTPClient ftpClient = ftpPooled.getObject();
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
            }
        } catch (Exception io) {
            logger.error("ftp client logout failed...{}", io.getMessage());
        } finally {
            try {
                ftpClient.disconnect();
            } catch (Exception io) {
                logger.error("close ftp client failed...{}", io.getMessage());
            }
        }


    }

    /**
     * 验证FtpClient对象
     * @param ftpPooled
     * @return
     */
    @Override
    public boolean validateObject(PooledObject<FTPClient> ftpPooled) {
        try {
            FTPClient ftpClient = ftpPooled.getObject();
            return ftpClient.sendNoOp();
        } catch (Exception e) {
            logger.error("Failed to validate client: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 连接ftp服务器
     *
     * @param ftpClientProperties ftp客户端属性
     * @return {@link FTPClient}
     */
    private FTPClient connectFtpServer(FtpClientProperties ftpClientProperties){
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(ftpClientProperties.getConnectTimeout());
        ftpClient.setControlEncoding(ftpClientProperties.getEncoding());
        if (ftpClientProperties.isPassiveMode()) {
            ftpClient.enterLocalPassiveMode();
        }
        try {
            ftpClient.connect(ftpClientProperties.getHost(), ftpClientProperties.getPort());
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)){
                logger.error("connect ftp {} failed", ftpClientProperties.getHost());
                ftpClient.disconnect();
                throw new FtpClientException(String.format("connect ftp %s failed", ftpClientProperties.getHost()));
            }

            if (!ftpClient.login(ftpClientProperties.getUsername(), ftpClientProperties.getPassword())) {
                logger.warn("ftpClient login failed... username is {}; password: {}", ftpClientProperties.getUsername(), ftpClientProperties.getPassword());
            }

            ftpClient.setBufferSize(ftpClientProperties.getBufferSize());
            ftpClient.setFileType(ftpClientProperties.getTransferFileType());

        } catch (Exception e) {
            logger.error("connect fail ------->>> {}",e.getCause());
            throw new FtpClientException(e);
        }
        return ftpClient;
    }

}

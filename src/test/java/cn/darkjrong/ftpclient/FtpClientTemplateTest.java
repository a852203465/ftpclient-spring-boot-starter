package cn.darkjrong.ftpclient;

import cn.darkjrong.ftpclient.pool.FtpClientPoolConfig;
import cn.darkjrong.ftpclient.pool.FtpClientPoolFactory;
import cn.darkjrong.spring.boot.autoconfigure.FtpClientFactoryBean;
import cn.darkjrong.spring.boot.autoconfigure.FtpClientProperties;
import cn.hutool.core.io.FileUtil;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

public class FtpClientTemplateTest {

    private FtpClientTemplate ftpClientTemplate;

    @BeforeEach
    public void setUp() throws Exception {

        FtpClientProperties ftpClientProperties = new FtpClientProperties();
        ftpClientProperties.setHost("10.20.241.54");
        ftpClientProperties.setPort(21);
        ftpClientProperties.setUsername("admin");
        ftpClientProperties.setPassword("123456");
        ftpClientProperties.setConnectTimeout(6000);
        ftpClientProperties.setEncoding("utf-8");
        ftpClientProperties.setBaseDir("/data");

        FtpClientPoolFactory ftpClientPoolFactory = new FtpClientPoolFactory(ftpClientProperties);
        FtpClientPoolConfig ftpClientPoolConfig = new FtpClientPoolConfig(ftpClientProperties, ftpClientPoolFactory);
        FtpClientFactoryBean factory = new FtpClientFactoryBean(ftpClientProperties, ftpClientPoolConfig.ftpClientPool());
        factory.afterPropertiesSet();
        ftpClientTemplate = factory.getObject();

    }

    @Test
    public void uploadFile() {

        File file = new File("F:\\1.jpg");

        System.out.println(ftpClientTemplate.uploadFile(file));

    }

    @Test
    public void uploadFile1() {

        File file = new File("F:\\1.jpg");
        System.out.println(ftpClientTemplate.uploadFile(FileUtil.readBytes(file), file.getName()));


    }

    @Test
    public void deleteFile() {
        System.out.println(ftpClientTemplate.deleteFile("/data/1.jpg"));
    }

    @Test
    public void getSize() {

        String size = ftpClientTemplate.getSize("/data/1.jpg");
        System.out.println(size);

    }

    @Test
    public void getStatus() {
        System.out.println(ftpClientTemplate.getStatus("/data/1.jpg"));
    }

    @Test
    public void getSystemType() {
        System.out.println(ftpClientTemplate.getSystemType());
    }

    @Test
    public void listFiles() {
        List<FTPFile> ftpFiles = ftpClientTemplate.listFiles("/data");
        for (FTPFile ftpFile : ftpFiles) {
            System.out.println(ftpFile.getName());
        }
    }

    @Test
    public void removeDirectory() {
        System.out.println(ftpClientTemplate.removeDirectory("/data"));
    }

    @Test
    public void rename() {
        System.out.println(ftpClientTemplate.rename("/data/1.jpg", "/data/2.jpg"));


    }

    @Test
    public void downloadFile() {

        byte[] bytes = ftpClientTemplate.downloadFile("/data/2.jpg");
        FileUtil.writeBytes(bytes, new File("F:\\1.jpg"));

    }





}

package cn.darkjrong.ftpclient;

import cn.darkjrong.ftpclient.exceptions.FtpClientException;
import cn.darkjrong.spring.boot.autoconfigure.FtpClientProperties;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPFileFilters;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * ftp 操作
 *
 * @author Rong.Jia
 * @date 2022/01/06
 */
public class FtpClientTemplate {

    private static final Logger logger = LoggerFactory.getLogger(FtpClientTemplate.class);
    private final FtpClientProperties ftpClientProperties;
    private final GenericObjectPool<FTPClient> pool;

    public FtpClientTemplate(FtpClientProperties ftpClientProperties, GenericObjectPool<FTPClient> pool) {
        this.ftpClientProperties = ftpClientProperties;
        this.pool = pool;
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @return {@link Boolean}
     */
    public Boolean uploadFile(File file) {
        return uploadFile(file, ftpClientProperties.getBaseDir());
    }

    /**
     * 上传文件
     *
     * @param file            文件
     * @param remoteDirectory 远程文件的名字
     * @return {@link Boolean}
     */
    public Boolean uploadFile(File file, String remoteDirectory) {
        return uploadFile(FileUtil.readBytes(file), remoteDirectory, file.getName());
    }

    /**
     * 上传文件
     *
     * @param bytes          字节
     * @param remoteFileName 远程文件的名字
     * @return {@link Boolean}
     */
    public Boolean uploadFile(byte[] bytes, String remoteFileName) {
        return uploadFile(bytes, ftpClientProperties.getBaseDir(), remoteFileName);
    }

    /**
     * 上传文件
     *
     * @param bytes           字节
     * @param remoteDirectory 远程目录
     * @param remoteFileName  远程文件的名字
     * @return {@link Boolean}
     */
    public Boolean uploadFile(byte[] bytes, String remoteDirectory, String remoteFileName) {
        Assert.isFalse(ArrayUtil.isEmpty(bytes), "文件字节不能空");
        return uploadFile(IoUtil.toStream(bytes), remoteDirectory, remoteFileName);
    }

    /**
     * 上传文件
     *
     * @param inputStream     输入流
     * @param remoteDirectory 远程目录
     * @param remoteFileName  远程文件的名字
     * @return {@link Boolean}
     */
    private Boolean uploadFile(InputStream inputStream, String remoteDirectory, String remoteFileName) {

        FTPClient ftpClient = null;
        try {
            ftpClient = pool.borrowObject();
            makeDirectories(ftpClient, remoteDirectory);
            if (changeWorkingDirectory(ftpClient, remoteDirectory)) {
                storeFile(ftpClient, assemblyFileName(remoteDirectory, remoteFileName), inputStream);
                return Boolean.TRUE;
            }
        }catch (Exception e) {
            logger.error("Can't upload {}/{}, Exception : {}", remoteDirectory, remoteFileName, e.getMessage());
        }finally {
            close(inputStream);
            returnObject(ftpClient);
        }

        return Boolean.FALSE;
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @return {@link Boolean}
     */
    public Boolean appendFile(File file) {
        return appendFile(file, ftpClientProperties.getBaseDir());
    }

    /**
     * 上传文件
     *
     * @param file            文件
     * @param remoteDirectory 远程文件的名字
     * @return {@link Boolean}
     */
    public Boolean appendFile(File file, String remoteDirectory) {
        return appendFile(IoUtil.toStream(file), remoteDirectory, file.getName());
    }

    /**
     * 上传文件
     *
     * @param bytes          字节
     * @param remoteFileName 远程文件的名字
     * @return {@link Boolean}
     */
    public Boolean appendFile(byte[] bytes, String remoteFileName) {
        return appendFile(bytes, ftpClientProperties.getBaseDir(), remoteFileName);
    }

    /**
     * 上传文件
     *
     * @param bytes           字节
     * @param remoteDirectory 远程目录
     * @param remoteFileName  远程文件的名字
     * @return {@link Boolean}
     */
    public Boolean appendFile(byte[] bytes, String remoteDirectory, String remoteFileName) {
        Assert.isFalse(ArrayUtil.isEmpty(bytes), "文件字节不能空");
        return appendFile(IoUtil.toStream(bytes), remoteDirectory, remoteFileName);
    }

    /**
     * 追加文件
     *
     * @param inputStream     输入流
     * @param remoteDirectory 远程目录
     * @param remoteFileName  远程文件的名字
     * @return {@link Boolean}
     */
    private Boolean appendFile(InputStream inputStream, String remoteDirectory, String remoteFileName) {

        FTPClient ftpClient = null;
        try {
            ftpClient = pool.borrowObject();
            makeDirectories(ftpClient, remoteDirectory);
            if (changeWorkingDirectory(ftpClient, remoteDirectory)) {
                ftpClient.appendFile(assemblyFileName(remoteDirectory, remoteFileName), inputStream);
                return Boolean.TRUE;
            }
        }catch (Exception e) {
            logger.error("Can't append {}/{}, Exception : {}", remoteDirectory, remoteFileName, e.getMessage());
        }finally {
            close(inputStream);
            returnObject(ftpClient);
        }

        return Boolean.FALSE;
    }

    /**
     * 删除文件
     *
     * @param pathName 路径名
     * @return {@link Boolean}
     */
    public Boolean deleteFile(String pathName) {

        FTPClient ftpClient = null;
        try {
            ftpClient = pool.borrowObject();
            return ftpClient.deleteFile(pathName);
        }catch (Exception e) {
            logger.error("Can't delete {}, Exception : {}", pathName, e.getMessage());
        }finally {
            returnObject(ftpClient);
        }

        return Boolean.FALSE;
    }

    /**
     * 获取大小
     *
     * @param pathName 路径名
     * @return {@link String}
     * @throws FtpClientException ftp客户端异常
     */
    public String getSize(String pathName) throws FtpClientException {

        FTPClient ftpClient = null;
        try {
            ftpClient = pool.borrowObject();
           return ftpClient.getSize(pathName);
        }catch (Exception e) {
            logger.error("Can't getSize {}, Exception : {}", pathName, e.getMessage());
            throw new FtpClientException(e);
        }finally {
            returnObject(ftpClient);
        }
    }

    /**
     * 获取状态
     *
     * @param pathName 路径名
     * @return {@link String}
     * @throws FtpClientException ftp客户端异常
     */
    public String getStatus(String pathName) throws FtpClientException {

        FTPClient ftpClient = null;
        try {
            ftpClient = pool.borrowObject();
            return ftpClient.getStatus(pathName);
        }catch (Exception e) {
            logger.error("Can't getStatus {}, Exception : {}", pathName, e.getMessage());
            throw new FtpClientException(e);
        }finally {
            returnObject(ftpClient);
        }

    }

    /**
     * 获取系统类型
     *
     * @return {@link String}
     * @throws FtpClientException ftp客户端异常
     */
    public String getSystemType() throws FtpClientException {
        FTPClient ftpClient = null;
        try {
            ftpClient = pool.borrowObject();
            return ftpClient.getSystemType();
        }catch (Exception e) {
            logger.error("Can't getSystemType, Exception : {}", e.getMessage());
            throw new FtpClientException(e);
        }finally {
            returnObject(ftpClient);
        }
    }

    /**
     * 列表文件
     *
     * @param pathName 路径名
     * @param filter   过滤器
     * @return {@link List}<{@link FTPFile}>
     */
    public List<FTPFile> listFiles(String pathName, FTPFileFilter filter) {
        FTPClient ftpClient = null;
        try {
            ftpClient = pool.borrowObject();
            return CollectionUtil.newArrayList(ftpClient.listFiles(pathName, filter));
        }catch (Exception e) {
            logger.error("Cannot obtain all files of {}, Exception : {}", pathName, e.getMessage());
            throw new FtpClientException(e);
        }finally {
            returnObject(ftpClient);
        }
    }

    /**
     * 列表文件
     *
     * @param pathName 路径名
     * @return {@link List}<{@link FTPFile}>
     */
    public List<FTPFile> listFiles(String pathName) {
        return listFiles(pathName, FTPFileFilters.ALL);
    }

    /**
     * 删除目录
     *
     * @param pathName 路径名
     * @return {@link Boolean}
     */
    public Boolean removeDirectory(String pathName) {
        FTPClient ftpClient = null;
        try {
            ftpClient = pool.borrowObject();
            List<FTPFile> ftpFileList = listFiles(pathName);
            if (CollectionUtil.isNotEmpty(ftpFileList)) {
                ftpFileList.forEach(a -> deleteFile(assemblyFileName(pathName, a.getName())));
            }
            if (CollectionUtil.isEmpty(listFiles(pathName))) {
                return ftpClient.removeDirectory(pathName);
            }
        }catch (Exception e) {
            logger.error("Can't remove directory {}, Exception : {}", pathName, e.getMessage());
        }finally {
            returnObject(ftpClient);
        }

        return Boolean.FALSE;
    }

    /**
     * 重命名
     *
     * @param srcPathName 原文件，含目录
     * @param targetPathName  目标文件，含目录
     * @return {@link Boolean}
     */
    public Boolean rename(String srcPathName, String targetPathName) {

        FTPClient ftpClient = null;
        try {
            ftpClient = pool.borrowObject();
            return ftpClient.rename(srcPathName, targetPathName);
        }catch (Exception e) {
            logger.error("Can't rename {}, Exception : {}", srcPathName, e.getMessage());
        }finally {
            returnObject(ftpClient);
        }

        return Boolean.FALSE;
    }

    /**
     * 重命名
     *
     * @param dir    目录
     * @param src    原
     * @param target 目标
     * @return {@link Boolean}
     */
    public Boolean rename(String dir, String src, String target) {
        return rename(assemblyFileName(dir, src), assemblyFileName(dir, target));
    }

    /**
     * 下载文件
     *
     * @param remote 远程
     * @return {@link Boolean}
     */
    public byte[] downloadFile(String remote) throws FtpClientException {

        FTPClient ftpClient = null;
        try {
            ftpClient = pool.borrowObject();
            InputStream inputStream = ftpClient.retrieveFileStream(remote);
            if (ftpClient.completePendingCommand()) {
                return IoUtil.readBytes(inputStream);
            }else {
                throw new FtpClientException(String.format("Can't download %s", remote));
            }
        }catch (Exception e) {
            logger.error("Can't download {}, Exception : {}", remote, e.getMessage());
            throw new FtpClientException(e);
        }finally {
            returnObject(ftpClient);
        }
    }

    /**
     * 组装文件名
     *
     * @param directory      目录
     * @param fileName 文件名称
     * @return {@link String}
     */
    private String assemblyFileName(String directory, String fileName) {
        return directory + File.separator + fileName;
    }

    /**
     * 返还一个对象
     *
     * @param ftpClient ftp客户端
     */
    private void returnObject(FTPClient ftpClient) {
        if(ObjectUtil.isNotNull(ftpClient)) {
            pool.returnObject(ftpClient);
        }
    }

    /**
     * 将输入流存储到指定的ftp路径下
     *
     * @param ftpFileName 文件在ftp上的路径
     * @param in          输入流
     */
    private void storeFile(FTPClient ftpClient, String ftpFileName, InputStream in) throws IOException {
        try {
            if (!ftpClient.storeFile(ftpFileName, in)) {
                throw new IOException("Can't upload file '" + ftpFileName + "' to FTP server. Check FTP permissions and path.");
            }
        } finally {
            close(in);
        }
    }

    /**
     * 改变工作目录
     *
     * @param dir       ftp服务器上目录
     * @param ftpClient ftp客户端
     * @return boolean 改变成功  返回true
     */
    private boolean changeWorkingDirectory(FTPClient ftpClient, String dir) {
        if (!ftpClient.isConnected()) {
            return false;
        }
        try {
            return ftpClient.changeWorkingDirectory(dir);
        } catch (Exception e) {
            logger.error("changeWorkingDirectory {}", e.getMessage());
        }
        return false;
    }

    /**
     * 创建目录
     *
     * @param pathname  路径名
     * @param ftpClient ftp客户端
     * @return boolean 创建成功返回true
     * @throws IOException 异常
     */
    private boolean makeDirectory(FTPClient ftpClient, String pathname) throws Exception {
        return ftpClient.makeDirectory(pathname);
    }

    /**
     * 创建多个目录
     *
     * @param pathName  路径名
     * @param ftpClient ftp客户端
     * @throws IOException 异常
     */
    private void makeDirectories(FTPClient ftpClient, String pathName) throws Exception {
        pathName = pathName.replace("\\\\", "/");
        String[] pathnameArray = pathName.split("/");
        for (String each : pathnameArray) {
            if (StrUtil.isNotEmpty(each)) {
                ftpClient.makeDirectory(each);
                ftpClient.changeWorkingDirectory(each);
            }
        }
    }

    /**
     * 关闭流
     *
     * @param stream 流
     */
    private void close(Closeable stream) {
        IoUtil.close(stream);
    }

    /**
     * 关闭ftp连接
     */
    public void disconnect(FTPClient ftpClient) {
        if (null != ftpClient && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException ex) {
                // do nothing
            }
        }
    }




















}

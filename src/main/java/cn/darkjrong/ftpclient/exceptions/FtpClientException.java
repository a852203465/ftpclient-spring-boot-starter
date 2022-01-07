package cn.darkjrong.ftpclient.exceptions;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * ftp客户端异常
 *
 * @author Rong.Jia
 * @date 2022/01/06
 */
public class FtpClientException extends RuntimeException {

    public FtpClientException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public FtpClientException(String message) {
        super(message);
    }

    public FtpClientException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public FtpClientException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public FtpClientException(Throwable throwable, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
    }

    public boolean causeInstanceOf(Class<? extends Throwable> clazz) {
        Throwable cause = this.getCause();
        return null != clazz && clazz.isInstance(cause);
    }

}

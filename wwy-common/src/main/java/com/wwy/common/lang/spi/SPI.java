package com.wwy.common.lang.spi;

import java.lang.annotation.*;

/**
 * 扩展点接口的标识。
 * <p>
 * 扩展点声明配置文件，格式:<br />
 * 以Protocol示例，配置文件META-INF/services/com.xxx.Protocol内容：<br />
 * <pre><code>xxx=com.foo.XxxProtocol
 * yyy=com.foo.YyyProtocol
 * </code></pre>
 * <br/>
 *
 * @author wangxiaosan
 * @date 2017/10/18
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {
    String value() default "";
}

package cn.bincker.common.constant;

import java.util.regex.Pattern;

public interface RegExpConstant {
    String REGEXP_STR_URL = "^(https?|ftp)://" +                     // 协议
            "(?:(?:[a-zA-Z0-9$-_@.&+!*'(),]|%[0-9a-fA-F]{2})+" +  // 用户名
            "(?::(?:[a-zA-Z0-9$-_@.&+!*'(),]|%[0-9a-fA-F]{2})+)?@)?" +  // 密码(可选)
            "(?:" +
            "(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+" +  // 域名部分
            "[a-zA-Z]{2,}" +                          // 顶级域名
            "|" +
            "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +  // 或IPv4地址
            "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)" +
            "|" +
            "localhost" +                              // 或localhost
            ")" +
            "(?::[0-9]{1,5})?" +                      // 端口(可选)
            "(?:/(?:[a-zA-Z0-9$-_@.&+!*'(),]|%[0-9a-fA-F]{2})*)*" +  // 路径
            "(?:\\?(?:[a-zA-Z0-9$-_@.&+!*'(),]|%[0-9a-fA-F]{2})*)?" +  // 查询字符串(可选)
            "(?:#(?:[a-zA-Z0-9$-_@.&+!*'(),]|%[0-9a-fA-F]{2})*)?$";  // 片段标识符(可选)
    Pattern REGEXP_URL = Pattern.compile(REGEXP_STR_URL);
}

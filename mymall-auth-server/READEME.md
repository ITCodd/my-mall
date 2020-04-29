###spring security oauth2 授权访问两种方式：
1. 访问方式：
    - url：http://../a?access_token={access_token}
    - request请求header中加入key为Authorization，值为{tokenType}+空格+{access_token}
2. 说明：
    > spring security oauth2默认先去找第2种方式获取token，如果第2种方式获取的token为空，则取第一种方式。
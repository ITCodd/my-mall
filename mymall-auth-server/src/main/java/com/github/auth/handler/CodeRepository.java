package com.github.auth.handler;

public interface CodeRepository {

    /**
     * 生成验证码
     * @param deviceId 用户临时标识
     */
    public void genCode(String deviceId);

    /**
     * 保存验证码
     * @param deviceId
     * @param code
     */
    public void save(String deviceId, String code);

    /**
     * 获取验证码
     * @param deviceId
     */
    public String get(String deviceId);

    public boolean validate(String deviceId, String code);

}

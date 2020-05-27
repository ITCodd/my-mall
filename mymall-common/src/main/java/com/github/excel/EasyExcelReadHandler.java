package com.github.excel;

import java.util.List;

/**
 * @author: hjp
 * Date: 2020/5/25
 * Description:
 */
public interface EasyExcelReadHandler<E> {

    public  Class<E> getTargetClass();

    public  boolean isBacthProcess();

    public  boolean skipSpaceRow();

    public ExcelPreCheckResult preProcess(ExcelProcessContext<E> context);

    /**
     * @Accessors(chain = true)类上的这个注解和easyexcel冲突
     * 获取到对象时里面的数据都是空的，要把对象这个注解去掉
     * @param data
     * @param params
     */
    public void process(E data, Object params);


    public void process(List<E> datas, Object params);


}

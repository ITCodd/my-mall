package com.github.excel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * @author: hjp
 * Date: 2020/5/25
 * Description:
 */
@Slf4j
public abstract class BaseEasyExcelReadHandler<E> implements EasyExcelProcess,EasyExcelReadHandler<E> {

    private Class<E> target;


    public BaseEasyExcelReadHandler() {
        // 获取当前new的对象的泛型的父类类型
        ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
        // 获取第一个类型参数的真实类型
        this.target = (Class<E>) pt.getActualTypeArguments()[0];
    }

    @Override
    public Class<E> getTargetClass() {
        return target;
    }



    @Override
    public abstract boolean isBacthProcess();

    @Override
    public boolean skipSpaceRow() {
        return false;
    }

    @Override
    public abstract void process(E data, Object params);

    @Override
    public abstract void process(List<E> datas, Object params);


    @Override
    public ExcelResult handleExcel(MultipartFile file) throws IOException {
        return EasyExcelReadUtils.handleExcel(file,this);
    }

    @Override
    public ExcelResult handleExcel(MultipartFile file, Object params) throws IOException {
        return EasyExcelReadUtils.handleExcel(file,this,params);
    }

    @Override
    public ExcelResult handleExcel(InputStream in) {
        return EasyExcelReadUtils.handleExcel(in,this);
    }

    @Override
    public ExcelResult handleExcel(InputStream in, Object params) {
        return EasyExcelReadUtils.handleExcel(in,this,params);
    }
}

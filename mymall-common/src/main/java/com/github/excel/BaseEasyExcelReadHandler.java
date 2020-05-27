package com.github.excel;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return handleExcel(file.getInputStream(),null);
    }

    @Override
    public ExcelResult handleExcel(MultipartFile file, Object params) throws IOException {
        return handleExcel(file.getInputStream(),params);
    }

    @Override
    public ExcelResult handleExcel(InputStream in) {
        return handleExcel(in,null);
    }

    @Override
    public ExcelResult handleExcel(InputStream in, Object params) {
        EasyExcelReadListener<E> listener=new EasyExcelReadListener();
        listener.setHandler(this);
        listener.setParams(params);
        EasyExcel.read(in, this.getTargetClass(), listener).sheet().doRead();
        Map<Integer, ExcelErrorMsg> map=new LinkedHashMap<>();
        List<ExcelErrorMsg> errors = listener.getErrors();
        for (ExcelErrorMsg error : errors) {
            if(map.containsKey(error.getRowIndex())){
                ExcelErrorMsg excelErrorMsg = map.get(error.getRowIndex());
                excelErrorMsg.getErrors().addAll(error.getErrors());
            }else{
                map.put(error.getRowIndex(),error);
            }
        }
        List<ExcelErrorMsg> errorMsgs = map.values().stream().collect(Collectors.toList());
        ExcelResult result=new ExcelResult();
        if(this.isBacthProcess()){
            result.setSucNUm(listener.getResults().size()-errorMsgs.size());
        }else{
            result.setSucNUm(listener.getIncr().get());
        }
        result.setErrorCount(listener.getErrorCount().get());
        result.setErrors(errorMsgs);
        result.setResults(listener.getResults());
        return result;
    }
}

package com.github.excel;

import com.alibaba.excel.EasyExcel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author: hjp
 * Date: 2020/5/25
 * Description:
 */
public class EasyExcelReadUtils {

    public static ExcelResult handleExcel(MultipartFile file, EasyExcelReadHandler handler) throws IOException {
        return handleExcel(file,handler,null);
    }

    public static ExcelResult handleExcel(MultipartFile file, EasyExcelReadHandler handler, Object params) throws IOException {
        return handleExcel(file.getInputStream(),handler,params);
    }

    public static ExcelResult handleExcel(InputStream in, EasyExcelReadHandler handler) {
        return handleExcel(in, handler,null);
    }

    public static ExcelResult handleExcel(InputStream in, EasyExcelReadHandler handler,Object params) {
        EasyExcelReadListener listener=new EasyExcelReadListener();
        listener.setHandler(handler);
        listener.setParams(params);
        EasyExcel.read(in, handler.getTargetClass(), listener).sheet().doRead();
        ExcelResult result=new ExcelResult();
        result.setErrors(listener.getErrors());
        result.setResults(listener.getResults());
        return result;
    }

}

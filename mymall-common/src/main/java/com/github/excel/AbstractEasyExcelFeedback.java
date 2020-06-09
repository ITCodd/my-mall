package com.github.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONObject;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: hjp
 * Date: 2020/5/28
 * Description:
 */
public abstract class AbstractEasyExcelFeedback implements EasyExcelFeedback {


    @Override
    public void feedback(String feedbackId,String classpath, String downloadName, HttpServletResponse response) throws Exception {
        feedback(feedbackId, classpath, "feedback", downloadName, response);
    }

    @Override
    public void feedback(String feedbackId, ClassPathResource classPathResource, String downloadName, HttpServletResponse response) throws Exception {
        feedback(feedbackId, classPathResource, "feedback", downloadName,response);
    }

    @Override
    public void feedback(String feedbackId, String classpath, String feedbackField, String downloadName, HttpServletResponse response) throws Exception {
        ClassPathResource classPathResource = new ClassPathResource(classpath);
        feedback(feedbackId, classPathResource, feedbackField, downloadName,response);
    }

    @Override
    public void feedback(String feedbackId, ClassPathResource classPathResource, String feedbackField, String downloadName, HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fName = URLEncoder.encode(downloadName, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fName);
        InputStream inputStream =classPathResource.getInputStream();
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).withTemplate
                (inputStream).build();
        WriteSheet writeSheet = EasyExcel.writerSheet().build();
        ExcelResult result = getExcelResult(feedbackId);
        List<JSONObject> list=new ArrayList<>();
        for (ExcelErrorMsg errorMsg : result.getErrors()) {
            String feedback = errorMsg.getErrors().stream().map(ExcelValidateMsg::getMessage).collect(Collectors.joining(";"));
            JSONObject rowData = (JSONObject) errorMsg.getRowData();
            rowData.put(feedbackField,feedback);
            list.add(rowData);
        }
        // 填充集合 {.name}
        excelWriter.fill(list, writeSheet);
        excelWriter.finish();
    }


    @Override
    public void feedback(String feedbackId, Class<?> target, String feedbackField, String downloadName, HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fName = URLEncoder.encode(downloadName, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fName);
        ExcelResult result = getExcelResult(feedbackId);
        List list=new ArrayList<>();
        for (ExcelErrorMsg errorMsg : result.getErrors()) {
            String feedback = errorMsg.getErrors().stream().map(ExcelValidateMsg::getMessage).collect(Collectors.joining(";"));
            JSONObject rowData = (JSONObject) errorMsg.getRowData();
            rowData.put(feedbackField,feedback);
            list.add(rowData.toJavaObject(target));
        }
        EasyExcel.write(response.getOutputStream(), target).sheet("反馈").doWrite(list);
    }


}

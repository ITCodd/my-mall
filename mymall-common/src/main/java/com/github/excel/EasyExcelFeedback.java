package com.github.excel;

import org.springframework.core.io.ClassPathResource;

import javax.servlet.http.HttpServletResponse;

/**
 * @author: hjp
 * Date: 2020/5/28
 * Description:
 */
public interface EasyExcelFeedback {
    ExcelResult getExcelResult(String feedbackId);
    void saveFeedbackmsg(ExcelResult result);
    void feedback(String feedbackId, String classpath, String downloadName, HttpServletResponse response)  throws Exception;
    void feedback(String feedbackId, ClassPathResource classPathResource, String downloadName, HttpServletResponse response) throws Exception;
    void feedback(String feedbackId, String classpath, String feedbackField, String downloadName, HttpServletResponse response) throws Exception;
    void feedback(String feedbackId, ClassPathResource classPathResource, String feedbackField, String downloadName, HttpServletResponse response) throws Exception;
    void feedback(String feedbackId, Class<?> target, String feedbackField, String downloadName, HttpServletResponse response) throws Exception;
}

package com.github.excel;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author: hjp
 * Date: 2020/5/25
 * Description:
 */
public interface EasyExcelProcess {
    ExcelResult handleExcel(MultipartFile file, boolean onlyCheck) throws IOException ;
    ExcelResult handleExcel(MultipartFile file, Object params, boolean onlyCheck) throws IOException ;
    ExcelResult handleExcel(InputStream in, boolean onlyCheck) ;
    ExcelResult handleExcel(InputStream in, Object params, boolean onlyCheck) ;
    ExcelResult handleExcel(MultipartFile file) throws IOException ;
    ExcelResult handleExcel(MultipartFile file, Object params) throws IOException ;
    ExcelResult handleExcel(InputStream in) ;
    ExcelResult handleExcel(InputStream in, Object params) ;

    ExcelResult checkExcelOnly(MultipartFile file) throws IOException ;
    ExcelResult checkExcelOnly(MultipartFile file, Object params) throws IOException ;
    ExcelResult checkExcelOnly(InputStream in) ;
    ExcelResult checkExcelOnly(InputStream in, Object params);

    ExcelResult handleCheckExcelResult(ExcelResult result) ;
}

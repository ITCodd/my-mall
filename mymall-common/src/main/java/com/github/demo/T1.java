package com.github.demo;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: hjp
 * Date: 2020/5/28
 * Description:
 */
public class T1 {
    public static void main(String[] args) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("templates/公章柜模板-反馈.xlsx");
        InputStream inputStream =classPathResource.getInputStream();
        File file=new File("F:\\1.xlsx");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ExcelWriter excelWriter = EasyExcel.write(fileOutputStream).withTemplate
                (inputStream).build();
        WriteSheet writeSheet = EasyExcel.writerSheet().build();
        List<DemoExcelDto> list=new ArrayList<>();
        DemoExcelDto dto=new DemoExcelDto();
        dto.setAdmins("ww");
        dto.setName("zzzz");
        dto.setFeedback("错误警告");
        list.add(dto);
        DemoExcelDto dto1=new DemoExcelDto();
        dto1.setAdmins("qqq");
        dto1.setName("hh");
        dto1.setFeedback("错误警告");
        list.add(dto1);
        // 填充集合 {.name}
        excelWriter.fill(list, writeSheet);
        excelWriter.finish();
    }
}

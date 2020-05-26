package com.github.excel;

import lombok.Data;

import java.util.List;

@Data
public class ExcelValidateException extends RuntimeException {
    List<ExcelValidateMsg> list;

    public ExcelValidateException(List<ExcelValidateMsg> list) {
        this.list=list;
    }
}

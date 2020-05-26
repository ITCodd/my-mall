package com.github.excel;

import lombok.Data;

import java.util.List;

@Data
public class ExcelErrorMsg {
    private Integer rowIndex;
    private Object rowData;
    private List<ExcelValidateMsg> errors;

}

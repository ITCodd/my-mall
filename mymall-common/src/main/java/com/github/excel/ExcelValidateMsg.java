package com.github.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExcelValidateMsg {

    private Integer rowIndex;

    private Integer columnIndex;

    private Object cellData;

    private String message;

    public ExcelValidateMsg(Integer rowIndex, String message) {
        this.rowIndex = rowIndex;
        this.message = message;
    }
}

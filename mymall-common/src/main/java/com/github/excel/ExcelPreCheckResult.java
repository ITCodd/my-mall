package com.github.excel;

import lombok.Data;

import java.util.List;

/**
 * @author: hjp
 * Date: 2020/5/26
 * Description:
 */
@Data
public class ExcelPreCheckResult {
    private boolean pass;
    private List<ExcelPreCheckMsg> errors;
}

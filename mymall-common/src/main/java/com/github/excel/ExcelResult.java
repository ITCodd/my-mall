package com.github.excel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: hjp
 * Date: 2020/5/25
 * Description:
 */
@Data
public class ExcelResult {

    private List<?> results = new ArrayList<>();

    private List<ExcelErrorMsg> errors=new ArrayList<>();
}

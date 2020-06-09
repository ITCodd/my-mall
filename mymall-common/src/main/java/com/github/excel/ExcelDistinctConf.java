package com.github.excel;

import lombok.Data;

/**
 * @author: hjp
 * Date: 2020/6/5
 * Description:
 */
@Data
public class ExcelDistinctConf {
    private String[] distinctFields;
    private EXcelDistinctStrategy distinctStrategy;
}

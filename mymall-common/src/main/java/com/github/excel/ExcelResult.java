package com.github.excel;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @JSONField(serialize = false)
    private List<?> results = new ArrayList<>();
    private Integer sucNUm;
    private Integer errorCount;
    private Integer total;
    private List<ExcelErrorMsg> errors=new ArrayList<>();
}

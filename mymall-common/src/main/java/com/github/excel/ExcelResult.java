package com.github.excel;

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
    private List<?> results = new ArrayList<>();
    private Integer sucNum;
    private Integer errorCount;
    private Integer total;
    private String feedbackId;
    @JsonIgnore
    private Object params;
    private List<ExcelErrorMsg> errors=new ArrayList<>();
}

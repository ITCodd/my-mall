package com.github.excel;

import com.alibaba.excel.context.AnalysisContext;
import lombok.Data;

import java.util.List;

/**
 * @author: hjp
 * Date: 2020/5/26
 * Description:
 */
@Data
public class ExcelProcessContext<E>{
    private ExcelPreCheckItem<E> item;
    private List<ExcelPreCheckItem<E>> items;
    private Object params;
    private AnalysisContext context;
}

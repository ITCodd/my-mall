package com.github.excel;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: hjp
 * Date: 2020/5/26
 * Description:
 */
@Data
@NoArgsConstructor
public class ExcelPreCheckItem<E> {
    /**
     * 当前行号
     */
    private Integer rowIndex;
    /**
     * 校验的数据
     */
    private E data;
    /**
     * 校验的字段名称
     */
    private String fieldName;
    /**
     * 校验失败返回的消息
     */
    private String message;

    public ExcelPreCheckItem(Integer rowIndex, E data) {
        this.rowIndex = rowIndex;
        this.data = data;
    }

    public ExcelPreCheckItem(Integer rowIndex, E data, String fieldName, String message) {
        this.rowIndex = rowIndex;
        this.data = data;
        this.fieldName = fieldName;
        this.message = message;
    }
}

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
public class ExcelPreCheckMsg<E> {
    /**
     * 当前行号
     */
    private ExcelRowItem<E> item;

    /**
     * 校验的字段名称
     */
    private String fieldName;
    /**
     * 校验失败返回的消息
     */
    private String message;

    public ExcelPreCheckMsg(ExcelRowItem<E> item, String fieldName, String message) {
        this.item = item;
        this.fieldName = fieldName;
        this.message = message;
    }

    public ExcelPreCheckMsg(ExcelRowItem<E> item, String message) {
        this.item = item;
        this.message = message;
    }
}

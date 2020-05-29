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
public class ExcelRowItem<E> {
    /**
     * 当前行号
     */
    private Integer rowIndex;
    /**
     * 校验的数据
     */
    private E data;


    public ExcelRowItem(Integer rowIndex, E data) {
        this.rowIndex = rowIndex;
        this.data = data;
    }

}

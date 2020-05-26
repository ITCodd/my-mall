package com.github.excel;

import lombok.Data;

/**
 * @author: hjp
 * Date: 2020/5/26
 * Description:
 */
@Data
public class EasyExcelCommonException extends RuntimeException {
    private Object rowData;
    private Integer colIndex;
    private String filedName;
    private String msg;

    public EasyExcelCommonException(Object rowData, Integer colIndex, String msg){
        super(msg);
        this.rowData=rowData;
        this.colIndex=colIndex;
        this.msg=msg;
    }

    public EasyExcelCommonException(Object rowData, String filedName, String msg){
        super(msg);
        this.rowData=rowData;
        this.filedName=filedName;
        this.msg=msg;
    }
}

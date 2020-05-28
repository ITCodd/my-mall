package com.github.demo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author hjp
 * @since 2020-05-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DemoExcelDto implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空")
    @ExcelProperty(index =0)
    private String name;



    @NotBlank(message = "分配的管理员不存在")
    @ExcelProperty(index =2)
    private String admins;

    private String feedback;


}

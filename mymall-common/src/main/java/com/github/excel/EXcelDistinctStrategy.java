package com.github.excel;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;


public enum EXcelDistinctStrategy {

    //0只要一个列重复即重复，1所有列重复即重复
    ANYONEFIELD(0,"只要一个列重复即重复"),ALLFIELD(1,"所有列重复即重复");

    /**
     * 插入数据库的值
     */
    @EnumValue
    private int value;

    /**
     * 显示的值
     */
    @JsonValue
    private String name;


    EXcelDistinctStrategy(int value, String name){
        this.value=value;
        this.name=name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

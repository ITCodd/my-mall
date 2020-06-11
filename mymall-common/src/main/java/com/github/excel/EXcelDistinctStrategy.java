package com.github.excel;

public enum EXcelDistinctStrategy {

    //0只要一个列重复即重复，1所有列重复即重复
    ANYONEFIELD(0,"只要一个列重复即重复"),ALLFIELD(1,"所有列重复即重复");


    private int value;


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

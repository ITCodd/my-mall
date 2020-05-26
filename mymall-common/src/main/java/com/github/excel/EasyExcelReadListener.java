package com.github.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.CellData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

//@Component
//注入其他类会多例，但是同个类的不同方法调用，或同一个方法调用多次还是同一个实例，
// 如不需要处理返回数据可交由spring管理
//@Scope("prototype")
@Slf4j
@NoArgsConstructor
public class EasyExcelReadListener<E> extends AnalysisEventListener<E> {

    private  Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Setter
    @Getter
    private EasyExcelReadHandler<E> handler;

    @Setter
    private Object params;

    @Setter
    @Getter
    private List<E> results = new ArrayList<>();

    @Getter
    private List<ExcelErrorMsg> errors=new ArrayList<>();

    @Override
    public void invoke(E data, AnalysisContext context) {
        if(handler.skipSpaceRow()){
            boolean spaceRow = isSpaceRow(data);
            if(spaceRow){
                return;
            }
        }
        //对数据进行校验
        validate(data, context);
        if(!handler.isBacthProcess()){
            handler.process(data,params);
        }else{
            results.add(data);
        }
    }

    private boolean isSpaceRow(E data) {
        Field[] fields = data.getClass().getDeclaredFields();
        for (Field field : fields) {
            ExcelProperty anno = field.getAnnotation(ExcelProperty.class);
            if(anno!=null){
                field.setAccessible(true);
                try {
                    Object o = field.get(data);
                    if(o!=null){
                        return false;
                    }
                } catch (Exception e) {
                    log.info("反射获取数据失败",e);
                }
            }

        }
        return true;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(handler.isBacthProcess()){
            handler.process(results,params);
        }
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        ExcelErrorMsg errorMsg=new ExcelErrorMsg();
        errorMsg.setRowIndex(context.readRowHolder().getRowIndex());
        if(exception instanceof ExcelValidateException){
            ExcelValidateException ev= (ExcelValidateException) exception;
            Object result = context.readRowHolder().getCurrentRowAnalysisResult();
            errorMsg.setRowData(result);
            List<ExcelValidateMsg> list = ev.getList();
            errorMsg.setErrors(list);
        }else if(exception instanceof ExcelDataConvertException){
            ExcelDataConvertException ev= (ExcelDataConvertException) exception;
            ExcelValidateMsg msg=new ExcelValidateMsg(ev.getRowIndex(),ev.getColumnIndex(),ev.getCellData().getStringValue(),ev.getMessage());
            List<ExcelValidateMsg> list = new ArrayList<>();
            list.add(msg);
            errorMsg.setErrors(list);
            //此格式为LinkedHashMap格式，数据中包含很多不需要的内容，需进行转换
            LinkedHashMap<Integer, CellData> result = (LinkedHashMap) context.readRowHolder().getCurrentRowAnalysisResult();
            LinkedHashMap<String,Object> map=new LinkedHashMap<>();
            Field[] fields = handler.getTargetClass().getDeclaredFields();
            for (Field field : fields) {
                ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
                if(annotation!=null){
                    CellData cellData = result.get(annotation.index());
                    if(cellData!=null){
                        if(cellData.getType()== CellDataTypeEnum.BOOLEAN){
                            map.put(field.getName(),cellData.getBooleanValue());
                        }else if(cellData.getType()== CellDataTypeEnum.STRING){
                            map.put(field.getName(),cellData.getStringValue());
                        }else if(cellData.getType()== CellDataTypeEnum.NUMBER){
                            map.put(field.getName(),cellData.getNumberValue());
                        }else{
                            map.put(field.getName(),cellData.getData());
                        }
                    }else{
                        map.put(field.getName(),null);
                    }

                }
            }
            errorMsg.setRowData(map);
        }else if(exception instanceof EasyExcelCommonException){
            EasyExcelCommonException ev= (EasyExcelCommonException) exception;
            errorMsg.setRowData(ev.getRowData());
            Object cellData=getCellData(ev);
            ExcelValidateMsg msg=new ExcelValidateMsg(errorMsg.getRowIndex(),ev.getColIndex(),cellData,ev.getMessage());
            List<ExcelValidateMsg> list = new ArrayList<>();
            list.add(msg);
            errorMsg.setErrors(list);
        }else{
            errorMsg.setRowData(exception.getMessage());
        }
        errors.add(errorMsg);
    }

    private Object getCellData(EasyExcelCommonException ev) throws NoSuchFieldException {
        if(StringUtils.isNotBlank(ev.getFiledName())){
            Field field = ev.getRowData().getClass().getDeclaredField(ev.getFiledName());
            ExcelProperty anno = field.getAnnotation(ExcelProperty.class);
            if(anno!=null){
                field.setAccessible(true);
                try {
                    ev.setColIndex(anno.index());
                    return field.get(ev.getRowData());
                } catch (Exception e) {
                    log.info("反射获取数据失败",e);
                    return null;
                }
            }
        }
        Field[] fields = ev.getRowData().getClass().getDeclaredFields();
        for (Field field : fields) {
            ExcelProperty anno = field.getAnnotation(ExcelProperty.class);
            if(anno!=null&&anno.index()==ev.getColIndex()){
                field.setAccessible(true);
                try {
                    return field.get(ev.getRowData());
                } catch (Exception e) {
                    log.info("反射获取数据失败",e);
                }
            }

        }
        return null;
    }


    private void validate(E data, AnalysisContext context) {
        Set<ConstraintViolation<E>> violationSet = validator.validate(data);
        if(!violationSet.isEmpty()){
            List<ExcelValidateMsg> list=new ArrayList<>();
            for (ConstraintViolation<E> violation : violationSet) {
                Integer rowIndex = context.readRowHolder().getRowIndex();
                String field=null;
                Object val=null;
                for (Path.Node node : violation.getPropertyPath()) {
                    if(StringUtils.isNotBlank(node.getName())){
                        field=node.getName();
                        val=violation.getInvalidValue();
                    }
                }
                Integer colIndex=null;
                try {
                    Field f = data.getClass().getDeclaredField(field);
                    ExcelProperty annotation = f.getAnnotation(ExcelProperty.class);
                    if(annotation!=null){
                        colIndex=annotation.index();
                    }
                } catch (NoSuchFieldException e) {
                    log.info("获取字段失败",e);
                }
                list.add(new ExcelValidateMsg(rowIndex,colIndex,val,violation.getMessage()));
            }
            throw new ExcelValidateException(list);
        }
    }


}
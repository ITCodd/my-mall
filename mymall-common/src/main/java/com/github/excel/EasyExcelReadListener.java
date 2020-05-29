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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
    private List<ExcelRowItem<E>> rowItems = new ArrayList<>();

    @Getter
    private List<ExcelErrorMsg> errors=new ArrayList<>();
    @Getter
    private volatile AtomicInteger incr=new AtomicInteger();
    @Getter
    private volatile AtomicInteger errorCount=new AtomicInteger();
    @Getter
    private volatile AtomicInteger total=new AtomicInteger();

    private Set<Integer> rowSkipRow=new HashSet<>();

    @Override
    public void invoke(E data, AnalysisContext context) {
        //判断是否开启空行过滤
        if(handler.skipSpaceRow()){
            boolean spaceRow = isSpaceRow(data);
            if(spaceRow){
                return;
            }
        }
        total.incrementAndGet();
        //对数据进行校验
        validate(data, context);
        if(!handler.isBacthProcess()){
            ExcelProcessContext<E> excelContext=new ExcelProcessContext<E>();
            excelContext.setContext(context);
            excelContext.setParams(params);
            excelContext.setItem(new ExcelRowItem<E>(context.readRowHolder().getRowIndex(),data));
            //预校验检查
            ExcelPreCheckResult checkResult = handler.preProcess(excelContext);
            if(checkResult==null||checkResult.isPass()){
                handler.process(excelContext);
                //统计成功导入的个数
                incr.incrementAndGet();
            }else{
                handlePreCheckResult(checkResult);
            }

        }else{
            rowItems.add(new ExcelRowItem<E>(context.readRowHolder().getRowIndex(),data));
        }
    }



    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        total.addAndGet(rowSkipRow.size());
        if(handler.isBacthProcess()){
            ExcelProcessContext<E> excelContext=new ExcelProcessContext();
            excelContext.setContext(analysisContext);
            excelContext.setParams(params);
            excelContext.setItems(rowItems);
            //预校验检查
            ExcelPreCheckResult<E> checkResult = handler.preProcess(excelContext);
            if(checkResult==null||checkResult.isPass()){
                handler.process(excelContext);
                //统计成功导入的个数
                incr.incrementAndGet();
            }else{
                handlePreCheckResult(checkResult);
            }
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
            //统计异常的次数
            errorCount.addAndGet(list.size());
        }else if(exception instanceof ExcelDataConvertException){
            ExcelDataConvertException ev= (ExcelDataConvertException) exception;
            Field errField = ev.getExcelContentProperty().getField();
            ExcelDataConvertMsg anno = errField.getAnnotation(ExcelDataConvertMsg.class);
            String message=ev.getMessage();
            if(anno!=null){
                message=anno.message();
            }
            rowSkipRow.add(ev.getRowIndex());
            ExcelValidateMsg msg=new ExcelValidateMsg(ev.getRowIndex(),ev.getColumnIndex(),ev.getCellData().getStringValue(),message);
            List<ExcelValidateMsg> list = new ArrayList<>();
            list.add(msg);
            errorMsg.setErrors(list);
            //此格式为LinkedHashMap格式，数据中包含很多不需要的内容，需进行转换
            LinkedHashMap<Integer, CellData> result = (LinkedHashMap) context.readRowHolder().getCurrentRowAnalysisResult();
            LinkedHashMap<String,Object> map=new LinkedHashMap<>();
            Field[] fields = handler.getTargetClass().getDeclaredFields();
            int annIndex=0;
            for (Field field : fields) {
                ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
                if(annotation!=null){
                    CellData cellData = result.get(annIndex++);
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
            //统计异常的次数
            errorCount.addAndGet(list.size());
        }else if(exception instanceof EasyExcelCommonException){
            EasyExcelCommonException ev= (EasyExcelCommonException) exception;
            errorMsg.setRowData(ev.getRowData());
            Object cellData=getCellData(ev);
            ExcelValidateMsg msg=new ExcelValidateMsg(errorMsg.getRowIndex(),ev.getColIndex(),cellData,ev.getMessage());
            List<ExcelValidateMsg> list = new ArrayList<>();
            list.add(msg);
            errorMsg.setErrors(list);
            //统计异常的次数
            errorCount.addAndGet(list.size());
        }else{
            ExcelValidateMsg msg=new ExcelValidateMsg(errorMsg.getRowIndex(),exception.getMessage());
            List<ExcelValidateMsg> list = new ArrayList<>();
            list.add(msg);
            errorMsg.setErrors(list);
            Object result = context.readRowHolder().getCurrentRowAnalysisResult();
            errorMsg.setRowData(result);
            errorCount.incrementAndGet();
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
        if(ev.getColIndex()==-1){
            return null;
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

    private void handlePreCheckResult(ExcelPreCheckResult<E> checkResult) {
        if(CollectionUtils.isNotEmpty(checkResult.getErrors())){

            Map<Integer, List<ExcelPreCheckMsg<E>>> listMap = checkResult.getErrors().stream()
                    .collect(Collectors.groupingBy(item -> item.getItem().getRowIndex()));
            listMap.keySet().forEach(rowIndex->{
                ExcelErrorMsg errorMsg=new ExcelErrorMsg();
                List<ExcelPreCheckMsg<E>> checkMsgs = listMap.get(rowIndex);
                errorMsg.setRowData(checkMsgs.get(0).getItem().getData());
                errorMsg.setRowIndex(rowIndex);
                List<ExcelValidateMsg> list=new ArrayList<>();
                errorMsg.setErrors(list);
                for (ExcelPreCheckMsg<E> checkMsg : checkMsgs) {
                    if(StringUtils.isNotBlank(checkMsg.getFieldName())){
                        try {
                            E data = checkMsg.getItem().getData();
                            Field field = data.getClass().getDeclaredField(checkMsg.getFieldName());
                            ExcelProperty anno = field.getAnnotation(ExcelProperty.class);
                            if(anno!=null){
                                field.setAccessible(true);
                                ExcelValidateMsg excelValidateMsg = new ExcelValidateMsg(rowIndex, anno.index(), field.get(data), checkMsg.getMessage());
                                list.add(excelValidateMsg);
                            }
                        } catch (Exception e) {
                            log.info("反射获取数据失败",e);
                        }

                    }else{
                        ExcelValidateMsg excelValidateMsg = new ExcelValidateMsg(rowIndex, checkMsg.getMessage());
                        list.add(excelValidateMsg);
                    }

                }
                errors.add(errorMsg);
            });

            //统计异常的次数
            errorCount.addAndGet(checkResult.getErrors().size());
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

}
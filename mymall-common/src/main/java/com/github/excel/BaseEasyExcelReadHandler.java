package com.github.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: hjp
 * Date: 2020/5/25
 * Description:
 */
@Slf4j
public abstract class BaseEasyExcelReadHandler<E> implements InitializingBean, EasyExcelProcess,EasyExcelReadHandler<E> {

    private Class<E> target;

    @Setter
    @Getter
    protected ExcelDistinctConf distinctConf;


    public BaseEasyExcelReadHandler() {
        // 获取当前new的对象的泛型的父类类型
        ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
        // 获取第一个类型参数的真实类型
        this.target = (Class<E>) pt.getActualTypeArguments()[0];
    }



    @Override
    public Class<E> getTargetClass() {
        return target;
    }

    @Override
    public abstract boolean isBacthProcess();

    @Override
    public boolean skipSpaceRow() {
        return false;
    }

    @Override
    public ExcelResult handleExcel(MultipartFile file) throws IOException {
        return handleExcel(file,null,false);
    }

    @Override
    public ExcelResult handleExcel(MultipartFile file, Object params) throws IOException {
        return handleExcel(file,params,false);
    }

    @Override
    public ExcelResult handleExcel(InputStream in) {
        return handleExcel(in,null,false);
    }

    @Override
    public ExcelResult handleExcel(InputStream in, Object params) {
        return handleExcel(in,params,false);
    }

    @Override
    public ExcelResult handleExcel(MultipartFile file, boolean onlyCheck) throws IOException {
        return handleExcel(file,null,onlyCheck);
    }

    @Override
    public ExcelResult handleExcel(MultipartFile file, Object params, boolean onlyCheck) throws IOException {
        return handleExcel(file.getInputStream(),params,onlyCheck);
    }

    @Override
    public ExcelResult handleExcel(InputStream in, boolean onlyCheck) {
        return handleExcel(in,null,onlyCheck);
    }

    @Override
    public ExcelResult handleExcel(InputStream in, Object params, boolean onlyCheck) {
        return handle(in, params, onlyCheck);
    }

    @Override
    public ExcelResult checkExcelOnly(MultipartFile file) throws IOException {
        return checkExcelOnly(file,null);
    }

    @Override
    public ExcelResult checkExcelOnly(MultipartFile file, Object params) throws IOException {
        return checkExcelOnly(file.getInputStream(),params);
    }

    @Override
    public ExcelResult checkExcelOnly(InputStream in) {
        return checkExcelOnly(in,null);
    }

    @Override
    public ExcelResult checkExcelOnly(InputStream in, Object params) {
        return handle(in, params, true);
    }

    private ExcelResult handle(InputStream in, Object params, boolean onlyCheck) {
        EasyExcelReadListener<E> listener = new EasyExcelReadListener();
        listener.setHandler(this);
        listener.setParams(params);
        listener.setOnlyCheck(onlyCheck);
        listener.setDistinctConf(getDistinctConf());
        EasyExcel.read(in, this.getTargetClass(), listener).sheet().doRead();
        Map<Integer, ExcelErrorMsg> map = new LinkedHashMap<>();
        List<ExcelErrorMsg> errors = listener.getErrors();
        for (ExcelErrorMsg error : errors) {
            if (map.containsKey(error.getRowIndex())) {
                ExcelErrorMsg excelErrorMsg = map.get(error.getRowIndex());
                excelErrorMsg.getErrors().addAll(error.getErrors());
            } else {
                map.put(error.getRowIndex(), error);
            }
        }
        List<ExcelErrorMsg> errorMsgs = map.values().stream().collect(Collectors.toList());
        ExcelResult result = new ExcelResult();
        result.setParams(params);
        result.setTotal(listener.getTotal().get());
        result.setErrorCount(listener.getErrorCount().get());
        if (this.isBacthProcess()) {
            result.setSucNum(result.getTotal() - errorMsgs.size());
        } else {
            result.setSucNum(listener.getIncr().get());
        }
        result.setResults(listener.getSucResults());
        result.setErrors(errorMsgs);
        return result;
    }

    @Override
    public ExcelResult handleCheckExcelResult(ExcelResult result) {
        List<ExcelPreCheckItem<E>> results = result.getResults().stream().map(item -> {
            JSONObject data = (JSONObject) item;
            ExcelPreCheckItem<E> target = data.toJavaObject(ExcelPreCheckItem.class);
            JSONObject rowData = data.getJSONObject("data");
            E e = rowData.toJavaObject(this.getTargetClass());
            target.setData(e);
            return target;
        }).collect(Collectors.toList());
        ExcelProcessContext<E> excelContext=new ExcelProcessContext<E>();
        excelContext.setContext(null);
        excelContext.setParams(result.getParams());
        if (this.isBacthProcess()) {
            Set<Integer> filter = result.getErrors().stream().map(ExcelErrorMsg::getRowIndex)
                                            .collect(Collectors.toSet());
            List<ExcelPreCheckItem<E>> items = results.stream().filter(item -> !filter.contains(item.getRowIndex()))
                                                    .collect(Collectors.toList());
            excelContext.setItems(items);
            //不进行预处理，直接进行保存处理
            process(excelContext);
        }else{
            List<ExcelErrorMsg> errorMsgs=new ArrayList<>();
            for (ExcelPreCheckItem<E> item : results) {
                excelContext.setItem(item);
                //进行预处理，进行保存处理
                ExcelPreCheckResult checkResult = preProcess(excelContext);
                if(checkResult==null||checkResult.isPass()){
                    process(excelContext);
                }else{
                    ExcelErrorMsg excelErrorMsg = handlePreCheckResult(checkResult);
                    errorMsgs.add(excelErrorMsg);
                }
            }
            if(CollectionUtils.isNotEmpty(errorMsgs)){
                Map<Integer, ExcelErrorMsg> map=new HashMap<>();
                for (ExcelErrorMsg error : result.getErrors()) {
                    map.put(error.getRowIndex(),error);
                }
                List<ExcelErrorMsg> newError=new ArrayList<>();
                for (ExcelErrorMsg newErrorMsg : errorMsgs) {
                    //原来已校验的消息
                    ExcelErrorMsg oldExcelErrorMsg = map.get(newErrorMsg.getRowIndex());
                    if(oldExcelErrorMsg==null){
                        newError.add(newErrorMsg);
                        int sucNum = result.getSucNum() - 1;
                        result.setSucNum(sucNum);
                        continue;
                    }
                    //获取旧的消息列表
                    Set<String> filterExistError = oldExcelErrorMsg.getErrors().stream()
                                                .map(ExcelValidateMsg::getMessage)
                                                .collect(Collectors.toSet());
                    //判断新的错误消息是否在原来的错误消息列表，如果不存在，添加到原来的错误消息列表
                    newErrorMsg.getErrors().stream()
                                .filter(item->!filterExistError.contains(item.getMessage()))
                                .forEach(item->{
                                    oldExcelErrorMsg.getErrors().add(item);
                                    int count=result.getErrorCount()+1;
                                    result.setErrorCount(count);
                                });
                }
                result.getErrors().addAll(newError);
            }

        }
        return result;
    }

    private ExcelErrorMsg handlePreCheckResult(ExcelPreCheckResult<E> checkResult) {
        if(CollectionUtils.isNotEmpty(checkResult.getErrors())){
            ExcelErrorMsg errorMsg=new ExcelErrorMsg();
            List<ExcelPreCheckMsg<E>> checkMsgs = checkResult.getErrors();
            errorMsg.setRowData(checkMsgs.get(0).getItem().getData());
            errorMsg.setRowIndex(checkMsgs.get(0).getItem().getRowIndex());
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
                            ExcelValidateMsg excelValidateMsg = new ExcelValidateMsg(errorMsg.getRowIndex(), anno.index(), field.get(data), checkMsg.getMessage());
                            list.add(excelValidateMsg);
                        }
                    } catch (Exception e) {
                        log.info("反射获取数据失败",e);
                    }

                }else{
                    ExcelValidateMsg excelValidateMsg = new ExcelValidateMsg(errorMsg.getRowIndex(), checkMsg.getMessage());
                    list.add(excelValidateMsg);
                }

            }
            return errorMsg;
        }
        return null;
    }

}

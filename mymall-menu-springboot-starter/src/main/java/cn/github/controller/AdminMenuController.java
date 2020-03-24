package cn.github.controller;

import cn.github.annotation.Menu;
import cn.github.pojo.MenuInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.*;

@RestController
@RequestMapping("/admin")
@Menu(menuCode = "menu:parent",menuName = "菜单")
public class AdminMenuController {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Menu(menuCode = "menu:dd",menuName = "菜单dd")
    public String t0() {
        return "OK";
    }

    @GetMapping("/qq")
    @Menu(menuCode = "menu:qq",menuName = "菜单列表")
    public String t1() {
        return "OK";
    }

    @GetMapping("/menu")
    @Menu(menuCode = "menu:list",menuName = "菜单列表",parentCode = "menu:parent")
    public List<MenuInfo> t2() {
        //获取Spring MVC管理的所有URL
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.handlerMapping.getHandlerMethods();
        //类上的menu注解可能存在多级关系
        Map<String,MenuInfo> mapController=new LinkedHashMap<>();
        // 方法上是有访问url的，是具体的访问项，不具有父级功能,有可能有父级也可能没有
        Map<String,List<MenuInfo>> mapMethod=new LinkedHashMap<>();
        //遍历Spring MVC管理的Method
        Set<Class<?>> filter=new HashSet<>();
        for (RequestMappingInfo rmi : handlerMethods.keySet()) {
            HandlerMethod method = handlerMethods.get(rmi);
            PatternsRequestCondition pc = rmi.getPatternsCondition();
            Set<String> pSet = pc.getPatterns();//获取url
            Method m = method.getMethod();//获取对应的方法
            Class<Menu> anno=Menu.class;
            //遍历类上的注解
            Menu menuController = method.getBeanType().getAnnotation(anno);
            if(menuController!=null) {//判断是否标注的自定义注解
                if(!filter.contains(method.getBeanType())){
                    filter.add(method.getBeanType());
                    MenuInfo menuInfo = MenuInfo.builder().menuCode(menuController.menuCode())
                            .menuDesc(menuController.menuDesc())
                            .menuName(menuController.menuName())
                            .parentCode(menuController.parentCode()).build();
                    mapController.put(menuController.menuCode(),menuInfo);
                    Method[] allDeclaredMethods = ReflectionUtils.getDeclaredMethods(method.getBeanType());
                    for (Method declaredMethod : allDeclaredMethods) {
                        RequestMapping requestMapping = AnnotationUtils.findAnnotation(declaredMethod, RequestMapping.class);
                        if(requestMapping==null){
                            Menu menu = AnnotationUtils.findAnnotation(declaredMethod, anno);
                            if(menu!=null){
                                MenuInfo menuInfo1 = MenuInfo.builder().menuCode(menu.menuCode())
                                        .menuDesc(menu.menuDesc())
                                        .menuName(menu.menuName())
                                        .parentCode(menu.parentCode()).build();
                                mapController.put(menu.menuCode(),menuInfo1);
                            }
                        }

                    }
                }

            }
            //遍历方法上的注解
            Menu menu = m.getAnnotation(anno);
            if(menu!=null) {//判断是否标注的自定义注解
                MenuInfo menuInfo = MenuInfo.builder().menuCode(menu.menuCode())
                        .menuDesc(menu.menuDesc())
                        .menuName(menu.menuName())
                        .menuUrl(pSet.iterator().next())
                        .parentCode(menu.parentCode()).build();
                if(StringUtils.isBlank(menuInfo.getParentCode())){
                    if(menuController!=null) {
                        menuInfo.setParentCode(menuController.menuCode());
                    }
                }
                if(StringUtils.isBlank(menuInfo.getParentCode())){
                    if(mapMethod.containsKey("-1")){
                        List<MenuInfo> menuInfos = mapMethod.get("-1");
                        menuInfos.add(menuInfo);
                    }else{
                        List<MenuInfo> menuInfos =new ArrayList<>();
                        menuInfos.add(menuInfo);
                        mapMethod.put("-1",menuInfos);
                    }
                }else{
                    if(mapMethod.containsKey(menuInfo.getParentCode())){
                        List<MenuInfo> menuInfos = mapMethod.get(menuInfo.getParentCode());
                        menuInfos.add(menuInfo);
                    }else{
                        List<MenuInfo> menuInfos =new ArrayList<>();
                        menuInfos.add(menuInfo);
                        mapMethod.put(menuInfo.getParentCode(),menuInfos);
                    }
                }

            }
        }//for 结束
        List<MenuInfo> list=new ArrayList<>();
        //查找所有的父类，并设置类的子级
        for (Map.Entry<String, MenuInfo> entry : mapController.entrySet()) {
            MenuInfo menuInfo = entry.getValue();
            if(StringUtils.isBlank(menuInfo.getParentCode())){
                list.add(menuInfo);
            }else{
                menuInfo.setParent(mapController.get(menuInfo.getParentCode()));
            }
            if(mapMethod.containsKey(menuInfo.getMenuCode())){
                menuInfo.setChildren(mapMethod.get(entry.getKey()));
            }
        }
        //获取没法父级的访问菜单
        List<MenuInfo> menus=mapMethod.get("-1");
        if(!CollectionUtils.isEmpty(menus)){
            list.addAll(menus);
        }
        return  list;
    }
}

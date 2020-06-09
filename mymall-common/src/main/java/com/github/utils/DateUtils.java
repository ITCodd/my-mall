package com.github.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * jdk8 获取当天，本周，本月，本季度，本年起始时间工具类
 *
 * */
public class DateUtils {

    public static final int NEXT = 1;

    /**
     * 获取某天的开始日期
     *
     * @param offset 0今天，1明天，-1昨天，依次类推
     * @return
     */
    public static LocalDate dayOffset(int offset) {
        return LocalDate.now().plusDays(offset);
    }

    /**
     * 获取某周的开始日期
     *
     * @param offset 0本周，1下周，-1上周，依次类推
     * @return
     */
    public static LocalDate weekOffset(int offset) {
        return LocalDate.now().plusWeeks(offset).with(DayOfWeek.MONDAY).minusDays(NEXT);
    }

    /**
     * 获取某月的开始日期
     *
     * @param offset 0本月，1下个月，-1上个月，依次类推
     * @return
     */
    public static LocalDate monthOffset(int offset) {
        return LocalDate.now().plusMonths(offset).with(TemporalAdjusters.firstDayOfMonth());
    }


    /**
     * 获取某季度的开始日期
     *
     * @param offset 0本季度，1下个季度，-1上个季度，依次类推
     * @return
     */
    public static LocalDate quarterOffset(int offset) {
        final LocalDate date = LocalDate.now();
        int month = date.getMonth().getValue();//当月
        int start =(month - 1) / 3 ;
        return LocalDate.now().with(TemporalAdjusters.firstDayOfYear()).plusMonths(start * 3).plusMonths(offset * 3);
    }

    /**
     * 获取某年的开始日期
     *
     * @param offset 0今年，1明年，-1去年，依次类推
     * @return
     */
    public static LocalDate yearOffset(int offset) {
        return LocalDate.now().plusYears(offset).with(TemporalAdjusters.firstDayOfYear());
    }

    public static void main(String[] args) {

        System.out.println("当天开始时间>>>"+ dayOffset(0));
        System.out.println("当天结束时间>>>"+ dayOffset(-1));

        System.out.println("本周开始时间>>>"+ weekOffset(0));
        System.out.println("本周结束时间>>>"+ weekOffset(-1));

        System.out.println("本月开始时间>>>"+ monthOffset(0));
        System.out.println("本月结束时间>>>"+ monthOffset(1));

        System.out.println("本年开始时间>>>"+ yearOffset(0));
        System.out.println("本年结束时间>>>"+ yearOffset(1));

        System.out.println("本季度开始时间>>>"+ quarterOffset(0));
        System.out.println("本季度结束时间>>>"+ quarterOffset(1));
    }
}
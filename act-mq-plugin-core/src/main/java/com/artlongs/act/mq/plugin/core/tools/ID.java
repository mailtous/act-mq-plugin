package com.artlongs.act.mq.plugin.core.tools;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.osgl.$;
import org.osgl.util.S;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单 ID 生成
 * 长度 19 位 = "yyyyMMddHHmmssSSS"(17) + (2位)原子顺序数累加
 * Created by ${leeton} on 2018/11/21.
 */
public enum ID {
    ONLY;
    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = 0;
    private long fistNum = 100;
    private static final AtomicInteger atomicNum = new AtomicInteger();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");


    private StringBuffer getYmdId() {
        return new StringBuffer(sdf.format(System.currentTimeMillis()));
    }

    public synchronized String id() {

        StringBuffer ymd = getYmdId();
        String s = ymd.append(getNewAutoNum()).toString();
        long currentTimes = Long.valueOf(s);
        while (currentTimes <= lastTimestamp) {
            ymd=getYmdId();
            currentTimes = Long.valueOf(ymd.append(getNewAutoNum()).toString());
        }

        //上次生成ID的时间截
        lastTimestamp = currentTimes;

        return String.valueOf(currentTimes);
    }

    public String getNewAutoNum(){
        //线程安全的原子操作，所以此方法无需同步
        int newNum = atomicNum.incrementAndGet();
        if(atomicNum.get()>99){
            atomicNum.set(0);
            newNum=atomicNum.get();
        }
        //数字长度为2位，长度不够数字前面补0
        String newStrNum = String.format("%02d", newNum);
        return newStrNum;
    }


    private long add(long lastTimestamp) {
        return lastTimestamp+1;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            String idStr =ID.ONLY.id();
            long id = Long.valueOf(idStr);
            String ymd = StringUtils.left(idStr,17);
            System.err.println(id);
            System.err.println(sdf.format(System.currentTimeMillis()).equals(ymd));
        }

    }

}

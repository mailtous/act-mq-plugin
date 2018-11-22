package com.fiidee.artlongs.mq.tools;

import java.text.SimpleDateFormat;

/**
 * 简单 ID 生成
 * 长度 20 位以上 = "yyyyMMddHHmmssSSS"(17) + 3顺序数累加
 * Created by ${leeton} on 2018/11/21.
 */
public enum ID {
    ONLY;
    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = 100;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");


    private StringBuffer getYmdId() {
        return new StringBuffer(sdf.format(System.currentTimeMillis()));
    }

    public synchronized String id() {
        long timestamp = tilNextMillis(lastTimestamp);

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        //上次生成ID的时间截
        lastTimestamp = timestamp;

        return getYmdId().append(timestamp).toString();
    }

    private long add(long lastTimestamp) {
        return lastTimestamp + 1;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = add(lastTimestamp);
        while (timestamp <= lastTimestamp) {
            timestamp = add(lastTimestamp);
        }
        return timestamp;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            System.err.println(ID.ONLY.id());
        }

    }

}

package com.fiidee.artlongs.mq.tools;

import org.apache.commons.beanutils.BeanUtilsBean;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by leeton on 9/4/17.
 */
public class BeanCopy {

    public static <T> T copy(Object source, T target) {
        try {
            BeanUtilsBean.getInstance().copyProperties(target, source);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return target;
    }

}

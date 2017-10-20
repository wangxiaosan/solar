package com.wwy.common.lang.utils;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * @author wangxiaosan
 * @date 2017/10/20
 */
public class ListUtils {

    public interface Filter<E> {
        /**
         * 满足要求返回true，规则在调用的地方实现
         * @param e
         * @return
         */
        boolean filter(E e);
    }

    /**
     * 过滤
     * @param list
     * @param filter
     * @param <E>
     * @return
     */
    public static <E> List<E> filter(final List<E> list, Filter<E> filter) {
        List<E> newList = Lists.newArrayList();
        if (nonNull(list) && !list.isEmpty()) {
            newList.addAll(list.stream().filter(l->filter.filter(l)).collect(Collectors.toList()));
        }
        return newList;
    }
}

package com.tune;

import java.util.LinkedList;

/**
 * @Author YJ
 * @create 2021/3/25 20:21
 */
public class MqDemo {
    // add remove 支持多线程

    LinkedList<Object> mq = new LinkedList<>();

    Object lock = new Object();

    public void add(Object o) {
        synchronized (lock) {
            mq.addLast(o);
        }
    }

    public Object poll() {
        Object o;
        synchronized (lock) {
            o = mq.getFirst();
        }
        return o;
    }
}

package com.tune;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

    static class Run implements Runnable {
        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public void run() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(sdf.format(new Date()) + " task-" + name + ", " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void exec_oom(ExecutorService service) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date()) + " start");
        Integer n = 1;
        while (n <= 500000000) {
            Run r = new Run();
            r.setName(n.toString());
            service.submit(r);
            if (n % 100 == 0) {
                System.out.println(" n = " + n + ", s: " + service);
            }
            n++;
        }
        service.shutdown();
    }

    private static void exec(ExecutorService service) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date()) + " start");
        Integer n = 1;
        while (n <= 5) {
            Run r = new Run();
            r.setName(n.toString());
            service.submit(r);
            n++;
        }
        service.shutdown();
    }

    private static void fix() {
        //固定核心线程数量
        ExecutorService s = Executors.newFixedThreadPool(3);
        exec(s);
    }

    private static void cache() {
        //适合短时间内大量请求
        ExecutorService s = Executors.newCachedThreadPool();
        exec(s);
    }

    private static void single() {
        //一次只执行一个线程,可以保证顺序执行
        ExecutorService s = Executors.newSingleThreadExecutor();
        exec(s);
    }

    private static void scheduled() {
        // https://blog.csdn.net/liuchangjie0112/article/details/90698401
        ScheduledExecutorService s = Executors.newScheduledThreadPool(3);
        Integer n = 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date()) + " start");
        while (n <= 5) {
            Run r = new Run();
            r.setName(n.toString());
            // 2秒后执行所有任务,但是核心线程数被占满了,只能等待释放 需要调用shutdown来关闭ExecutorService
            s.schedule(r, 2, TimeUnit.SECONDS);
            // 每隔3秒执行所有任务,但是核心线程数被占满了,只能等待释放 需要注释掉shutdown,否则直接退出
//            s.scheduleAtFixedRate(r, 1, 3, TimeUnit.SECONDS);
            // 上一批任务执行完成后,等待3秒,再执行下一批任务,但是核心线程数被占满了,只能等待释放 需要注释掉shutdown,否则直接退出
//            s.scheduleWithFixedDelay(r, 1, 3, TimeUnit.SECONDS);
            n++;
        }
        s.shutdown();
    }


    static class CountDownLatchRun implements Runnable {
        private String name;
        private CountDownLatch cl;

        public CountDownLatchRun(CountDownLatch cl) {
            this.cl = cl;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void run() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(sdf.format(new Date()) + " task-" + name + ", " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cl.countDown();
        }
    }

    private static void fork() throws InterruptedException {
        // 和newFixedThreadPool没有太大区别?
        ExecutorService s = Executors.newWorkStealingPool();
        CountDownLatch cl = new CountDownLatch(15);
        Integer n = 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date()) + " start");
        while (n <= 15) {
            CountDownLatchRun r = new CountDownLatchRun(cl);
            r.setName(n.toString());
            s.execute(r);
            n++;
        }
        cl.await();
        System.out.println(sdf.format(new Date()) + " end");
        s.shutdown();
    }

    private static void custom() {
        // 使用默认的ThreadPoolExecutor构造器 队列 拒绝策略
        // https://www.jianshu.com/p/c41e942bcd64
        // https://upload-images.jianshu.io/upload_images/11183270-a01aea078d7f4178.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp
        ExecutorService s = new ThreadPoolExecutor(2, 3,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(2));
        exec(s);
    }


    public static void main(String[] args) throws InterruptedException {
        fix();
//        cache();
//        single();
//        scheduled();
//        fork();
//        custom();
    }
}

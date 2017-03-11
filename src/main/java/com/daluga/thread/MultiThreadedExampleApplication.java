package com.daluga.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootApplication
public class MultiThreadedExampleApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiThreadedExampleApplication.class);

    @Autowired
    private ApplicationContext context;

    @Value("${number.of.requests}")
    private int numberOfRequests;

    @Value("${number.of.threads}")
    private int numberOfThreads;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(MultiThreadedExampleApplication.class);
        application.setApplicationContextClass(AnnotationConfigApplicationContext.class);
        SpringApplication.run(MultiThreadedExampleApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        LOGGER.debug("Spring Boot multithreaded example has started....");
        LOGGER.debug("Number of requests: " + numberOfRequests);
        LOGGER.debug("Number of threads: " + numberOfThreads);
        LOGGER.debug("Number of processors: " + Runtime.getRuntime().availableProcessors());

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // TODO; This does not seem to work and need to research why.
        // A java program can't terminate or exit while a normal thread is executing. So, left over threads
        // waiting for a never-satisfied event can cause problems. However, if you have blocks that need
        // to be executed (like a finally block to clean up resources) then you should not set daemon threads
        // to true. It all depends on the context of your solution.
//        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads, threadFactory -> {
//            Thread t = new Thread();
//            t.setDaemon(true);
//            return t;
//        });

        List<WorkerThread> tasks = new ArrayList<>(numberOfRequests);

        // --------------------------------------------------------------------------------------------------------------
        // Notes for multithreading separate worker tasks.
        // --------------------------------------------------------------------------------------------------------------
        // 1. You need to create the WorkerThread from the spring context so that bean will be properly injected with
        //    it's dependencies. You cannot use this: WorkerThread wt = new WorkerThread();
        // 2. The WorkerThread class must implement the Callable interface so that it can be executed in a separate
        //    thread.
        // 3. The WorkerThread class needs to have a scope of prototype so that it is not a singleton. The singleton
        //    scope is the default scope for a Spring bean. As a result, this class must have this annotation:
        //    @Scope("prototype")
        // --------------------------------------------------------------------------------------------------------------

        for (int i = 0; i < numberOfRequests; i++) {
            WorkerThread wt = context.getBean(WorkerThread.class, String.valueOf(i));
            tasks.add(wt);
        }

        // The problem with this approach is that this blocks until all threads have completed. As a result, you need
        // to wait before all threads have executed before you see any of the results in the get() method.
        List<Future<String>> futures = executorService.invokeAll(tasks);

        for (Future<String> future : futures) {
            String result = future.get(10000, TimeUnit.MILLISECONDS);
            LOGGER.debug("Thread reply results [" + result + "]");
        }

        executorService.shutdown();

        LOGGER.debug("Spring Boot multithreaded example has ended....");
    }
}

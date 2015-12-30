package com.daluga.messaging.producer;

import com.daluga.messaging.producer.service.Producer;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;

import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootApplication
@EnableJms
public class QueueProducerExampleApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueProducerExampleApplication.class);

    @Autowired
    private Producer producer;

    @Autowired
    private ApplicationContext context;

    @Value("${queue.name}")
    private String queueName;

    @Value("${number.of.requests}")
    private int numberOfRequests;

    @Value("${number.of.threads}")
    private int numberOfThreads;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(QueueProducerExampleApplication.class);
        application.setApplicationContextClass(AnnotationConfigApplicationContext.class);
        SpringApplication.run(QueueProducerExampleApplication.class, args);
    }

    @Bean
    public Queue queue() {
        //return new ActiveMQQueue(queueName);
        return new ActiveMQQueue("baseball.request");
    }

    @Override
    public void run(String... strings) throws Exception {
        LOGGER.debug("Spring Boot messaging producer example has started....");
        LOGGER.debug("Number of requests: " + numberOfRequests);
        LOGGER.debug("Number of threads: " + numberOfThreads);
        LOGGER.debug("Queue name: " + queueName);

        // TODO: Is there a Spring friendly way to create thread pool executors?

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<Future<Producer>> tasks = new ArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {
            // Need to create the producer from the spring context so that bean will be properly injected with it's dependencies.
            // Producer producer = new Producer();
            // Also, the Producer class needs to have a scope of prototype so that it is not a singleton, which is the default scope.
            // As a result, this class must have this annotation: @Scope("prototype")
            Producer producer = context.getBean(Producer.class);
            producer.setPayload(String.valueOf(i));
            Future<Producer> future = executorService.submit(producer, producer);
            tasks.add(future);
        }

        for (Future<Producer> future : tasks) {
            Producer producer = future.get();
            LOGGER.debug("Reply message received...." + producer.getReply());
        }

        executorService.shutdown();

        LOGGER.debug("Spring Boot messaging producer example has ended....");

    }
}

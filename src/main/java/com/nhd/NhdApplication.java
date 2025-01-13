package com.nhd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.nhd.batch.runner.BulkRunner;
import com.nhd.batch.runner.DspRunner;
import com.nhd.batch.runner.TickerRunner;

@EnableScheduling
@SpringBootApplication
public class NhdApplication {
	private static final Logger log = LoggerFactory.getLogger(NhdApplication.class);

	@Autowired
	TickerRunner tickerRunner;

	@Autowired
	DspRunner dspTickerRunner;

	@Autowired
	BulkRunner bulkTickerRunner;

	public static void main(String[] args) {
		SpringApplication.run(NhdApplication.class, args);
	}


//	@Bean
//	public TaskScheduler concurrentTaskScheduler(){
//		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
//		taskScheduler.setPoolSize(Integer.valueOf(3));
//		//taskScheduler.setPoolSize(Integer.valueOf("${app.thread.size}"));
//		return taskScheduler;
//	}


	@Bean
	@Order(1)
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			tickerRunner.runJob();
		};
	}


//	@Bean
//	@Order(2)
//	public CommandLineRunner commandLineRunner1(ApplicationContext ctx) {
//		return args -> {
//			dspTickerRunner.run();
//		};
//	}
//

//	@Bean
//	@Order(3)
//	public CommandLineRunner commandLineRunner1(ApplicationContext ctx) {
//		return args -> {
//			bulkTickerRunner.runJob();
//		};
//	}
}

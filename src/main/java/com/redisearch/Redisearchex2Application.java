package com.redisearch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redisearch.model.Order;
import com.redisearch.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class Redisearchex2Application implements ApplicationRunner {

	@Autowired
	private JedisPooled jedis;

	@Autowired
	private OrderRepository orderRepo;

	@Autowired
	private String luaScript;

	@Value("classpath:data.json")
	Resource resourceFile;


	public static void main(String[] args) {
		SpringApplication.run(Redisearchex2Application.class, args);
	}



	@Override
	public void run(ApplicationArguments args) throws Exception {

		String data = new String(resourceFile.getInputStream().readAllBytes());

		ObjectMapper objectMapper = new ObjectMapper()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Order[] orders = objectMapper.readValue(data, Order[].class);

		Arrays.stream(orders).forEach(orderRepo::save);
		long startTime1 = System.currentTimeMillis();
		orderRepo.search1("GMDC");
		System.out.println("Jedis Execution Time: -> " + (System.currentTimeMillis()-startTime1)+" milli");


		System.out.println("--------------------------------------------------------");
		long startTime = System.currentTimeMillis();
		List<Order> moong = orderRepo.search("GMDC");
		System.out.println("Complete Execution Time: -> " + (System.currentTimeMillis()-startTime)+" milli");

	}
}

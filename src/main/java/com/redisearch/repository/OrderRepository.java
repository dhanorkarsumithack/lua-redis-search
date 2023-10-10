package com.redisearch.repository;


import com.google.gson.Gson;
import com.redisearch.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisPooled;
import java.util.*;


@Repository
public class OrderRepository {

    @Autowired
    private JedisPooled jedis;

    @Autowired
    private String luaScript;

    private final Gson gson = new Gson();

    public Order save(Order order) {
        Gson gson = new Gson();
        String key = order.getInternalOrdNo();
        String jsonOrder = gson.toJson(order);
        jedis.hset("orders", key, jsonOrder);
        return order;
    }



    public List<String > search1(String searchKey){

        try {
            List<String> results = new ArrayList<>();

            // List of Redis keys to search through (assuming "orders" and "orders1" as in your Lua script)
            String[] redisKeys = {"orders"};

            for (String key : redisKeys) {
                Map<String, String> orders = jedis.hgetAll(key);

                for (Map.Entry<String, String> entry : orders.entrySet()) {
                    String json = entry.getValue().toLowerCase();

                    if (json.contains(searchKey.toLowerCase())) {
                        results.add(entry.getValue());
                    }
                }
            }

            // Print results (optional)
            System.out.println("Length -> " + results.size());
            System.out.println(results);

            return results;

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public List<Order> search(String searchKey) {
        try {
            long startTime = System.currentTimeMillis();
            Object result = jedis.evalsha(
                    luaScript,
                    2,
                    "orders","orders1",
                    searchKey
            );
            System.out.println("Redis execution time -> " + (System.currentTimeMillis()-startTime)+" milli");

            if (result instanceof ArrayList) {
                List<String> resultList = (ArrayList<String>) result;
                List<Order> orders = new ArrayList<>();
                for (int i = 0; i < resultList.size(); i++) {
                    String json = resultList.get(i);
                    Order order = gson.fromJson(json, Order.class);
                    orders.add(order);
                }

                System.out.println("length-> "+orders.size());
                System.out.println(orders);
                return orders;
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    public List<Order> search(String searchKey) {
//        try {
//            System.out.println("jedis " + System.currentTimeMillis());
//            Object result = jedis.evalsha(luaScript, 1, "orders", searchKey);
//            System.out.println("jedis " + System.currentTimeMillis());
//
//            if (result instanceof ArrayList) {
//                List<String> resultList = (ArrayList<String>) result;
//
//                Gson gson = new Gson();
//
//                List<Order> orders = resultList.parallelStream()
//                        .filter(json -> {
//                            String lowerJson = json.toLowerCase();
//                            return lowerJson.contains(searchKey);
//                        })
//                        .map(json -> gson.fromJson(json, Order.class))
//                        .collect(Collectors.toList());
//
//                System.out.println("Length -> " + orders.size());
//                System.out.println(orders);
//                return orders;
//            } else {
//                return Collections.emptyList();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }




}

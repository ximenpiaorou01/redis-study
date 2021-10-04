package com.block.redis.controller;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class GoodsController {

    public static final String REDIS_LOCK="block";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;//StringRedisTemplate继承自RedisTemplate

    @Autowired
    private Redisson redisson;


    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/buy_goods")
    public String buy_goods() throws Exception {
        String value= UUID.randomUUID().toString()+Thread.currentThread().getName();
        RLock redissonLock = redisson.getLock(REDIS_LOCK);
        redissonLock.lock();//加锁


        try {
            String res=stringRedisTemplate.opsForValue().get("goods:001");//get key=====看看库存够不够
            int goodsNumber=res==null?0:Integer.parseInt(res);
            if(goodsNumber>0){
                int resNum=goodsNumber-1;
                stringRedisTemplate.opsForValue().set("goods:001",String.valueOf(resNum));
                System.out.println("成功买到商品，还剩下："+resNum+"件！"+serverPort);
                return "成功买到商品，库存还剩下："+resNum+"件，服务提供窗口："+serverPort;
            }else{
                System.out.println("商品已经售完，欢迎下次光临！"+serverPort);
                return "商品已经售完，欢迎下次光临！"+serverPort;
            }
        }finally {

            if(redissonLock.isLocked()){
                if(redissonLock.isHeldByCurrentThread()){
                    redissonLock.unlock();
                }
            }
        }
    }
}
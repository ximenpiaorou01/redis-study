package com.block.redis.controller;

import com.block.redis.utils.RedisUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
            //setnx分布式锁,key加上过期时间，保证即使机器宕机了redis也能到期自动释放锁,而且必须是在获取锁的同时设置过期时间，
            // 保证这个是原子操作,否则分布式环境下，可能导致获取锁了，但是没有设置过期时间
//            Boolean isLock = stringRedisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, value,10l,TimeUnit.SECONDS);
//            if(!isLock){
//                return "抢锁失败";
//            }

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

            //判断是否是自己加的锁，防止线程A执行执行时间过长，导致redis锁过期后自动释放，其他线程抢到锁，然后线程A执行完,
            //再删除时，误删除别人的锁，但是还有问题，分布式获取锁的值和删除不是原子操作,可以用lua脚本或者redis事务保证原子操作
//            while (true){
//                //开启哨兵
//                stringRedisTemplate.watch(REDIS_LOCK);
//                if(stringRedisTemplate.opsForValue().get(REDIS_LOCK).equalsIgnoreCase(value)){
//                    stringRedisTemplate.setEnableTransactionSupport(true);//开启支持事务
//                    stringRedisTemplate.multi();//事务开始
//                    //保证程序是否出现异常，redis分布式锁正常释放
//                    stringRedisTemplate.delete(REDIS_LOCK);
//                    List<Object> list = stringRedisTemplate.exec();
//                    //如果是空，表示没有删除成功，需要自循删除
//                    if(list==null){
//                        continue;
//                    }
//
//                }
//                stringRedisTemplate.unwatch();//关闭哨兵
//                break;//删除成功后退出循环
//            }

            /**
             * lua脚本
             */

//            Jedis jedis = RedisUtils.getJedis();
//
//            //官方lua脚本
//            String luaScript="if redis.call('get',KEYS[1]) == ARGV[1] " +
//                    "then " +
//                    "    return redis.call('del',KEYS[1]) " +
//                    "else " +
//                    "    return 0 " +
//                    "end";
//            try {
//                Object eval = jedis.eval(luaScript, Collections.singletonList(REDIS_LOCK), Collections.singletonList(value));
//                if("1".equals(eval)){
//                    System.out.println("----del redis lock ok");
//                }else {
//                    System.out.println("----del redis lock error");
//                }
//            }finally {
//                if(null!=null){
//                    jedis.close();
//                }
//            }
            //如果redissonLock锁还存在，并且是被当前线程持有，才释放锁
            if(redissonLock.isLocked()){
                if(redissonLock.isHeldByCurrentThread()){
                    redissonLock.unlock();
                }
            }
        }
    }
}
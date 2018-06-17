package com.seckill.dao.cache;

import com.seckill.dao.SeckillDao;
import com.seckill.entity.Seckill;
import org.apache.ibatis.annotations.Param;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
//告诉Jnit spring配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {

    private final long id = 1001;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SeckillDao seckillDao;

    @Test
    public void TestSeckill() {
        Seckill seckill = redisDao.getSeckill(id);
        if (seckill == null){
           seckill = seckillDao.queryById(id);
           if(seckill != null){
               String result = redisDao.putSeckill(seckill);
               System.out.println(result);
               seckill = redisDao.getSeckill(id);
               System.out.println(seckill);
           }
        }
    }
}
package com.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {

    private JedisPool jedisPool;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public RedisDao(String ip,int port){
        jedisPool = new JedisPool(ip,port);
    }

    public Seckill getSeckill(long seckillId){
        //Redis操作
        Jedis jedis=null;
        try{
            jedis = jedisPool.getResource();
            String key = "seckill:"+seckillId;
            //jedis没有实现序列化
            //采用自定义序列化方式
            //需要序列化的对象：pojo含有getset方法
            byte[] bytes = jedis.get(key.getBytes());
            if(bytes != null){
                Seckill seckill = schema.newMessage();
                ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                //seckill被反序列化
                return seckill;
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }finally {
            if(jedis != null) jedis.close();
        }
        return null;
    }

    public String putSeckill(Seckill seckill){
        //object->bytes--->redis     序列化-发送
        Jedis jedis=null;
        try{
            jedis = jedisPool.getResource();
            String key = "seckill:"+seckill.getId();
            byte[] bytes = ProtostuffIOUtil.toByteArray(seckill,schema,
                    LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            int timeout = 60*60;//缓存一小时
            String result = jedis.setex(key.getBytes(),timeout,bytes);
            return result;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }finally {
            if(jedis != null) jedis.close();
        }
        return null;
    }
}

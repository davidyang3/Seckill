package com.seckill.service.impl;

import com.mchange.v1.util.MapUtils;
import com.seckill.dao.SeckillDao;
import com.seckill.dao.SuccessKilledDao;
import com.seckill.dao.cache.RedisDao;
import com.seckill.dto.Exposer;
import com.seckill.dto.SeckillExecution;
import com.seckill.entity.Seckill;
import com.seckill.entity.SuccessKilled;
import com.seckill.enums.SeckillStatEnum;
import com.seckill.exception.RepeatKillException;
import com.seckill.exception.SeckillCloseException;
import com.seckill.exception.SeckillException;
import com.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger=LoggerFactory.getLogger(this.getClass());

    //注入RedisDao
    @Autowired
    private RedisDao redisDao;

    //注入Service依赖
    //根据类型查找bean
    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    //生成md5盐值，用于生成md5
    private final String slat="wqrhewiSEWndsuh-912u9-4hwnbd0pq9-j(2-9he-=**(&***sdqw";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    public Seckill getByID(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        //Redis缓存:建立在超时基础上维护一致性
        //1.访问Redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null){
            seckill = seckillDao.queryById(seckillId);
            if(seckill != null){
                String result = redisDao.putSeckill(seckill);
            }else{
                return new Exposer(false,seckillId);
            }
        }
        Date startTime=seckill.getStartTime();
        Date endTime=seckill.getEndTime();
        Date nowTime=new Date();
        if(nowTime.getTime() <= endTime.getTime() && nowTime.getTime() >= startTime.getTime()){
            //在秒杀时间范围内
            String md5=getMD5(seckillId);
            return new Exposer(true,md5,seckillId);
        }else{
            //不在秒杀时间范围内
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }
    }

    public String getMD5(long seckillId){
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    /**
     * 使用注解配置事务：团队达成一致约定，明确标注事务方法编程风格，保证事务方法执行时间尽可能短，不要穿插其他网络操作，或者剥离到事务外，不是所有方法都需要事务，只读操作/单修改不需要事务
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws SeckillCloseException
     * @throws RepeatKillException
     */
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, SeckillCloseException, RepeatKillException {
        //md5值验证错误
        if(md5 == null ||!md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill date rewrite");
        }
        //执行秒杀逻辑：减库存，记录秒杀行为
        Date nowTime=new Date();
        try {
            //减库存成功，记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            if (insertCount <= 0) {
                //唯一验证，之前ignore，返回0，重复秒杀
                throw new RepeatKillException("seckill repeated");
            } else {
                //减库存
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    //没有更新到记录
                    throw new SeckillCloseException("seckill is closed");
                }else {
                    //秒杀成功
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }
            }
        }catch (SeckillCloseException e1){
            logger.error(e1.getMessage());
            throw e1;
        }catch (RepeatKillException e2){
            logger.error(e2.getMessage());
            throw e2;
        }catch (Exception e){
            logger.error(e.getMessage());
            //所有检查异常，转化为运行时异常
            throw new SeckillException("seckill inner error:"+e.getMessage());
        }
    }

    public SeckillExecution executeSeckillByProcedure(long seckillId, long userPhone, String md5) {
        //md5值验证错误
        if(md5 == null ||!md5.equals(getMD5(seckillId))){
            return new SeckillExecution(seckillId,SeckillStatEnum.DATA_REWRITE);
        }
        Date killTime = new Date();
        Map<String ,Object> map = new HashMap<String ,Object>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);
        try {
            seckillDao.killByProcedure(map);
            //MapUtil
            Object o=map.get("result");
            if(o instanceof Integer){
                int result = (Integer)o;
                if(result == 1){
                    SuccessKilled sk=successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,sk);
                }else{
                    return new SeckillExecution(seckillId,SeckillStatEnum.stateOf(result));
                }
            }else{
                return new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
        }
    }
}

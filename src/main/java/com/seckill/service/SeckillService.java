package com.seckill.service;

import com.seckill.dto.Exposer;
import com.seckill.dto.SeckillExecution;
import com.seckill.entity.Seckill;
import com.seckill.exception.RepeatKillException;
import com.seckill.exception.SeckillCloseException;
import com.seckill.exception.SeckillException;

import java.util.List;

/**
 * 站在“使用者”角度设计接口
 * 三个方面：1.方法定义粒度，参数（简单直接），返回类型（return 类型/throw 异常）
 */
public interface SeckillService {
    /**
     * 查询所有秒杀记录
     * @return 秒杀记录列表
     */
    List<Seckill> getSeckillList();

    /**
     *  查询单条秒杀记录
     * @param seckillId 用于查询的id
     * @return 秒杀记录
     */
    Seckill getByID(long seckillId);

    /**
     * 秒杀开启时，输出秒杀接口的地址，否则输出系统时间和秒杀时间。
     * @param seckillId
     * @return 暴露秒杀DTO
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作
     * @param seckillId
     * @param userPhone
     * @param md5
     * return 返回秒杀执行状态
     */
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, SeckillCloseException, RepeatKillException;

    /**
     * 执行秒杀操作by存储过程
     * @param seckillId
     * @param userPhone
     * @param md5
     * return 返回秒杀执行状态
     */
    SeckillExecution executeSeckillByProcedure(long seckillId, long userPhone, String md5);

}

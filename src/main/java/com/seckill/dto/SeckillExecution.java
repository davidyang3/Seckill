package com.seckill.dto;

import com.seckill.entity.SuccessKilled;
import com.seckill.enums.SeckillStatEnum;

public class SeckillExecution {

    //秒杀id
    private long SeckillId;

    //秒杀执行结果状态
    private int state;

    //解释执行状态标识
    private String stateInfo;

    //秒杀成功后返回秒杀成功对象
    private SuccessKilled successKilled;

    public SeckillExecution(long seckillId, SeckillStatEnum statEnum, SuccessKilled successKilled) {
        SeckillId = seckillId;
        this.state = statEnum.getState();
        this.stateInfo = statEnum.getStateInfo();
        this.successKilled = successKilled;
    }

    public SeckillExecution(long seckillId, SeckillStatEnum statEnum) {
        SeckillId = seckillId;
        this.state = statEnum.getState();
        this.stateInfo = statEnum.getStateInfo();
    }

    public long getSeckillId() {
        return SeckillId;
    }

    public void setSeckillId(long seckillId) {
        SeckillId = seckillId;
    }

    public int getState() {
        return state;
    }

    /*public void setState(int state) {
        this.state = state;
    }*/

    public String getStateInfo() {
        return stateInfo;
    }

    /*public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }*/

    public SuccessKilled getSuccessKilled() {
        return successKilled;
    }

    public void setSuccessKilled(SuccessKilled successKilled) {
        this.successKilled = successKilled;
    }

    @Override
    public String toString() {
        return "SeckillExecution{" +
                "SeckillId=" + SeckillId +
                ", state=" + state +
                ", stateInfo='" + stateInfo + '\'' +
                ", successKilled=" + successKilled +
                '}';
    }
}

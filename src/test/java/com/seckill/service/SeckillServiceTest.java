package com.seckill.service;

import com.seckill.dto.Exposer;
import com.seckill.dto.SeckillExecution;
import com.seckill.entity.Seckill;
import com.seckill.exception.RepeatKillException;
import com.seckill.exception.SeckillCloseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml","classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

    private Logger logger=LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        List<Seckill> list=seckillService.getSeckillList();
        logger.info("lost={}",list);
    }

    @Test
    public void getByID() {
        Seckill seckill=seckillService.getByID(1000);
        logger.info("seckill={}",seckill);
    }

    @Test
    public void testSecidkill() {
        long id=1003;
        long phone=12323124122L;
        Exposer exposer=seckillService.exportSeckillUrl(id);
        logger.info("exposer:{}",exposer);
        if(exposer.isExposed()) {
         //秒杀已经开始
            try {
                SeckillExecution seckillExcution = seckillService.executeSeckill(id, phone, exposer.getMd5());
                logger.info("result={}", seckillExcution);
            } catch (RepeatKillException e) {
                logger.error(e.getMessage());
            } catch (SeckillCloseException e) {
                logger.error(e.getMessage());
            }
        }else {
            //秒杀未开始
            logger.warn("exposer={}",exposer);
        }
    }

    @Test
    public void TestProc(){
        long seckillId = 1003;
        long phone = 12312312212L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if(exposer.isExposed()){
            String md5 = exposer.getMd5();
            SeckillExecution seckillExecution = seckillService.executeSeckillByProcedure(seckillId,phone,md5);
            System.out.println(seckillExecution);
        }
    }
}
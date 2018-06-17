package com.seckill.web;

import com.seckill.dto.Exposer;
import com.seckill.dto.SeckillExecution;
import com.seckill.dto.SeckillResult;
import com.seckill.entity.Seckill;
import com.seckill.enums.SeckillStatEnum;
import com.seckill.exception.RepeatKillException;
import com.seckill.exception.SeckillCloseException;
import com.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller//放入容器
@RequestMapping("/seckill")//模块 url:/模块/资源/{id}/细分
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/list",method=RequestMethod.GET)
    public String List(Model model){
        model.addAttribute("list",seckillService.getSeckillList());
        return "list";//    WEB-INF/jsp/list.jsp
    }

    @RequestMapping(value = "/{id}/detail",method = RequestMethod.GET)
    public String detail(Model model,@PathVariable(name = "id")Long id){
        if(id == null){
            return "redirect:/seckill/list";
        }
        Seckill seckill=seckillService.getByID(id);
        if(seckill == null){
            return "forward:/seckill/list";
        }
        model.addAttribute("seckill",seckill);
        return "detail";
    }

    @RequestMapping(value = "/{id}/exposer",method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable("id") Long id) {
        if (id == null) {
            return new SeckillResult<Exposer>(false, "id不存在");
        }
        try {
            Exposer exposer = seckillService.exportSeckillUrl(id);
            if (exposer == null) {
                return new SeckillResult<Exposer>(false, "id不存在");
            }
            return new SeckillResult<Exposer>(true, exposer);
        }catch (Exception e){
            logger.error(e.getMessage());
            return new SeckillResult<Exposer>(false,e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}/{md5}/execute",method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("id") Long id,@CookieValue(value = "killPhone",required = false) Long userPhone,@PathVariable("md5") String md5){
        SeckillResult <SeckillExecution> result ;
        if (id == null) {
            result = new SeckillResult<SeckillExecution>(false, "id不存在");
        }else if(userPhone == null){
            result = new SeckillResult<SeckillExecution>(false,"未注册");
        }else {
            //调用秒杀存储过程
            SeckillExecution seckillExecution = seckillService.executeSeckillByProcedure(id, userPhone, md5);
            if (seckillExecution == null) {
                result = new SeckillResult<SeckillExecution>(false, "id不存在");
            }else {
                result = new SeckillResult<SeckillExecution>(true, seckillExecution);
            }
        }
        return result;
    }

    @RequestMapping(value = "/time/now",method = RequestMethod.GET,produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Long> time(){
        Date now =new Date();
        return new SeckillResult<Long>(true,now.getTime());
    }
}

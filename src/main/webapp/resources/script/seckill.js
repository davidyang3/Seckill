//存放主要交互逻辑js代码
var seckill={
    //封装秒杀相关ajax的url
    URL : {
        now : function(){
            return '/seckill/time/now';
        },
        exposer:function (seckillId) {
            return '/seckill/'+seckillId+'/exposer';
        },
        execution:function(seckillId,md5){
            return '/seckill/'+seckillId+'/'+md5+'/execute';
        }
    },
    //验证手机号
    validatePhone:function (phone) {
        if(phone && phone.length == 11 && !isNaN(phone)){
            return true;
        }else {
            return false;
        }
    },
    //处理秒杀逻辑
    handlerSeckill:function(seckillId,node){
        node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');
        $.post(seckill.URL.exposer(seckillId),{},function (result) {
            //在回调函数中执行交互流程
            if(result && result['success']){
                var exposer=result['data'];
                if(exposer['exposed']){
                    //秒杀开启
                    //获取秒杀地址
                    var md5=exposer['md5'];
                    var killUrl=seckill.URL.execution(seckillId,md5);
                    //绑定一次点击事件
                    $('#killBtn').one('click',function () {
                       //绑定执行秒杀操作
                       //先禁用按钮
                        $(this).addClass('disabled');
                       //2.发送秒杀请求
                        $.post(killUrl,{},function (result) {
                            if(result && result['success']){
                                //显示秒杀结果
                                var killResult=result['data'];
                                var state=killResult['state'];
                                var stateInfo=killResult['stateInfo'];
                                node.html('<span class="label label-success">'+stateInfo+'</span>').show()
                            }else{

                            }
                        });
                    });
                    node.show();
                }else{
                    //未开启秒杀
                    var now=exposer['now'];
                    var start=exposer['start'];
                    var end=exposer['end'];
                    //重新计时
                    seckill.countdown(seckillId,now,start,end);
                }
            }
        })
    },
    //计时
    countdown:function (seckillId,nowTime,startTime,endTime) {
        //时间判断
        var seckillBox = $('#seckill-box');
        if(nowTime>endTime){
            //秒杀结束
            seckillBox.html('秒杀结束！');
        }else if(nowTime<startTime){
            //秒杀未开始
            var killTime=new Date(startTime+1000);
            seckillBox.countdown(killTime,function (event) {
                var format=event.strftime('秒杀倒计时：%D天 %H时 %M分 %S秒 ');
                seckillBox.html(format);
            }).on('finish.countdown',function () {
                //获取秒杀地址，控制显示逻辑，执行秒杀
                seckill.handlerSeckill(seckillId,seckillBox);
            });
        }else{
            //秒杀开始
            seckill.handlerSeckill(seckillId,seckillBox);
        }
    },
    //详情页秒杀逻辑
    detail:{
        //详情页初始化
        init : function (params) {
            //手机验证和登陆，计时交互
            //规划交互流程
            //cookie数据
            var killPhone = $.cookie('killPhone');
            //验证手机号
            if(!seckill.validatePhone(killPhone)){
                //绑定Phone
                var killPhoneModal = $('#killPhoneModal');
                killPhoneModal.modal({
                    show:true,//显示弹出层
                    backdrop:'static',//禁止位置关闭
                    keyboard:false//关闭键盘事件
                });
                $('#killPhoneBtn').click(function () {
                   var inputPhone = $('#killPhoneKey').val();
                   if(seckill.validatePhone(inputPhone)){
                       //写入电话写入cookie
                       $.cookie('killPhone',inputPhone,
                           {
                               expires:7 ,
                               path:'/seckill'
                           });
                       window.location.reload();
                   }else{
                       $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误</label>').show(300);
                   }
                });
            }else{
                var startTime = params['startTime'];
                var endTime = params['endTime'];
                var seckillId = params['seckillId'];
                $.get(seckill.URL.now(),{},function (result) {
                    if(result && result['success']){
                        var nowTime = result['data'];
                        //判断时间
                        seckill.countdown(seckillId,nowTime,startTime,endTime);
                    }
                });
            }
        }
    }
}
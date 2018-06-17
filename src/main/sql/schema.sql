-- 数据库初始化脚本
create DATABASE seckill;
-- 使用数据库
use seckill;
--创建秒杀库存表
create table seckill(
seckill_id bigint Not NULL AUTO_INCREMENT COMMENT '商品库存id',
name varchar(120) not null comment '商品名称',
number int  not null comment '库存数量',
start_time timestamp not null comment '秒杀开启时间',
end_time timestamp not null comment '秒杀结束时间',
create_time timestamp not null default CURRENT_TIMESTAMP comment '创建时间',
primary key (seckill_id),
key idx_start_time(start_time),
key idx_end_time(end_time),
key idx_create_time(create_time)
)ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT='秒杀库存表';

--初始化数据
insert into
  seckill(name,number,start_time,end_time)
values
  ('1000元秒杀iphone6',100,'2018-6-5 00:00:00','2018-6-6 00:00:00'),
  ('500元秒杀ipad2',200,'2018-6-5 00:00:00','2018-6-6 00:00:00'),
  ('300元秒杀小米4',300,'2018-6-5 00:00:00','2018-6-6 00:00:00'),
  ('200元秒杀红米note',400,'2018-6-5 00:00:00','2018-6-6 00:00:00');

--设计秒杀成功明细表
--用户登陆认证相关信息
alert table success_killed(
seckill_id bigint not null comment '秒杀商品id',
user_phone bigint not null comment '用户手机号',
state tinyint not null default -1 comment '状态标识 -1无效 0成功 1已付款 2已发货',
create_time timestamp not null default CURRENT_TIMESTAMP comment '创建时间',
primary key (seckill_id,user_phone),/*联合主键*/
key idx_create_time(create_time)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='秒杀成功明细表';

--连接数据库控制台
mysql -uroot -p
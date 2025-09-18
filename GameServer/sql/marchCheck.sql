-- 注意在执行之前修改时间........

-- 行军类型总数
select marchType, count(1) from world_march group by marchType;
-- 行军状态总数
select marchStatus, count(1) from world_march group by marchStatus;
-- 出征和回城中，结束时间小于当前时间的行军(此条最重要, 如果不为空, 必有问题)
select * from world_march where endTime < (unix_timestamp(now()) * 1000) and marchStatus in (1, 6, 12);
-- 行军结束时间超过24小时的行军类型数量
select marchType, count(1) from world_march where endTime > (unix_timestamp('2018-01-27 23:12:00') * 1000) group by marchType;
-- 行军结束时间超过24小时的行军状态数量
select marchStatus, count(1) from world_march where endTime > (unix_timestamp('2018-01-27 23:12:00') * 1000) group by marchStatus;
-- 行军结束时间和采集时间小于当前时间的行军类型数量
select marchType, count(1) from world_march where endTime < (unix_timestamp('2018-01-26 23:12:00') * 1000) and resEndTime < (unix_timestamp('2018-01-26 23:12:00') * 1000) group by marchType;
-- 行军结束时间和采集时间小于当前时间的行军状态数量
select marchStatus, count(1) from world_march where endTime < (unix_timestamp('2018-01-26 23:15:00') * 1000) and resEndTime < (unix_timestamp('2018-01-26 23:15:00') * 1000) group by marchStatus;
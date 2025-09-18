package com.hawk.activity.type.impl.exchangeTip;

import com.hawk.game.protocol.Activity.GeneralExchangeTip;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.result.Result;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 兑换提醒活动需要实现此接口
 * @param <T> 兑换配置
 */
public interface IExchangeTip<T extends AExchangeTipConfig> {
    int CLOSE = 0;
    int OPEN = 1;
    /**
     * 设置勾选提醒
     * @param entity 数据库实体
     * @param tips 勾选信息
     * @return
     */
    default Result<?> chooseTip(IExchangeTipEntity entity, List<GeneralExchangeTip> tips){
        if(tips == null || tips.isEmpty()){
            return Result.success();
        }
        Set<Integer> tipSet = entity.getTipSet();
        if(tipSet == null){
            tipSet = new HashSet<>();
        }
        for(GeneralExchangeTip tip : tips){
            switch (tip.getTip()){
                case CLOSE :{
                    tipSet.add(tip.getId());
                }
                break;
                case OPEN:{
                    tipSet.remove(tip.getId());
                }
                break;
            }
        }
        entity.setTipSet(tipSet);
        return Result.success();
    }

    /**
     * 获得勾选信息
     * @param tClass 兑换配置类
     * @param tips 配置信息
     * @return 勾选信息
     * @param <T> 兑换配置
     */
    @SuppressWarnings("hiding")
	default <T extends AExchangeTipConfig> List<GeneralExchangeTip> getTips(Class<T> tClass, Set<Integer> tips){
        List<GeneralExchangeTip> pbTips = new ArrayList<>();
        ConfigIterator<T> iterator = HawkConfigManager.getInstance().getConfigIterator(tClass);
        while (iterator.hasNext()) {
            T cfg = iterator.next();
            if(!tips.contains(cfg.getId())){
                GeneralExchangeTip.Builder builder = GeneralExchangeTip.newBuilder();
                builder.setId(cfg.getId());
                builder.setTip(OPEN);
                pbTips.add(builder.build());
            }
        }
        return pbTips;
    }
}

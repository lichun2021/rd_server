package com.hawk.activity.type.impl.roseGift;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * 玫瑰赠礼协议类
 */
public class RoseGiftHandler extends ActivityProtocolHandler {
    @ProtocolHandler(code = HP.code.ROSE_GIFT_REQ_VALUE)
    public void reaInfo(HawkProtocol protocol, String playerId){
        //获得活动实例
        RoseGiftActivity activity = getActivity(ActivityType.ROSE_GIFT);
        activity.syncActivityDataInfo(playerId);
    }

    /**
     * 兑换
     * @param protocol 前端协议
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code.ROSE_GIFT_EXCHANGE_VALUE)
    public void exchange(HawkProtocol protocol, String playerId){
        //获得活动实例
        RoseGiftActivity activity = getActivity(ActivityType.ROSE_GIFT);
        //解析前端协议
        Activity.RoseGiftExchangeInfo req = protocol.parseProtocol(Activity.RoseGiftExchangeInfo.getDefaultInstance());
        //执行兑换逻辑
        Result<?> result = activity.exchange(playerId, req.getExchangeId(), req.getNum());
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    /**
     * 兑换提醒
     * @param protocol 前端协议
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code.ROSE_GIFT_TIPS_VALUE)
    public void exchangeTips(HawkProtocol protocol, String playerId){
        //获得活动实例
        RoseGiftActivity activity = getActivity(ActivityType.ROSE_GIFT);
        //解析前端协议
        Activity.RoseGiftExchangeTipsInfo req = protocol.parseProtocol(Activity.RoseGiftExchangeTipsInfo.getDefaultInstance());
        //勾选提醒协议
        int tip = 0;
        List<Integer> ids = new ArrayList<>();
        for(Activity.RoseGiftExchangeTips exchangeTips : req.getTipsList()){
            tip = exchangeTips.getTip();
            ids.add(exchangeTips.getId());
        }
        Result<?> result = activity.exchangeTips(playerId, ids, tip);
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    /**
     * 抽奖
     * @param protocol 前端协议
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code.ROSE_GIFT_DRAW_VALUE)
    public void draw(HawkProtocol protocol, String playerId){
        //获得活动实例
        RoseGiftActivity activity = getActivity(ActivityType.ROSE_GIFT);
        //执行抽奖逻辑
        Result<?> result = activity.draw(playerId);
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }



}

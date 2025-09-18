package com.hawk.activity.type.impl.fristRechagerThree;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.FirstRechargeThreeAward;
import com.hawk.game.protocol.HP;

/**
 * 新首充协议类
 */
public class FirstRechargeThreeHandler extends ActivityProtocolHandler {

 
    @ProtocolHandler(code = HP.code2.FIRST_RECHARGE_THREE_REQ_VALUE)
    public void onReqInfo(HawkProtocol hawkProtocol, String playerId) {
        //获取新首充活动
        FirstRechargeThreeActivity activity = this.getActivity(ActivityType.FIRST_RECHARGE_THREE);
        //如果活动为空直接返回
        if(activity == null){
            return;
        }
        //同步数据给前端
        activity.syncActivityDataInfo(playerId);
    }

  
    @ProtocolHandler(code = HP.code2.FIRST_RECHARGE_THREE_AWARD_VALUE)
    public void onGetReward(HawkProtocol hawkProtocol, String playerId) {
        //获取新首充活动
    	FirstRechargeThreeActivity activity = this.getActivity(ActivityType.FIRST_RECHARGE_THREE);
        //如果活动为空直接返回
        if(activity == null){
            return;
        }
        //解析前端协议
        FirstRechargeThreeAward award = hawkProtocol.parseProtocol(FirstRechargeThreeAward.getDefaultInstance());
        //领奖
        Result<?> result = activity.reward(playerId, award.getId());
        //如果逻辑没有成功执行，直接返回错误码
        if (result.isFail()) {
            //给前端返回错误信息
            sendErrorAndBreak(playerId, hawkProtocol.getType(), result.getStatus());
        }
    }

  
}

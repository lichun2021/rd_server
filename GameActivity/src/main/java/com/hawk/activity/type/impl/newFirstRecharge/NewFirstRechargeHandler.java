package com.hawk.activity.type.impl.newFirstRecharge;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.NewFirstRechargeAward;
import com.hawk.game.protocol.Activity.NewFirstRechargePop;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

/**
 * 新首充协议类
 */
public class NewFirstRechargeHandler extends ActivityProtocolHandler {

    /**
     * 前端主动请求活动数据协议
     * @param hawkProtocol 前端请求
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code.NEW_FIRST_RECHARGE_REQ_VALUE)
    public void onReqInfo(HawkProtocol hawkProtocol, String playerId) {
        //获取新首充活动
        NewFirstRechargeActivity activity = this.getActivity(ActivityType.NEW_FIRST_RECHARGE);
        //如果活动为空直接返回
        if(activity == null){
            return;
        }
        //同步数据给前端
        activity.syncActivityDataInfo(playerId);
    }

    /**
     * 领奖协议
     * @param hawkProtocol 前端请求
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code.NEW_FIRST_RECHARGE_AWARD_VALUE)
    public void onGetReward(HawkProtocol hawkProtocol, String playerId) {
        //获取新首充活动
        NewFirstRechargeActivity activity = this.getActivity(ActivityType.NEW_FIRST_RECHARGE);
        //如果活动为空直接返回
        if(activity == null){
            return;
        }
        //解析前端协议
        NewFirstRechargeAward award = hawkProtocol.parseProtocol(NewFirstRechargeAward.getDefaultInstance());
        //领奖
        Result<?> result = activity.reward(playerId, award.getId());
        //如果逻辑没有成功执行，直接返回错误码
        if (result.isFail()) {
            //给前端返回错误信息
            sendErrorAndBreak(playerId, hawkProtocol.getType(), result.getStatus());
        }
    }

    /**
     * 记录弹窗等级协议
     * @param hawkProtocol 前端请求
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code.NEW_FIRST_RECHARGE_POP_VALUE)
    public void onPop(HawkProtocol hawkProtocol, String playerId) {
        //获取新首充活动
        NewFirstRechargeActivity activity = this.getActivity(ActivityType.NEW_FIRST_RECHARGE);
        //如果活动为空直接返回
        if(activity == null){
            return;
        }
        //解析前端协议
        NewFirstRechargePop pop = hawkProtocol.parseProtocol(NewFirstRechargePop.getDefaultInstance());
        //记录弹窗
        Result<Integer> result = activity.pop(playerId, pop.getLevel());
        //如果逻辑没有成功执行，直接返回错误码
        if (result.isFail()) {
            //给前端返回错误信息
            sendErrorAndBreak(playerId, hawkProtocol.getType(), result.getStatus());
        }
    }
}

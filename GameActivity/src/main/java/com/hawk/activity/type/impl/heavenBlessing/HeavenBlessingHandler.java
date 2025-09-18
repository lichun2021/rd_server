package com.hawk.activity.type.impl.heavenBlessing;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.Activity.HeavenBlessingChoose;
import com.hawk.game.protocol.Activity.HeavenBlessingAward;
import org.hawk.result.Result;

/**
 * 鸿福天降，前端活动协议处理类
 */
public class HeavenBlessingHandler extends ActivityProtocolHandler {

    /**
     * 更换自定义奖励
     * @param hawkProtocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code.HEAVEN_BLESSING_CHOOSE_VALUE)
    public void choose(HawkProtocol hawkProtocol, String playerId) {
        //获得鸿福天降活动实例
        HeavenBlessingActivity activity = this.getActivity(ActivityType.HEAVEN_BESSING_ACTIVTY);
        //活动为空直接返回
        if(activity == null){
            return;
        }
        //获得前端协议
        HeavenBlessingChoose choose = hawkProtocol.parseProtocol(HeavenBlessingChoose.getDefaultInstance());
        //选择自定义奖励
        Result<?> result = activity.choose(playerId, choose.getChoose());
        //如果逻辑没有成功执行，直接返回错误码
        if (result.isFail()) {
            sendErrorAndBreak(playerId, hawkProtocol.getType(), result.getStatus());
        }
    }


    /**
     * 领取自定义奖励
     * @param hawkProtocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code.HEAVEN_BLESSING_AWARD_VALUE)
    public void award(HawkProtocol hawkProtocol, String playerId) {
        //获得鸿福天降活动实例
        HeavenBlessingActivity activity = this.getActivity(ActivityType.HEAVEN_BESSING_ACTIVTY);
        //活动为空直接返回
        if(activity == null){
            return;
        }
        //获得前端协议
        HeavenBlessingAward award = hawkProtocol.parseProtocol(HeavenBlessingAward.getDefaultInstance());
        //领取自定义奖励
        Result<?> result = activity.award(playerId);
        //如果逻辑没有成功执行，直接返回错误码
        if (result.isFail()) {
            sendErrorAndBreak(playerId, hawkProtocol.getType(), result.getStatus());
        }

    }

    /**
     * 前端打开页面日志打点
     * @param hawkProtocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code.HEAVEN_BLESSING_LOG_VALUE)
    public void log(HawkProtocol hawkProtocol, String playerId) {
        //获得鸿福天降活动实例
        HeavenBlessingActivity activity = this.getActivity(ActivityType.HEAVEN_BESSING_ACTIVTY);
        //活动为空直接返回
        if(activity == null){
            return;
        }
        activity.getDataGeter().logHeavenBlessingOpen(playerId);

    }
}

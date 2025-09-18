package com.hawk.activity.type.impl.gratefulBenefits;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.GratefulBenefitsHelp;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

/**
 * 感恩福利 活动协议处理
 */
public class GratefulBenefitsHandler extends ActivityProtocolHandler {
    /**
     * 请求活动数据
     * @param hawkProtocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code.GRATEFUL_BENEFITS_REQ_VALUE)
    public void request(HawkProtocol hawkProtocol, String playerId) {
        //获得感恩福利活动实例
        GratefulBenefitsActivity activity = this.getActivity(ActivityType.GRATEFUL_BENEFITS_ACTIVTY);
        //活动为空直接返回
        if(activity == null){
            return;
        }
        //请求活动数据
        Result<?> result = activity.request(playerId);
        //如果逻辑没有成功执行，直接返回错误码
        if (result.isFail()) {
            sendErrorAndBreak(playerId, hawkProtocol.getType(), result.getStatus());
        }
    }

    /**
     * 签到
     * @param hawkProtocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code.GRATEFUL_BENEFITS_PUNCH_VALUE)
    public void punch(HawkProtocol hawkProtocol, String playerId) {
        //获得感恩福利活动实例
        GratefulBenefitsActivity activity = this.getActivity(ActivityType.GRATEFUL_BENEFITS_ACTIVTY);
        //活动为空直接返回
        if(activity == null){
            return;
        }
        //签到
        Result<?> result = activity.punch(playerId);
        //如果逻辑没有成功执行，直接返回错误码
        if (result.isFail()) {
            sendErrorAndBreak(playerId, hawkProtocol.getType(), result.getStatus());
        }
    }

    /**
     * 邀请盟友
     * @param hawkProtocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code.GRATEFUL_BENEFITS_INVITE_VALUE)
    public void invite(HawkProtocol hawkProtocol, String playerId) {
        //获得感恩福利活动实例
        GratefulBenefitsActivity activity = this.getActivity(ActivityType.GRATEFUL_BENEFITS_ACTIVTY);
        //活动为空直接返回
        if(activity == null){
            return;
        }
        //邀请盟友
        Result<?> result = activity.invite(playerId);
        //如果逻辑没有成功执行，直接返回错误码
        if (result.isFail()) {
            sendErrorAndBreak(playerId, hawkProtocol.getType(), result.getStatus());
        }
    }

    /**
     * 领奖
     * @param hawkProtocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code.GRATEFUL_BENEFITS_AWARD_VALUE)
    public void award(HawkProtocol hawkProtocol, String playerId) {
        //获得感恩福利活动实例
        GratefulBenefitsActivity activity = this.getActivity(ActivityType.GRATEFUL_BENEFITS_ACTIVTY);
        //活动为空直接返回
        if(activity == null){
            return;
        }
        //领奖
        Result<?> result = activity.award(playerId);
        //如果逻辑没有成功执行，直接返回错误码
        if (result.isFail()) {
            sendErrorAndBreak(playerId, hawkProtocol.getType(), result.getStatus());
        }
    }

    /**
     * 帮助其他玩家
     * @param hawkProtocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code.GRATEFUL_BENEFITS_HELP_VALUE)
    public void help(HawkProtocol hawkProtocol, String playerId) {
        //获得感恩福利活动实例
        GratefulBenefitsActivity activity = this.getActivity(ActivityType.GRATEFUL_BENEFITS_ACTIVTY);
        //活动为空直接返回
        if(activity == null){
            return;
        }
        //获得前端协议
        GratefulBenefitsHelp help = hawkProtocol.parseProtocol(GratefulBenefitsHelp.getDefaultInstance());
        //帮助其他玩家
        Result<?> result = activity.help(playerId, help.getTargetPlayerId());
        //如果逻辑没有成功执行，直接返回错误码
        if (result.isFail()) {
            sendErrorAndBreak(playerId, hawkProtocol.getType(), result.getStatus());
        }
    }
}

package com.hawk.activity.type.impl.guildBack;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class GuildBackHandler extends ActivityProtocolHandler {

    @ProtocolHandler(code = HP.code2.GUILD_BACK_INFO_REQ_VALUE)
    public void info(HawkProtocol protocol, String playerId){
        GuildBackActivity activity = getActivity(ActivityType.GUILD_BACK);
        //兑换
        Result<?> result = activity.info(playerId);
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.GUILD_BACK_EXCHANGE_REQ_VALUE)
    public void exchange(HawkProtocol protocol, String playerId){
        Activity.GuildBackExchangeReq req = protocol.parseProtocol(Activity.GuildBackExchangeReq.getDefaultInstance());
        GuildBackActivity activity = getActivity(ActivityType.GUILD_BACK);
        //兑换
        Result<?> result = activity.exchange(playerId, req.getGoodsId(), req.getExhangeTimes());
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.GUILD_BACK_INVITE_REQ_VALUE)
    public void invite(HawkProtocol protocol, String playerId){
        Activity.GuildBackInviteReq req = protocol.parseProtocol(Activity.GuildBackInviteReq.getDefaultInstance());
        GuildBackActivity activity = getActivity(ActivityType.GUILD_BACK);
        //兑换
        Result<?> result = activity.invite(playerId, req.getPlayerId());
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.GUILD_BACK_INVITE_LIST_REQ_VALUE)
    public void inviteList(HawkProtocol protocol, String playerId){
        Activity.GuildBackInviteListReq req = protocol.parseProtocol(Activity.GuildBackInviteListReq.getDefaultInstance());
        GuildBackActivity activity = getActivity(ActivityType.GUILD_BACK);
        //兑换
        Result<?> result = activity.inviteList(playerId, req.getIsSwitch());
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.GUILD_BACK_VIT_POOL_REWARD_REQ_VALUE)
    public void vitPoolReward(HawkProtocol protocol, String playerId){
        GuildBackActivity activity = getActivity(ActivityType.GUILD_BACK);
        //兑换
        Result<?> result = activity.poolReward(playerId, 2);
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.GUILD_BACK_GOLD_POOL_REWARD_REQ_VALUE)
    public void goldPoolReward(HawkProtocol protocol, String playerId){
        GuildBackActivity activity = getActivity(ActivityType.GUILD_BACK);
        //兑换
        Result<?> result = activity.poolReward(playerId, 1);
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.GUILD_BACK_BOX_REWARD_REQ_VALUE)
    public void boxReward(HawkProtocol protocol, String playerId){
        Activity.GuildBackBoxRewardReq req = protocol.parseProtocol(Activity.GuildBackBoxRewardReq.getDefaultInstance());
        GuildBackActivity activity = getActivity(ActivityType.GUILD_BACK);
        //兑换
        Result<?> result = activity.boxReward(playerId, req.getIsAll());
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }
}

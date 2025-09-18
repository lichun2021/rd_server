package com.hawk.activity.type.impl.seasonActivity;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.seasonActivity.cfg.SeasonConstCfg;
import com.hawk.activity.type.impl.seasonActivity.rank.GuildSeasonKingGradeInfo;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.YQZZWar;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

/**
 * 赛季活动消息处理类
 */
public class SeasonActivityHandler extends ActivityProtocolHandler {

    /**
     * 前端请求活动信息
     * @param protocol 前端协议
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code2.SEASON_INFO_GET_VALUE)
    public void infoGet(HawkProtocol protocol, String playerId){
        //获得活动
        SeasonActivity activity = getActivity(ActivityType.SEASON_ACTIVITY);
        //同步数据
        activity.syncActivityDataInfo(playerId);
    }

    @ProtocolHandler(code = HP.code2.SEASON_CLIENT_LEVEL_UPDATE_VALUE)
    public void clientLevelUpdate(HawkProtocol protocol, String playerId){
        //获得活动
        SeasonActivity activity = getActivity(ActivityType.SEASON_ACTIVITY);
        Activity.SeasonClientLevelUpdate clientLevelUpdate = protocol.parseProtocol(Activity.SeasonClientLevelUpdate.getDefaultInstance());
        //同步数据
        Result<?> result = activity.clientLevelupdate(playerId, clientLevelUpdate.getLevel());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    /**
     * 兑换物品
     * @param protocol 前端协议
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code2.SEASON_SHOP_EXCHANGE_VALUE)
    public void seasonShopExchange(HawkProtocol protocol, String playerId){
        //获得活动
        SeasonActivity activity = getActivity(ActivityType.SEASON_ACTIVITY);
        //兑换信息
        Activity.OrderShopItemPB shopItem = protocol.parseProtocol(Activity.OrderShopItemPB.getDefaultInstance());
        //兑换
        Result<?> result = activity.exchange(playerId, shopItem.getId(), shopItem.getCount());
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    /**
     * 战令领奖
     * @param protocol 前端协议
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code2.SEASON_ORDER_REWARD_GET_VALUE)
    public void seasonOrderRewardGet(HawkProtocol protocol, String playerId){
        //获得活动
        SeasonActivity activity = getActivity(ActivityType.SEASON_ACTIVITY);
        //领奖信息
        Activity.SeasonOrderRewardGet rewardGet = protocol.parseProtocol(Activity.SeasonOrderRewardGet.getDefaultInstance());
        //领奖
        Result<?> result = activity.reward(playerId, rewardGet.getType(), (int)rewardGet.getLevel());
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    /**
     * 请求王者段位排行榜
     * @param protocol 前端协议
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code2.SEASON_GUILD_KING_RANK_REQ_VALUE)
    public void seasonGuildKingRankReq(HawkProtocol protocol, String playerId){
        //获得活动
        SeasonActivity activity = getActivity(ActivityType.SEASON_ACTIVITY);
        //同步排行榜信息
        activity.pushRankInfo(playerId);
    }

}

package com.hawk.activity.type.impl.urlReward;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.ParameterizedType;

public interface IURLReward<T extends URLRewardBaseCfg> {
    String URL_REWARD = "URL_REWARD";

    default Class<T> getTClass()
    {
        Class<T> tClass = (Class<T>)((ParameterizedTypeImpl)getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
        return tClass;
    }

    default String getURLRewardCfg(){
        return HawkConfigManager.getInstance().getKVInstance(getTClass()).getUrlDailyReward();
    }

    default Action takeURLRewardAction(){
        return Action.URL_REWARD;
    }

    default Result<?> getURLReward(String playerId, int activityId){
        String urlReward = getURLRewardCfg();
        if(HawkOSOperator.isEmptyString(urlReward)){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        String key = URL_REWARD + ":" + playerId;
        String activityKey = String.valueOf(activityId);
        String timeStr = ActivityGlobalRedis.getInstance().hget(key, activityKey);
        long now = HawkTime.getMillisecond();
        long updateTime = HawkOSOperator.isEmptyString(timeStr) ? 0L : Long.parseLong(timeStr);
        if(HawkTime.isSameDay(updateTime, now)){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        ActivityGlobalRedis.getInstance().hset(key, activityKey, String.valueOf(now));
        ActivityManager.getInstance().getDataGeter().takeReward(playerId, RewardHelper.toRewardItemImmutableList(urlReward), 1 , takeURLRewardAction(), true);
        Activity.URLRewardSync.Builder builder = Activity.URLRewardSync.newBuilder();
        builder.addItems(Activity.URLRewardItem.newBuilder().setActivityId(activityId).setCanGet(false));
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.URL_REWARD_SYNC, builder));
        return Result.success();
    }
}

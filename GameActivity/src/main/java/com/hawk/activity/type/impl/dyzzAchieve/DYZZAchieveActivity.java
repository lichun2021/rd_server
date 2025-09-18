package com.hawk.activity.type.impl.dyzzAchieve;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.*;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.dyzzAchieve.cfg.DYZZAchieveCfg;
import com.hawk.activity.type.impl.dyzzAchieve.cfg.DYZZAchieveKVCfg;
import com.hawk.activity.type.impl.dyzzAchieve.entity.DYZZAchieveEntity;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class DYZZAchieveActivity extends ActivityBase implements AchieveProvider {
    private static final Logger logger = LoggerFactory.getLogger("Server");

    public DYZZAchieveActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.DYZZ_ACHIEVE;
    }
    
    @Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        //创建活动实例
        DYZZAchieveActivity activity = new DYZZAchieveActivity(config.getActivityId(), activityEntity);
        //注册当前活动到成就系统
        AchieveContext.registeProvider(activity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        //根据玩家id和活动期数去数据库里取数据
        List<DYZZAchieveEntity> queryList = HawkDBManager.getInstance()
                .query("from DYZZAchieveEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
            DYZZAchieveEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    public void onOpen() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for (String playerId : onlinePlayerIds) {
            callBack(playerId, GameConst.MsgId.DYZZ_ACHIEVE_INIT, ()-> {
                initAchieve(playerId);
            });
        }
    }

    @Override
    public void onPlayerLogin(String playerId) {
        String val = getDataGeter().getDYZZBattleInfo(playerId);
        if(HawkOSOperator.isEmptyString(val)){
            return;
        }
        logger.info("DYZZAchieveActivity check data start, playerId:{}",playerId);
        logger.info("DYZZAchieveActivity data start, data:{}",val);
        getDataGeter().delDYZZBattleInfo(playerId);
        JSONObject obj = JSON.parseObject(val);
        boolean win = obj.getBooleanValue("win");
        boolean mvp = obj.getBooleanValue("mvp");
        int kda = obj.getIntValue("kda");
        int baseHp = obj.getIntValue("baseHp");
        int seasonAddScore = obj.getIntValue("seasonAddScore");
        boolean isSeason = obj.getBooleanValue("isSeason");
        ActivityManager.getInstance().postEvent(new DYZZScoreEvent(playerId, kda, HawkTime.getMillisecond(), isSeason));
        int lessTen = 10;
        int equalOne = 1;
        int holyShit =17;
        DYZZAchieveKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZAchieveKVCfg.class);
        if(cfg != null){
            lessTen = cfg.getAchieveLessTen();
            equalOne = cfg.getAchieveEqualOne();
            holyShit = cfg.getAchieveHolyShit();
        }
        if(win){
            ActivityManager.getInstance().postEvent(new DYZZWinEvent(playerId, true),true);
            if(mvp){
                ActivityManager.getInstance().postEvent(new DYZZWinBestEvent(playerId),true);
            }
            if(baseHp < lessTen){
                ActivityManager.getInstance().postEvent(new DYZZWinWithBaseLessTenEvent(playerId),true);
            }
            if(baseHp == equalOne){
                ActivityManager.getInstance().postEvent(new DYZZWinWithBaseEqualOneEvent(playerId),true);
            }
        }else {
            ActivityManager.getInstance().postEvent(new DYZZWinEvent(playerId, false),true);
            if(mvp){
                ActivityManager.getInstance().postEvent(new DYZZLostBestEvent(playerId),true);
            }
        }
        if(kda >= holyShit){
            ActivityManager.getInstance().postEvent(new DYZZHolyShitEvent(playerId),true);
        }
        logger.info("DYZZAchieveActivity check data end, playerId:{}",playerId);
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        DYZZAchieveEntity entity = new DYZZAchieveEntity(playerId, termId);
        return entity;
    }

    @Override
    public boolean isProviderActive(String playerId) {
        return isOpening(playerId);
    }

    @Override
    public boolean isProviderNeedSync(String playerId) {
        return isShow(playerId);
    }

    @Override
    public Optional<AchieveItems> getAchieveItems(String playerId) {
        //获得玩家活动数据
        Optional<DYZZAchieveEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        //获得玩家活动数据
        DYZZAchieveEntity entity = opEntity.get();
        //如果成就数据为空返回空数据
        if (entity.getItemList().isEmpty()) {
            this.initAchieve(playerId);
        }
        //返回当前成就数据
        AchieveItems items = new AchieveItems(entity.getItemList(), entity);
        return Optional.of(items);
    }

    /**初始化成就
     * @param playerId
     */
    private void initAchieve(String playerId) {
        Optional<DYZZAchieveEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return;
        }
        DYZZAchieveEntity entity = optional.get();
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        ConfigIterator<DYZZAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(DYZZAchieveCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        while (iterator.hasNext()) {
            DYZZAchieveCfg cfg = iterator.next();
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            list.add(item);
        }
        entity.setItemList(list);
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()));
    }

    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(DYZZAchieveCfg.class, achieveId);
        return config;
    }

    @Override
    public Action takeRewardAction() {
        return Action.DYZZ_ACHIEVE_REWARD;
    }

    @Override
    public Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
        getDataGeter().logDYZZAchieveReach(playerId, achieveItem.getAchieveId());
        return Result.success();
    }

    @Override
    public Result<?> onTakeReward(String playerId, int achieveId) {
        getDataGeter().logDYZZAchieveTake(playerId, achieveId);
        return Result.success();
    }
}

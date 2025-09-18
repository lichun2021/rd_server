package com.hawk.activity.type.impl.jijiaSkin;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.DiamondRechargeEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.jijiaSkin.cfg.JijiaSkinActivityAchieveCfg;
import com.hawk.activity.type.impl.jijiaSkin.cfg.JijiaSkinActivityKVCfg;
import com.hawk.activity.type.impl.jijiaSkin.entity.JijiaSkinEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JijiaSkinActivity extends ActivityBase implements AchieveProvider {
    public final Logger logger = LoggerFactory.getLogger("Server");

    public JijiaSkinActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.JIJIA_SKIN;
    }

    @Override
    public int providerActivityId() {
        return this.getActivityType().intValue();
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        JijiaSkinActivity activity = new JijiaSkinActivity(config.getActivityId(), activityEntity);
        // 加入成就管理器
        AchieveContext.registeProvider(activity);
        return activity;
    }

    @Override
    public void onPlayerLogin(String playerId) {
        sync(playerId);
    }

    /** 跨天事件
     *
     * @param event */
    @Subscribe
    public void onContinueLogin(ContinueLoginEvent event) {
        if (!isOpening(event.getPlayerId())) {
            return;
        }
        if (!event.isCrossDay()) {
            return;
        }
        String playerId = event.getPlayerId();
        Optional<JijiaSkinEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
        if (!opPlayerDataEntity.isPresent()) {
            return;
        }
        JijiaSkinEntity entity = opPlayerDataEntity.get();
        if (entity.hasFinally()) {
            return;
        }
        entity.setRefreshTimes(0);
        sync(playerId);
    }

    public void sync(String playerId) {
        Optional<JijiaSkinEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        JijiaSkinEntity entity = opEntity.get();
        int openCount = (int) entity.getItemsList().stream().filter(i -> i > 0).count();
        JijiaSkinActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(JijiaSkinActivityKVCfg.class);
        Activity.PBJijiaSkinInfo.Builder resp = Activity.PBJijiaSkinInfo.newBuilder();
        resp.setPool(entity.getPool());
        resp.addAllRewardId(entity.getItemsList());
        resp.setRefreshTimes(entity.getRefreshTimes());
        resp.setMaxRefresh(kvCfg.getMaxRefresh());
        if (openCount < 9) {
            resp.setOpenCost(kvCfg.getTreasureCost(openCount));
        }
        if (entity.getRefreshTimes() < kvCfg.getMaxRefresh()) {
            resp.setRefreshCost(kvCfg.getRefreshCost(entity.getRefreshTimes()));
        }
        resp.setHasFinally(entity.hasFinally());
        pushToPlayer(playerId, HP.code2.JIJIA_SKIN_INFO_S_VALUE, resp);

    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<JijiaSkinEntity> queryList = HawkDBManager.getInstance()
                .query("from JijiaSkinEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
            JijiaSkinEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        JijiaSkinEntity entity = new JijiaSkinEntity(playerId, termId);
        entity.resetItems();
        return entity;
    }

    /**
     * 充值事件，活动期间充值，1元给一个物品
     * @param event
     */
    @Subscribe
    public void onEvent(DiamondRechargeEvent event) {
        String playerId = event.getPlayerId();
        if (!isOpening(playerId)) {
            return;
        }
        Optional<JijiaSkinEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        JijiaSkinActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(JijiaSkinActivityKVCfg.class);
        if (cfg == null) {
            return;
        }
        Reward.RewardItem.Builder cfgReward = cfg.getGetItemList();
        int itemId = cfgReward.getItemId();
        int num = event.getDiamondNum() / 10;
        long itemNum = cfgReward.getItemCount() * num;
        Reward.RewardItem.Builder reward = Reward.RewardItem.newBuilder();
        reward.setItemId(itemId);
        reward.setItemCount(itemNum);
        reward.setItemType(cfgReward.getItemType());
        List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
        rewardList.add(reward);
        // 邮件发送奖励
        Object[] content;
        content = new Object[1];
        content[0] = getActivityCfg().getActivityName();
        Object[] title = new Object[0];
        Object[] subTitle = new Object[0];
        //发奖
        this.getDataGeter().takeReward(playerId, rewardList, 1,  Action.JIJIA_SKIN_RECHARGE, false, Reward.RewardOrginType.JIJIA_SKIN_RECHARGE_REWARD);
        //发邮件
        sendMailToPlayer(playerId, MailConst.MailId.JIJIA_SKIN_RECHARGE, title, subTitle, content, rewardList, true);
        logger.info("JijiaSkinActivity sendMail addItems from DiamondRechargeEvent ItemId:{}, num:{}", itemId, itemNum);
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
    }

    @Override
    public void onTick() {
    }

    @Override
    public void onPlayerMigrate(String playerId) {

    }

    @Override
    public void onImmigrateInPlayer(String playerId) {

    }

    @Override
    public boolean isProviderActive(String playerId) {
        return isOpening(playerId);
    }

    @Override
    public boolean isProviderNeedSync(String playerId) {
        return !isHidden(playerId);
    }

    @Override
    public Optional<AchieveItems> getAchieveItems(String playerId) {
        Optional<JijiaSkinEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        JijiaSkinEntity entity = opEntity.get();
        if(entity.getItemList().isEmpty()){
            initAchieveInfo(playerId);
        }
        AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
        return Optional.of(achieveItems);
    }

    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        return HawkConfigManager.getInstance().getConfigByKey(JijiaSkinActivityAchieveCfg.class, achieveId);
    }

    @Override
    public Action takeRewardAction() {
        return Action.JIJIA_SKIN_ACHIEVE_AWARD;
    }

    @Override
    public void onOpen() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for (String playerId : onlinePlayerIds) {
            callBack(playerId, GameConst.MsgId.ON_JIJIA_SKIN_INIT, ()-> {
                initAchieveInfo(playerId);
                sync(playerId);
            });
        }
    }

    /**
     * 初始化成就信息
     */
    private void initAchieveInfo(String playerId) {
        Optional<JijiaSkinEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        JijiaSkinEntity entity = opEntity.get();
        // 成就已初始化
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        // 初始添加成就项
        ConfigIterator<JijiaSkinActivityAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(JijiaSkinActivityAchieveCfg.class);
        while (configIterator.hasNext()) {
            JijiaSkinActivityAchieveCfg next = configIterator.next();
            AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
            entity.addItem(item);
        }

        // 初始化成就数据
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
    }
}

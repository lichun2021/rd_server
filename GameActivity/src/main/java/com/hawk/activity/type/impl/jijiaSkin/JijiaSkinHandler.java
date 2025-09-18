package com.hawk.activity.type.impl.jijiaSkin;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.jijiaSkin.cfg.JijiaSkinActivityKVCfg;
import com.hawk.activity.type.impl.jijiaSkin.cfg.JijiaSkinActivityRewardCfg;
import com.hawk.activity.type.impl.jijiaSkin.entity.JijiaSkinEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;
import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JijiaSkinHandler extends ActivityProtocolHandler {

    /**
     * 同步
     */
    @ProtocolHandler(code = HP.code2.JIJIA_SKIN_INFO_C_VALUE)
    public boolean onReqInfo(HawkProtocol protocol, String playerId) {
        JijiaSkinActivity activity = getActivity(ActivityType.JIJIA_SKIN);
        activity.sync(playerId);
        return true;
    }

    /**
     * 翻
     */
    @ProtocolHandler(code = HP.code2.JIJIA_SKIN_OPEN_VALUE)
    public boolean onOpen(HawkProtocol protocol, String playerId) {
        Activity.PBJijiaSkinOpen req = protocol.parseProtocol(Activity.PBJijiaSkinOpen.getDefaultInstance());
        int index = req.getIndex();
        if (index < 0 || index > 8) {
            return false;
        }
        JijiaSkinActivity activity = getActivity(ActivityType.JIJIA_SKIN);
        Optional<JijiaSkinEntity> opEntity = activity.getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return false;
        }
        activity.sync(playerId);
        JijiaSkinEntity entity = opEntity.get();
        List<Integer> itemList = entity.getItemsList();
        if (itemList.get(index) > 0) {
            return false;
        }

        JijiaSkinActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(JijiaSkinActivityKVCfg.class);
        int openCount = 0; // 已翻
        int multipleCount = 0; // 已翻倍数道具数
        for (Integer i : itemList) {
            if (i.intValue() == 0) {
                continue;
            }
            openCount++;
            JijiaSkinActivityRewardCfg rcfg = HawkConfigManager.getInstance().getConfigByKey(JijiaSkinActivityRewardCfg.class, i);
            Reward.RewardItem.Builder reward = RewardHelper.toRewardItem(rcfg.getReward());
            if (kvCfg.isMultipleItem(reward.getItemId())) {
                multipleCount++;
            }
        }
        if (openCount == 0 && entity.getMultiple() > 0) {
            entity.setMultiple(0);
        }
        boolean is4_0 = openCount == 4 && multipleCount == 0;
        boolean is8_1 = openCount == 8 && multipleCount == 1;

        ConfigIterator<JijiaSkinActivityRewardCfg> it = HawkConfigManager.getInstance().getConfigIterator(JijiaSkinActivityRewardCfg.class);
        // 剩余可抽宝藏
        List<JijiaSkinActivityRewardCfg> rlist = new ArrayList<>();
        List<JijiaSkinActivityRewardCfg> allLeft = new ArrayList<>();
        for (JijiaSkinActivityRewardCfg cfg : it) {
            if(cfg.getPool() != entity.getPool()){
                continue;
            }
            if (itemList.contains(cfg.getId())) {
                continue;
            }
            allLeft.add(cfg);
            Reward.RewardItem.Builder reward = RewardHelper.toRewardItem(cfg.getReward());
            if (is4_0 || is8_1) {
                if (!kvCfg.isMultipleItem(reward.getItemId())) {
                    continue;
                }
            }
            if (entity.getMultiple() > 0) {
                if (kvCfg.isMultipleItem(reward.getItemId())) {
                    continue;
                }
            }
            rlist.add(cfg);
        }

        if(rlist.isEmpty()){
            rlist.addAll(allLeft);
        }

        JijiaSkinActivityRewardCfg cfg = HawkRand.randomWeightObject(rlist);

        // 扣费
        List<Reward.RewardItem.Builder> openCost = RewardHelper.toRewardItemList(kvCfg.getTreasureCost(openCount));

        int extryRewardCount = 0;
        if (openCost.size() > 0) {
            extryRewardCount = (int)openCost.get(0).getItemCount();
        }
        final int extryRewardFinalC = extryRewardCount;

        boolean consumeResult = activity.getDataGeter().consumeItems(playerId, openCost, protocol.getType(), Action.JIJIA_SKIN_OPEN);
        if (consumeResult == false) {
            return false;
        }
        itemList.set(index, cfg.getId());

//		// 必得奖励
        if (StringUtils.isNotEmpty(kvCfg.getExtReward())) {
            List<Reward.RewardItem.Builder> result = RewardHelper.toRewardItemList(kvCfg.getExtReward());
            result.forEach(ite -> ite.setItemCount(ite.getItemCount() * extryRewardFinalC));
            ActivityReward reward = new ActivityReward(result, Action.JIJIA_SKIN_OPEN);
            reward.setAlert(false);
            reward.setOrginType(Reward.RewardOrginType.JIJIA_SKIN_REWARD, activity.getActivityId());
            activity.postReward(playerId, reward);
        }

        // 如果是最终奖励
        if (cfg.isFinallyReward()) {
            entity.setHasFinally(1);
        }

        Reward.RewardItem.Builder reward = RewardHelper.toRewardItem(cfg.getReward());
        if (entity.getMultiple() > 0) {
            reward.setItemCount(reward.getItemCount() * entity.getMultiple());
            entity.setMultiple(0);
        }
        entity.notifyUpdate();
        List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
        if (!kvCfg.isMultipleItem(reward.getItemId())) {
            rewardList.add(reward);
        }else{
            entity.setMultiple((int) reward.getItemCount());
        }
        ActivityReward rewards = new ActivityReward(rewardList, Action.JIJIA_SKIN_OPEN);
        rewards.setAlert(true);
        if(cfg.isFinallyReward()){
            rewards.setOrginType(Reward.RewardOrginType.JIJIA_SKIN_REWARD, activity.getActivityId());
        }else {
            rewards.setOrginType(Reward.RewardOrginType.ACTIVITY_REWARD, activity.getActivityId());
        }
        activity.postReward(playerId, rewards);
        activity.sync(playerId);
        responseSuccess(playerId, protocol.getType());

        logger.info("JIJIA_SKIN_OPEN playerId={} index={} record={} reward={}", playerId, index, rewardList, RewardHelper.toItemString(reward.build()));
        return true;
    }

    /**
     * 刷新
     */
    @ProtocolHandler(code = HP.code2.JIJIA_SKIN_REFRESH_C_VALUE)
    public boolean onRefresh(HawkProtocol protocol, String playerId) {
        JijiaSkinActivity activity = getActivity(ActivityType.JIJIA_SKIN);
        Optional<JijiaSkinEntity> opEntity = activity.getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return false;
        }

        JijiaSkinActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(JijiaSkinActivityKVCfg.class);
        JijiaSkinEntity entity = opEntity.get();
        if (entity.getRefreshTimes() >= kvCfg.getMaxRefresh()) {
            return false;
        }

        if (entity.hasFinally()) {
            sendErrorAndBreak(playerId, protocol.getType(), Status.Error.JIJIA_SKIN_ACTI_HAS_FINALLY_VALUE);
            return false;
        }

        String costStr = kvCfg.getRefreshCost(entity.getRefreshTimes());
        List<Reward.RewardItem.Builder> cost = RewardHelper.toRewardItemList(costStr);
        // 拥有道具数
        boolean consumeResult = activity.getDataGeter().consumeItemsIsGold(playerId, cost, protocol.getType(), Action.JIJIA_SKIN_REFRESH);
        if (consumeResult == false) {
            return false;
        }

        entity.resetItems();
        entity.setMultiple(0);
        entity.setRefreshTimes(entity.getRefreshTimes() + 1);


        Activity.PBJijiaSkinRefreshResp.Builder resp = Activity.PBJijiaSkinRefreshResp.newBuilder();
        resp.setPool(entity.getPool());
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.JIJIA_SKIN_REFRESH_S, resp));
        activity.sync(playerId);

        return true;
    }
}

package com.hawk.activity.type.impl.directGift;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.BuyDirectGiftEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.directGift.cfg.DirectGiftCfg;
import com.hawk.activity.type.impl.directGift.entity.DirectGiftEntity;
import com.hawk.game.protocol.Activity.DirectGiftInfo;
import com.hawk.game.protocol.Activity.DirectGiftItem;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.log.Action;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DirectGiftActivity extends ActivityBase {

    public DirectGiftActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.DIRECT_GIFT_ACTIVITY;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        DirectGiftActivity activity = new DirectGiftActivity(config.getActivityId(), activityEntity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<DirectGiftEntity> queryList = HawkDBManager.getInstance()
                .query("from DirectGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
            DirectGiftEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        DirectGiftEntity entity = new DirectGiftEntity(playerId, termId);
        return entity;
    }


    @Override
    public void syncActivityDataInfo(String playerId) {

        Optional<DirectGiftEntity> optional = getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return;
        }
        DirectGiftEntity entity = optional.get();
        DirectGiftInfo.Builder builder = DirectGiftInfo.newBuilder();
        for (Map.Entry<Integer, Integer> entry : entity.getBuyGiftTimesMap().entrySet()) {
            DirectGiftItem.Builder itemBuilder = DirectGiftItem.newBuilder();
            itemBuilder.setId(entry.getKey());
            itemBuilder.setTimes(entry.getValue());
            builder.addItems(itemBuilder);
        }
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.DIRECT_GIFT_INFO_SYNC, builder));
    }

    @Subscribe
    public void buyGift(BuyDirectGiftEvent event) {
        String goodsId = event.getGoodsId();
        DirectGiftCfg cfg = DirectGiftCfg.getConfigBuyGoodsId(goodsId);
        if (cfg == null) {
            return;
        }
        Optional<DirectGiftEntity> optional = getPlayerDataEntity(event.getPlayerId());
        if (!optional.isPresent()) {
            return;
        }

        DirectGiftEntity entity = optional.get();
        entity.addBuyTimes(cfg.getId(), event.getTimes());
        if (event.isRewardDeliver()) {
        	this.getDataGeter().takeReward(event.getPlayerId(), cfg.getRewardList(), Action.DIRECT_GIFT_REWARD, true);
        	// 邮件发送奖励
        	Object[] content = new Object[0];
        	Object[] title = new Object[0];
        	Object[] subTitle = new Object[0];
        	//发邮件
        	sendMailToPlayer(event.getPlayerId(), MailId.DIRECT_GIFT_OUTTER_BUY, title, subTitle, content, cfg.getRewardList(), true);
        }
        syncActivityDataInfo(event.getPlayerId());
    }

    /**
     * 检查购买上限
     *
     * @param playerId
     * @param goodsId
     * @return
     */
    public boolean buyGiftCheck(String playerId, String goodsId) {
       return buyGiftCheck(playerId, goodsId, 1);
    }
    
    /**
     * 检查购买上限
     * @param playerId
     * @param goodsId
     * @param times
     * @return
     */
    public boolean buyGiftCheck(String playerId, String goodsId, int times) {
        Optional<DirectGiftEntity> optional = getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return false;
        }
        DirectGiftEntity entity = optional.get();
        DirectGiftCfg cfg = DirectGiftCfg.getConfigBuyGoodsId(goodsId);
        int buyTimes = entity.getBuyTimes(cfg.getId());
        int limit = cfg.getLimit();
        if (buyTimes + times > limit) {
            return false;
        }
        return true;
    }

    /**
     * 购买回滚
     *
     * @param goodsId
     */
    public void rollback(String playerId, String goodsId) {
        Optional<DirectGiftEntity> optional = getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            HawkLog.errPrintln("DirectGiftActivity rollback failed, playerId: {}, goodsId: {}", playerId, goodsId);
            return;
        }
        DirectGiftEntity entity = optional.get();
        DirectGiftCfg cfg = DirectGiftCfg.getConfigBuyGoodsId(goodsId);
        int buyTimesOld = entity.getBuyTimes(cfg.getId());
        entity.decBuyTimes(cfg.getId());
        int buyTimesNew = entity.getBuyTimes(cfg.getId());
		syncActivityDataInfo(playerId);
        HawkLog.errPrintln("DirectGiftActivity rollback success, playerId: {}, goodsId: {}, buyTimesOld: {}, buyTimesNew: {}", playerId, goodsId, buyTimesOld, buyTimesNew);
    }
}

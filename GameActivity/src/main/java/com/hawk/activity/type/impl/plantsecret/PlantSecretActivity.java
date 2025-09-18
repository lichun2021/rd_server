package com.hawk.activity.type.impl.plantsecret;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.plantsecret.cfg.PlantSecretBoxCfg;
import com.hawk.activity.type.impl.plantsecret.cfg.PlantSecretDailyReward;
import com.hawk.activity.type.impl.plantsecret.cfg.PlantSecretKVCfg;
import com.hawk.activity.type.impl.plantsecret.entity.PlantSecretEntity;
import com.hawk.game.protocol.Activity.PlantSecretActivityInfo;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Status.PlantSecretErr;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import java.util.*;
import java.util.Map.Entry;

/**
 * 泰能机密活动
 *
 * @author lating
 */
public class PlantSecretActivity extends ActivityBase {
    /**
     * 构造
     *
     * @param activityId     活动ID
     * @param activityEntity 活动数据库对象
     */
    public PlantSecretActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.PLANT_SECRET_ACTIVITY;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        PlantSecretActivity activity = new PlantSecretActivity(config.getActivityId(), activityEntity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<PlantSecretEntity> queryList = HawkDBManager.getInstance()
                .query("from PlantSecretEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
            PlantSecretEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        PlantSecretEntity entity = new PlantSecretEntity(playerId, termId);
        return entity;
    }

    @Override
    public void onOpen() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for (String playerId : onlinePlayerIds) {
            callBack(playerId, MsgId.PLANT_SECRET_INIT, () -> {
                resetPlayerOpenBoxData(playerId);
                syncActivityDataInfo(playerId);
            });
        }
    }

    @Subscribe
    public void onEvent(ContinueLoginEvent event){
        if (!isOpening(event.getPlayerId())) {
            return;
        }

        if(!event.isCrossDay()){
            return;
        }

        //记录一下清空之前的购买记录
        logger.info("PlantSecretActivity cross day syncActivityInfo: {}", event.getPlayerId());

        Optional<PlantSecretEntity> opDataEntity = getPlayerDataEntity(event.getPlayerId());
        if (!opDataEntity.isPresent()) {
            return;
        }

        PlantSecretEntity entity = opDataEntity.get();
        if(null == entity){
            return;
        }
        onDayChange(entity);
        syncActivityInfo(event.getPlayerId(), entity);
    }

    private void resetPlayerOpenBoxData(String playerId) {
        Optional<PlantSecretEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        PlantSecretEntity entity = opDataEntity.get();
        if(entity.getSecret() == 0){
            resetOpenBoxData(entity);
        }
        return;
    }

    /**
     * 给客户端同步玩家活动数据
     *
     * @param playerId
     */
    @Override
    public void syncActivityDataInfo(String playerId) {
        Optional<PlantSecretEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }

        syncActivityInfo(playerId, opDataEntity.get());
    }

    /**
     * 活动结束事件的回调
     */
    @Override
    public void onEnd() {
        HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
            @Override
            public Object run() {
                //grantItem();
                return null;
            }
        });
    }

    /**
     * 兑换所有玩家剩余活动道具，将兑换后的物品通过邮件发送给玩家
     */
    private void grantItem() {
        PlantSecretKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantSecretKVCfg.class);
        int termId = getTimeControl().getActivityTermId(HawkApp.getInstance().getCurrentTime() - 5 * HawkTime.MINUTE_MILLI_SECONDS);
        String redisKey = getRedisKey(termId);
        Map<String, String> map = ActivityLocalRedis.getInstance().hgetAll(redisKey);
        ActivityLocalRedis.getInstance().del(redisKey);
        for (Entry<String, String> entry : map.entrySet()) {
            try {
                int count = Integer.parseInt(entry.getValue());
                if (count <= 0) {
                    continue;
                }

                String playerId = entry.getKey();
                
                RewardItem.Builder consumeBuilder = RewardHelper.toRewardItem(cfg.getPropid());
                int itemNum = getDataGeter().getItemNum(playerId, cfg.getConsume().get(0).getItemId());
                consumeBuilder.setItemCount(itemNum);
                List<RewardItem.Builder> consume = ImmutableList.of(consumeBuilder);
                this.getDataGeter().cost(playerId, consume, 1, Action.PLANT_SECRET_TICKET_CONSUME, false);
                
                RewardItem.Builder builder = RewardHelper.toRewardItem(cfg.getItems());
                builder.setItemCount(count);
                List<RewardItem.Builder> rewardItems = ImmutableList.of(builder);

                this.getDataGeter().sendMail(playerId, MailId.PLANT_SECRET_END_REWARD,
                        new Object[]{this.getActivityCfg().getActivityName()},
                        new Object[]{this.getActivityCfg().getActivityName()},
                        null,
                        rewardItems, false);
            } catch (Exception e) {
                HawkException.catchException(e);
            }
        }
    }

    /**
     * 玩家登陆事件
     *
     * @param playerId
     */
    @Override
    public void onPlayerLogin(String playerId) {
        Optional<PlantSecretEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        resetPlayerOpenBoxData(playerId);
        updateDayTime(opDataEntity.get());
        //同步活动数据给客户端
        syncActivityInfo(playerId, opDataEntity.get());
        //在活动redis里刷新一下玩家剩余活动道具数量
        refreshItemNum(playerId);
    }

    /**
     * 玩家下线的事件回调
     *
     * @param playerId
     */
    @Override
    public void onPlayerLogout(String playerId) {
        Optional<PlantSecretEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        //在活动redis里刷新一下玩家剩余活动道具数量
        refreshItemNum(playerId);
    }

    /**
     * 刷新翻牌道具的数量到活动redis
     *
     * @param playerId
     */
    private void refreshItemNum(String playerId) {
        try {
        	int termId = getTimeControl().getActivityTermId(HawkApp.getInstance().getCurrentTime());
            PlantSecretKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantSecretKVCfg.class);
            int itemNum = getDataGeter().getItemNum(playerId, cfg.getConsume().get(0).getItemId());
            if (itemNum == 0) {
                ActivityLocalRedis.getInstance().hDel(getRedisKey(termId), playerId);
            } else {
                ActivityLocalRedis.getInstance().hset(getRedisKey(termId), playerId, String.valueOf(itemNum));
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 活动redis的key
     *
     * @return
     */
    private String getRedisKey(int termId) {
        return "plant_secret:" + this.getDataGeter().getServerId() + ":" + termId;
    }

    /**
     * 翻牌
     *
     * @param playerId
     * @param cardId
     */
    public Result<?> openCard(String playerId, int cardId) {
        //获取玩家数据库中的泰能机密活动数据
        Optional<PlantSecretEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //取数据对象
        PlantSecretEntity entity = opDataEntity.get();
        //已经翻过，不能再翻重复的牌
        if (entity.getOpenedCardList().contains(cardId)) {
            return Result.fail(Status.Error.PLANT_SECRET_OPEN_CARD_REPEAT_VALUE);
        }
        //翻牌ID合法范围检查
        if (cardId <= 0 || cardId > 24) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        //取互动表
        PlantSecretKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantSecretKVCfg.class);
        //从表格取翻牌消耗的道具
        List<RewardItem.Builder> consume = cfg.getConsume();
        //消耗一个道具，如果失败给客户端返回错误码，不继续处理
        boolean cost = this.getDataGeter().cost(playerId, consume, 1, Action.PLANT_SECRET_TICKET_CONSUME, true);
        if (!cost) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        //翻牌成功，将卡牌ID计入玩家数据库对象
        entity.getOpenedCardList().add(cardId);
        entity.notifyUpdate();
        //给客户端同步
        syncActivityInfo(playerId, entity);
        //向活动redis刷新翻牌道具的数量
        refreshItemNum(playerId);
        // 打点
        int openCardCount = entity.getOpenedCardList().size();
        getDataGeter().logPlantSecret(playerId, entity.getTermId(), 1, entity.getOpenBoxCount(), openCardCount, entity.getBuyItemCount(), 0, false, 0, 0);

        return Result.success();
    }

    /**
     * 开启机密宝箱
     *
     * @param playerId
     * @param secretNum
     */
    public Result<?> openBox(String playerId, int secretNum) {
        //取玩家数据库数据
        Optional<PlantSecretEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        PlantSecretEntity entity = opDataEntity.get();

        if(dailyOpenBoxLimit(entity)){
            return Result.fail(PlantSecretErr.PLANT_SECRET_OPENBOX_EXCEED_LIMIT_VALUE);
        }

        //当前箱子开启了多少次
        int openBoxTimes = entity.getOpenBoxTimes();
        //已翻出的牌的列表
        int openCardCount = entity.getOpenedCardList().size();
        //牌下面盖着的密码
        int serverNum = entity.getSecret();
        //客户端发来的数字和牌下面盖着的密码不一样，没猜中
        if (entity.getSecret() != secretNum) {
            //同步给客户端
            openBoxFail(entity);
            getDataGeter().logPlantSecret(playerId, entity.getTermId(), 2, entity.getOpenBoxCount(), openCardCount,
                    entity.getBuyItemCount(), openBoxTimes + 1, false, serverNum, secretNum);
            return Result.success();
        }
        //成功开箱次数+1
        entity.setOpenBoxCount(entity.getOpenBoxCount() + 1);
        //当日开箱次数+1
        entity.setDailyOpenBox(entity.getDailyOpenBox() + 1);
        ConfigIterator<PlantSecretBoxCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(PlantSecretBoxCfg.class);
        PlantSecretBoxCfg boxCfg = null;
        //遍历配置表格，找到等级经验小于，并最接近玩家当前开箱次数的配置项
        while (iterator.hasNext()) {
            PlantSecretBoxCfg cfg = iterator.next();
            if (boxCfg == null || cfg.getUpgrade() <= entity.getOpenBoxCount()) {
                boxCfg = cfg;
            }
        }

        List<RewardItem.Builder> rewardItemBuilder = new ArrayList<RewardItem.Builder>();
        //将当前配置项里的所有奖励
        rewardItemBuilder.addAll(boxCfg.getAwardList());
        rewardItemBuilder.addAll(boxCfg.getSpecialAwardList());
        //发奖
        this.getDataGeter().takeReward(playerId, rewardItemBuilder, 1,
                Action.PLANT_SECRET_BOX_REWARD, true, RewardOrginType.PLANT_SECRET_BOX);
        //重置开箱数据库数据
        resetOpenBoxData(entity);
        //给客户端同步服务器数据
        syncActivityInfo(playerId, entity);

        // 打点
        getDataGeter().logPlantSecret(playerId, entity.getTermId(), 2, entity.getOpenBoxCount(), openCardCount,
                entity.getBuyItemCount(), openBoxTimes + 1, true, serverNum, secretNum);

        return Result.success();
    }

    private boolean dailyOpenBoxLimit(PlantSecretEntity entity) {
        updateDayTime(entity);

        PlantSecretKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantSecretKVCfg.class);
        //达到每日开箱次数上限，返回
        if(entity.getDailyOpenBox() >= cfg.getMaxboxs()){
            return true;
        }
        //没达到上限
        return false;
    }

    /**
     * 购买翻牌道具
     *
     * @param playerId
     * @param count
     * @return
     */
    public Result<?> buyItem(String playerId, int count) {
        //取得玩法的玩家数据
        Optional<PlantSecretEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //购买道具数量合法性检查
        if (count <= 0) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }

        PlantSecretEntity entity = opDataEntity.get();
        PlantSecretKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantSecretKVCfg.class);
        //购买道具数量限制判断
        if (entity.getBuyItemCount() + count > cfg.getTimes()) {
            return Result.fail(Status.Error.PLANT_SECRET_BUY_ITEM_LIMIT_VALUE);
        }
        //扣除道具购买的花费
        RewardItem.Builder builder = RewardHelper.toRewardItem(cfg.getPrice());
        List<RewardItem.Builder> consume = ImmutableList.of(builder);
        boolean cost = this.getDataGeter().cost(playerId, consume, count, Action.PLANT_SECRET_BUY_CONSUME, true);
        if (!cost) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        //花费扣除成功，发放购买的道具
        List<RewardItem.Builder> rewardItem = new ArrayList<RewardItem.Builder>();
        rewardItem.addAll(cfg.getConsume());
        this.getDataGeter().takeReward(playerId, rewardItem, count, Action.PLANT_SECRET_BUY_AWARD, true, RewardOrginType.ACTIVITY_REWARD);
        //记录最新的已购买数量
        entity.setBuyItemCount(entity.getBuyItemCount() + count);
        //给客户端同步数据
        syncActivityInfo(playerId, entity);
        //刷新翻牌道具的数量
        refreshItemNum(playerId);
        return Result.success();
    }

    /**
     * 开箱失败
     *
     * @param entity
     */
    @SuppressWarnings("deprecation")
    private void openBoxFail(PlantSecretEntity entity) {
        //增加开箱次数
        entity.setOpenBoxTimes(entity.getOpenBoxTimes() + 1);
        PlantSecretKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantSecretKVCfg.class);
        // 给客户端发送错误提示
        PlayerPushHelper.getInstance().sendError(entity.getPlayerId(), HP.code2.PLANT_SECRET_OPEN_BOX_REQ_VALUE, Status.Error.PLANT_SECRET_OPEN_BOX_FAILED_VALUE);
        //当前箱子的开箱次数超过单个宝箱可开启次数限制(试错次数)
        if (entity.getOpenBoxTimes() >= cfg.getOpentime()) {
            resetOpenBoxData(entity);
            //这里也要记一次开箱次数
            entity.setDailyOpenBox(entity.getDailyOpenBox() + 1);
        }
        //给客户端同步全部服务器数据
        syncActivityInfo(entity.getPlayerId(), entity);
    }

    /**
     * 重置数据库开箱数据
     *
     * @param entity
     */
    private void resetOpenBoxData(PlantSecretEntity entity) {
        //清空已翻出的牌
        entity.getOpenedCardList().clear();
        //清空开箱次数
        entity.setOpenBoxTimes(0);
        //从新初始化牌下面盖着的密码
        entity.setSecret(productSecret());
    }

    /**
     * 随机密码
     *
     * @return
     */
    private int productSecret() {
        int one = HawkRand.randInt(0, 9);
        int two = HawkRand.randInt(0, 9);
        int three = HawkRand.randInt(0, 9);
        int four = HawkRand.randInt(0, 9);
        return one * 1000 + two * 100 + three * 10 + four;
    }

    /**
     * 信息同步
     *
     * @param playerId
     */
    private void syncActivityInfo(String playerId, PlantSecretEntity entity) {
        PlantSecretActivityInfo.Builder builder = PlantSecretActivityInfo.newBuilder();
        builder.setBuyItemCount(entity.getBuyItemCount());
        builder.setOpenedBoxCount(entity.getOpenBoxCount());
        builder.setOpenBoxTimes(entity.getOpenBoxTimes());
        builder.setSecretNum(entity.getSecret());
        builder.addAllOpenedCard(entity.getOpenedCardList());
        builder.setLastShareTimeGuild(entity.getLastShareTimeGuild());
        builder.setLastShareTimeWorld(entity.getLastShareTimeWorld());
        builder.setShareGuildTimes(entity.getAllianceshare());
        builder.setShareWorldTimes(entity.getWorldshare());
        builder.setDailyOpenBox(entity.getDailyOpenBox());
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.PLANT_SECRET_ACTIVITY_INFO_SYNC, builder));
    }

    private void onDayChange(PlantSecretEntity entity){
        entity.setDailyOpenBox(0);
        entity.setAllianceshare(0);
        entity.setWorldshare(0);
    }

    private void updateDayTime(PlantSecretEntity entity){
        int dayTime = HawkTime.getYearDay();
        //第一次初始化
        if(entity.getDayTime() == 0){
            entity.setDayTime(dayTime);
        }
        //跨天了，清空每日开箱次数
        if(entity.getDayTime() != dayTime){
            onDayChange(entity);
            entity.setDayTime(dayTime);
        }
    }
    /**
     * @param chatTypeValue 聊天类型
     * @param chatMsg       聊天内容
     * @param playerId      玩家ID
     * @return
     */
    public Result<?> chatShare(int chatTypeValue, String chatMsg, String playerId) {
        if (chatTypeValue != Const.ChatType.CHAT_ALLIANCE.getNumber() &&
                chatTypeValue != Const.ChatType.CHAT_WORLD.getNumber()) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }

        Optional<PlantSecretEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }

        PlantSecretEntity entity = opDataEntity.get();
        boolean openCardArea1 = false, openCardArea2 = false, openCardArea3 = false, openCardArea4 = false;
        for (int openedCard : entity.getOpenedCardList()) {
        	if (openedCard >= 1 && openedCard <= 6) {
        		openCardArea1 = true;
        		continue;
        	}
        	
        	if (openedCard >= 7 && openedCard <= 12) {
        		openCardArea2 = true;
        		continue;
        	}
        	
        	if (openedCard >= 13 && openedCard <= 18) {
        		openCardArea3 = true;
        		continue;
        	}
        	
        	if (openedCard >= 19 && openedCard <= 24) {
        		openCardArea4 = true;
        	}
        }
        
        boolean openCardCond = openCardArea4 && openCardArea3 && openCardArea2 && openCardArea1;
        if (!openCardCond) {
            return Result.fail(PlantSecretErr.PLANT_SECRET_OPENCARD_COND_SHARE_LIMIT_VALUE);
        }
        
        if(dailyOpenBoxLimit(entity)){
            return Result.fail(PlantSecretErr.PLANT_SECRET_OPENBOX_EXCEED_SHARE_LIMIT_VALUE);
        }
        
        //刷新日期，更新每日限制
        updateDayTime(entity);

        PlantSecretKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantSecretKVCfg.class);

        long curTime = HawkTime.getMillisecond();
        ChatType chatType = Const.ChatType.CHAT_ALLIANCE;
        if (chatTypeValue == Const.ChatType.CHAT_ALLIANCE.getNumber()) {
            if(getDataGeter().getGuildId(playerId).isEmpty()){
                return  Result.fail(PlantSecretErr.PLANT_SECRET_SHARE_NO_GUILD_VALUE);
            }
            //先看次数是不是到上限
            int shareCount = entity.getAllianceshare();
            if (shareCount >= cfg.getAllianceshare()) {
                return Result.fail(PlantSecretErr.PLANT_SECRET_GUILD_SHARE_LIMIT_VALUE);
            }
            //初始化冷却时间
            if(entity.getLastShareTimeGuild() <= 0){
                entity.setLastShareTimeGuild(curTime);
            }else if (entity.getLastShareTimeGuild() + cfg.getAllianceshareCD() * 1000 > curTime) {
                return Result.fail(PlantSecretErr.PLANT_SECRET_SHARE_FREQUENTLY_VALUE);
            }
            chatType = Const.ChatType.CHAT_ALLIANCE;
            entity.setAllianceshare(shareCount + 1);
            entity.setLastShareTimeGuild(curTime);

        } else {
            //先看次数是不是到上限
            int shareCount = entity.getWorldshare();
            if (shareCount >= cfg.getWorldshare()) {
                return Result.fail(PlantSecretErr.PLANT_SECRET_WORLD_SHARE_LIMIT_VALUE);
            }
            //初始化冷却时间
            if(entity.getLastShareTimeWorld() <= 0){
                entity.setLastShareTimeWorld(curTime);
            }else if (entity.getLastShareTimeWorld() + cfg.getWorldshareCD() * 1000 > curTime) {
                return Result.fail(PlantSecretErr.PLANT_SECRET_SHARE_FREQUENTLY_VALUE);
            }
            chatType = ChatType.CHAT_WORLD;
            entity.setWorldshare(shareCount + 1);
            entity.setLastShareTimeWorld(curTime);
        }

        String guildId = this.getDataGeter().getGuildId(playerId);

        this.getDataGeter().addWorldBroadcastMsg(
                chatType, getDataGeter().getGuildId(playerId), Const.NoticeCfgId.PLANT_SECRET_CHAT_SHARE, playerId, chatMsg);

        syncActivityDataInfo(playerId);

        return Result.success();
    }
    
    /**
     * 每日任务活动获取奖励的接口
     * 
     * @param id
     * @return
     */
    public List<RewardItem.Builder> getDailyBoxExtRewards(int id){
		List<PlantSecretDailyReward> list = HawkConfigManager.getInstance().
				getConfigIterator(PlantSecretDailyReward.class).toList();
		for(PlantSecretDailyReward reward : list){
			if(reward.getDaily_reward() == id){
				List<RewardItem.Builder> items = RewardHelper.toRewardItemList(reward.getKey());
				return items;
			}
		}
		return null;
	}
    
}

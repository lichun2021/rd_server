package com.hawk.activity.type.impl.shareGlory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ActivityRewardsEvent;
import com.hawk.activity.event.impl.GuildQuiteEvent;
import com.hawk.activity.event.impl.JoinGuildEvent;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.shareGlory.cfg.ShareGloryKVCfg;
import com.hawk.activity.type.impl.shareGlory.cfg.ShareGloryKVCfg.DonateItemType;
import com.hawk.activity.type.impl.shareGlory.cfg.ShareGloryLevelCfg;
import com.hawk.activity.type.impl.shareGlory.cfg.ShareGloryTimeCfg;
import com.hawk.activity.type.impl.shareGlory.entity.ShareGloryEntity;
import com.hawk.game.protocol.Activity.PBShareGloryAllianceInfo;
import com.hawk.game.protocol.Activity.PBShareGloryDonateRank;
import com.hawk.game.protocol.Activity.PBShareGloryEnergyInfo;
import com.hawk.game.protocol.Activity.PBShareGloryItems;
import com.hawk.game.protocol.Activity.PBShareGloryPlayer;
import com.hawk.game.protocol.Activity.PBShareGloryPlayerInfo;
import com.hawk.game.protocol.Activity.PBShareGloryPlayerItems;
import com.hawk.game.protocol.Activity.PBShareGloryPlayerItems.Builder;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;

import redis.clients.jedis.Tuple;

/**
 * @author richard
 * @desc 荣耀同享活动
 * 通过玩家对工会捐献指定道具A、B提升两个能量柱等级，每当等级提升，所有成员可获得固定奖励和返还奖励
 * 监控策划指定的活动和礼包奖励，根据公式计算返还奖励
 * 捐献的A、B道具各有一个排行榜，排行榜是工会内部排行
 */
public class ShareGloryActivity extends ActivityBase {

    /**
     * 捐献排行榜缓存
     */
    private Map<String, PBShareGloryDonateRank.Builder> rankDataMap = 
    		new ConcurrentHashMap<String, PBShareGloryDonateRank.Builder>();
    /**
     * A能量redis key
     */
    private final String energyA = "energy_a";
    /**
     * B能量redis key
     */
    private final String energyB = "energy_b";


    public ShareGloryActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.SHARE_GLORY_ACTIVITY;
    }

    /**
     * 玩家登陆，这里需要处理下玩家不在线期间应该获取的能量柱升级奖励
     *
     * @param playerId
     */
    @Override
    public void onPlayerLogin(String playerId) {
    	//活动开启期间，给玩家推送下工会数据
        if(this.isOpening(playerId)) {
            onAllianceInfoReq(playerId, this.getActivityTermId());
        }

        //如果处在活动期间，及刷新下玩家工会信息
        savePlayerGuildId(playerId);

        String guildId = getGuildId(playerId);
        //玩家没有工会，不处理
        if (HawkOSOperator.isEmptyString(guildId)) {
            HawkLog.logPrintln("shareGlory onPlayerLogin HawkOSOperator.isEmptyString {}", playerId);
            return;
        }

        int activityTermId = this.getActivityTermId();

        //取玩家数据库对象
        ShareGloryEntity entity = getRealPlayerEntity(playerId, activityTermId);
        if(null == entity){
            return;
        }

        String guildDonatekey = getGuildDonatekey(playerId,entity.getTermId());
        if (HawkOSOperator.isEmptyString(guildDonatekey)) {
        	HawkLog.logPrintln("shareGlory error onPlayerLogin HawkOSOperator.isEmptyString(guildDonatekey)");
        	return;
        }
        
        //先处理A能量柱
        rewardOfflinePlayer(playerId, guildDonatekey, DonateItemType.typeA, energyA,entity);
        //再处理B能量柱
        rewardOfflinePlayer(playerId, guildDonatekey, DonateItemType.typeB, energyB,entity);
        //如果是在活动关闭时候的补发，需要立即更新数据库，不能异步
        if(this.getActivityTermId() == 0){
        	entity.notifyUpdate(false, 0);
        }
    }

    /**
     * 玩家捐献处理,对接客户端捐献协议
     *
     * @param playerId
     * @param itemId
     * @param number
     */
    public void onDonate(String playerId, int itemId, int number,int termId) {
        if(!this.isOpening(playerId)){
            return;
        }

        //不是待捐献的道具，不继续处理
        if (!isDonateItem(itemId)) {
            return;
        }

        Optional<ShareGloryEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        ShareGloryEntity entity = opDataEntity.get();

        //超出捐献上限
        if(isDonateExceededlimit(playerId, itemId, number, entity)){
            return;
        }
        //道具扣除
        if (!costItem(playerId, itemId, number)) {
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.SHARE_GLORY_DONATE_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
            return;
        }
        //消耗扣除成功，这里先记录捐献数量
        entity.addDonateCount(itemId, number);
        //发放捐献固定奖励
        donateFixReward(playerId, itemId, number);
        //处理工会能量柱等级
        onAllianceEnergy(playerId, itemId, number,termId);
    }

    private Boolean isDonateExceededlimit(String playerId, int itemId, int number, ShareGloryEntity entity){
        int donateCount = entity.getDonateCount(itemId);
        DonateItemType itemType = this.getDonateItemType(itemId);
        ShareGloryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ShareGloryKVCfg.class);
        int donateMax = itemType == DonateItemType.typeA ? cfg.getDonateAMax() : cfg.getDonateBMax();
        if(donateCount + number > donateMax){
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.SHARE_GLORY_DONATE_REQ_VALUE,
                    Status.Error.SHARE_GLORY_DONATE_EXCEED_THE_LIMIT_VALUE);
            return true;
        }
        return false;
    }

    /**
     * 捐献时的固定奖励，只要捐成功了就发的奖励
     * 每捐献一个拿一份奖励
     * @param playerId
     * @param itemId
     */
    private void donateFixReward(String playerId, int itemId, int count) {
        DonateItemType donateItemType = getDonateItemType(itemId);
        ShareGloryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ShareGloryKVCfg.class);
        int rewardId =  donateItemType == DonateItemType.typeA ? cfg.getItemAReward() : cfg.getItemBReward();
        List<RewardItem.Builder> rewardItemList = new ArrayList<>();

        for(int i=0; i<count; ++i){
            List<String> rewardList = this.getDataGeter().getAwardFromAwardCfg(rewardId);
            for (String rewardStr : rewardList) {
                List<RewardItem.Builder> rewardBuilders = RewardHelper.toRewardItemImmutableList(rewardStr);
                rewardItemList.addAll(rewardBuilders);
            }
        }

        this.getDataGeter().takeReward(playerId, rewardItemList,
                1, Action.SHARE_GLORY_DONATE_FIX_REWARD, true);
    }

    /**
     * 玩家获得指定奖励的事件处理函数
     *
     * @param event
     */
    @Subscribe
    public void onActivityRewardsEventHandler(ActivityRewardsEvent event) {
        if(!this.isOpening(event.getPlayerId())){
            return;
        }
        ShareGloryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ShareGloryKVCfg.class);
        String strNoRewardItem = cfg.getNoRewardPackItem();
        List<Integer> exclusionList = ImmutableList.copyOf(SerializeHelper.stringToList(
                Integer.class, strNoRewardItem, SerializeHelper.ATTRIBUTE_SPLIT));
        //在排除列表里，不记录
        if (exclusionList.contains(event.getItemId())) {
            return;
        }
        //如果是周年奖励活动，需要过滤特定ID，不在配置的监控范围里不处理
        if(event.getActivityId() == Action.CELEBRATION_SHOP_EXCHANGE_REWARD.intItemVal() &&
        !cfg.isId242Contain(event.getExchangeId())){
            return;
        }

        //处理工会能量柱
        String playerId = event.getPlayerId();
        if (!isOpening(event.getPlayerId())) {
            return;
        }
        //把玩家被监控的活动获得的指定奖励存库
        Optional<ShareGloryEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        ShareGloryEntity entity = opDataEntity.get();
        entity.addRewardActivity(event.getDonateItemType(), event.getItemId(), event.getNumber());
    }

    /**
     * 处理玩家请求活动中联盟数据
     *
     * @param playerId
     */
    public void onAllianceInfoReq(String playerId,int termId) {
        PlayerPushHelper.getInstance().pushToPlayer(
                playerId, HawkProtocol.valueOf(HP.code2.SHARE_GLORY_ALLIANCE_ENERGY_RESP_VALUE,
                        buildAllianceInfo(playerId, termId)));
    }

    /**
     * 处理玩家在本活动中的个人数据请求
     *
     * @param playerId
     */
    public void onPlayerInfoReq(String playerId,int termId) {
        PBShareGloryPlayerInfo.Builder playerBuilder = PBShareGloryPlayerInfo.newBuilder();

        PBShareGloryAllianceInfo.Builder alliancebuilder = buildAllianceInfo(playerId, termId);
        playerBuilder.setAllianceInfo(alliancebuilder);

        //把玩家被监控的活动获得的指定奖励存库
        Optional<ShareGloryEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        ShareGloryEntity entity = opDataEntity.get();
        //监控到的A能量道具列表
        Builder playerItemBuilderA = fillPlayerItemBuilder(entity.getRewardActivityMapA(), DonateItemType.typeA);
        playerBuilder.addMonitorItem(playerItemBuilderA);
        //监控到的B能量道具列表
        Builder playerItemBuilderB = fillPlayerItemBuilder(entity.getRewardActivityMapB(), DonateItemType.typeB);
        playerBuilder.addMonitorItem(playerItemBuilderB);
        //监控到的A能量道具已返还的列表
        Builder playerItemRewardBuilderA = fillPlayerItemBuilder(entity.getRewardInfoMapA(), DonateItemType.typeA);
        playerBuilder.addMonitorItemReward(playerItemRewardBuilderA);
        //监控到的B能量道具已返还的列表
        Builder playerItemRewardBuilderB = fillPlayerItemBuilder(entity.getRewardInfoMapB(), DonateItemType.typeB);
        playerBuilder.addMonitorItemReward(playerItemRewardBuilderB);
        //给玩家发协议
        PlayerPushHelper.getInstance().pushToPlayer(
                playerId, HawkProtocol.valueOf(HP.code2.SHARE_GLORY_PLAYER_INFO_RESP_VALUE,
                        playerBuilder));
    }

    /**
     * 联盟退出
     *
     * @param event
     */
    @Subscribe
    public void onGuildQuite(GuildQuiteEvent event) {
    }

    /**
     * 联盟加入
     * 这里需要将玩家A，B能量柱已经领取的奖励等级设置为新加入的联盟的A，B能量柱当前等级。
     * 如果从1级能量的工会离开，玩家已经领取过1级能量奖励，然后离开工会，加入了一个能量柱
     * 3级的工会，那么就把玩家能量柱已领取奖励等级设置为3，相当于玩家错过了2,3两级的奖励
     * 需要等新工会能量等级升到4的时候直接领取4级能量柱奖励。
     * <p>
     * 反过来，如玩家从3级能量柱的联盟离开，加入了1级能量柱的联盟，此时需要保留玩家的已领
     * 取等级，防止重复领取2、3级奖励
     *
     * @param event
     */
    @Subscribe
    public void onJoinGuild(JoinGuildEvent event) {
        //如果处在活动期间，及刷新下玩家工会信息
        savePlayerGuildId(event.getPlayerId());
        
        if(!this.isOpening(event.getPlayerId())){
            return;
        }
        
        // 重置下玩家公会的值
        Optional<ShareGloryEntity> opDataEntity = getPlayerDataEntity(event.getPlayerId());
        if (opDataEntity.isPresent()) {
        	ShareGloryEntity entity = opDataEntity.get();
        	
            String guildDonatekey = getGuildDonatekey(event.getPlayerId() ,entity.getTermId());
            
            ShareGloryLevelCfg levelA = getCurShareGloryLevelCfg(DonateItemType.typeA, getGuildEnergy(guildDonatekey, energyA));
            if (levelA != null) {
            	int beforeLevelA = getMaxLevel(entity.getRewardEnergyLevel(levelA.getType()));
            	if (levelA.getLevel() > beforeLevelA) {
            		entity.setRewardEnergyLevel(levelA.getLevel(), levelA.getType());
            	}
            }
            
            ShareGloryLevelCfg levelB = getCurShareGloryLevelCfg(DonateItemType.typeB, getGuildEnergy(guildDonatekey, energyB));
			if (levelB != null) {
				int beforeLevelB = getMaxLevel(entity.getRewardEnergyLevel(levelB.getType()));
				if (levelB.getLevel() > beforeLevelB) {
					entity.setRewardEnergyLevel(levelB.getLevel(), levelB.getType());
				}
			}
        }
        
        onAllianceInfoReq(event.getPlayerId(), this.getActivityTermId());
    }

    private PBShareGloryDonateRank.Builder getPBShareGloryDonateRankBuilder(String guildId){
    	if(HawkOSOperator.isEmptyString(guildId)){
    		return PBShareGloryDonateRank.newBuilder();
    	}
    	PBShareGloryDonateRank.Builder rankbuilder = this.rankDataMap.get(guildId);
    	long curTime = HawkTime.getMillisecond();

    	if(rankbuilder != null && rankbuilder.getRankTime() + HawkTime.MINUTE_MILLI_SECONDS * 5 > curTime){
    		return rankbuilder;
    	}
    	PBShareGloryDonateRank.Builder newRankbuilder = PBShareGloryDonateRank.newBuilder();
        fillDonateRankBuilder(guildId, newRankbuilder, DonateItemType.typeA);
        fillDonateRankBuilder(guildId, newRankbuilder, DonateItemType.typeB);
        newRankbuilder.setRankTime(curTime);
        this.rankDataMap.put(guildId, newRankbuilder);
        return newRankbuilder;
    }
    
    
    /**
     * 处理本活动捐献排行榜数据请求
     *
     * @param playerId
     */
    public void onShareGloryDonateRankReq(String playerId) {
        String guildId = getGuildId(playerId);
    	PBShareGloryDonateRank.Builder rankbuilder = getPBShareGloryDonateRankBuilder(guildId);
        PlayerPushHelper.getInstance().pushToPlayer(
                playerId, HawkProtocol.valueOf(HP.code2.SHARE_GLORY_DONATE_RANK_RESP_VALUE,
                        rankbuilder));
    }

    private Builder fillPlayerItemBuilder(Map<Integer, Integer> rewardMap, DonateItemType donateItemType) {
        Builder playerItemBuilder = PBShareGloryPlayerItems.newBuilder();
        playerItemBuilder.setEnergyType(donateItemType.VAL);

        PBShareGloryItems.Builder itemBuilder = PBShareGloryItems.newBuilder();
        ShareGloryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ShareGloryKVCfg.class);
        //被监控的道具的统一类型
        int itemType = cfg.getItemType();

        for (Map.Entry<Integer,Integer> entry : rewardMap.entrySet()) {
            String str = String.valueOf(itemType) + "_" +
                    String.valueOf(entry.getKey()) + "_" +
                    String.valueOf(entry.getValue());
            itemBuilder.addItemId(str);
        }

        playerItemBuilder.setItems(itemBuilder);
        return playerItemBuilder;
    }

    private void fillDonateRankBuilder(String guildId, PBShareGloryDonateRank.Builder rankbuilder, DonateItemType donateItemType) {
        ShareGloryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ShareGloryKVCfg.class);
        int itemA = cfg.getItemA();
        int itemB = cfg.getItemB();

        int itemId = donateItemType == DonateItemType.typeA ? itemA : itemB;

        String guildItemKey = getGuildDonateItemkeyByGuildId( guildId, itemId);

        Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrangeWithExipre(
                guildItemKey, 0, -1, (int)TimeUnit.DAYS.toSeconds(50));

        for (Tuple rank : rankSet) {
            PBShareGloryPlayer.Builder builder = PBShareGloryPlayer.newBuilder();
            String playerId = rank.getElement();

            builder.setIcon(this.getDataGeter().getIcon(playerId));
            builder.setPlayeName(this.getDataGeter().getPlayerName(playerId));
            builder.setPfIcon(this.getDataGeter().getPfIcon(playerId));
            builder.setDonateCount((int)rank.getScore());
            builder.setPlayerId(playerId);
            if (donateItemType == DonateItemType.typeA) {
                rankbuilder.addRankA(builder);
            } else {
                rankbuilder.addRankB(builder);
            }
        }
    }

    /**
     * 玩家登陆时处理未领取的能量柱升级相关奖励
     *
     * @param playerId
     * @param guildDonatekey
     * @param donateItemType
     * @param member
     */
    private void rewardOfflinePlayer(String playerId, String guildDonatekey, DonateItemType donateItemType, String member,ShareGloryEntity entity) {
        //取联盟能量柱能量值
        int energy = getGuildEnergy(guildDonatekey, member);
        if (energy == 0) {
            HawkLog.logPrintln("shareGlory error rewardOfflinePlayer energy == 0");
            return;
        }
        //取配置文件,取得所有能量值低于当前能量值的表格行
        ArrayList<ShareGloryLevelCfg> levelCfgList = this.getShareGloryLevelCfg(donateItemType, energy);
        if (levelCfgList.isEmpty()) {
            HawkLog.logPrintln("shareGlory error rewardOfflinePlayer levelCfgList.isEmpty()");
            return;
        }

        Set<Integer> energyLevelSet  = entity.getRewardEnergyLevel(donateItemType.VAL);

        int energyLevel = getMaxLevel(energyLevelSet);
        //这里按当前能量柱的实际等级执行一次返还奖励
        returnRewardToPlayer(playerId, getCurShareGloryLevelCfg(donateItemType, energy), energyLevel, true,entity);
        //遍历配置列表，如果玩家还没有领取的等级直接发放
        for (ShareGloryLevelCfg cfg : levelCfgList) {
            if ( energyLevel >= cfg.getLevel()) {
                continue;
            }
            if(energyLevelSet.contains(cfg.getLevel())){
                continue;
            }
            //发送对应等级的奖励
            fixRewardToPlayer(playerId, cfg, entity);
        }
    }

    private int getMaxLevel(Set<Integer> energyLevel) {
        int maxLevel = 0;
        for(int level : energyLevel){
            if(level > maxLevel){
                maxLevel = level;
            }
        }
        return maxLevel;
    }

    /**
     * 取得联盟能量柱的能量值
     *
     * @param guildDonatekey
     * @param member
     * @return
     */
    private int getGuildEnergy(String guildDonatekey, String member) {
        Double tmpDouble = ActivityLocalRedis.getInstance().zScore(
                guildDonatekey, member);
        //说明工会能量柱没升级过，不处理
        if (null == tmpDouble) {
            return 0;
        }

        double doubleEnergy = tmpDouble;
        int energy = (int) doubleEnergy;
        return energy;
    }

    /**
     * 根据energy参数，计算能量住小于此值的所有配置行
     *
     * @param donateItemType
     * @param energy
     * @return 配置列表
     */
    private ArrayList<ShareGloryLevelCfg> getShareGloryLevelCfg(DonateItemType donateItemType, int energy) {
        ConfigIterator<ShareGloryLevelCfg> iterator =
                HawkConfigManager.getInstance().getConfigIterator(ShareGloryLevelCfg.class);

        ArrayList<ShareGloryLevelCfg> cfgList = new ArrayList<>();
        while (iterator.hasNext()) {
            ShareGloryLevelCfg config = iterator.next();
            if (config.getType() != donateItemType.VAL) {
                //不是当前能量配置，忽略
                continue;
            }
            //能量柱升级了
            if (energy >= config.getExpNeed()) {
                cfgList.add(config);
            }
        }
        return cfgList;
    }

    /**
     * 根据energy参数，计算能量住能量最接近此参数的配置项
     *
     * @param donateItemType
     * @param energy
     * @return
     */
    private ShareGloryLevelCfg getCurShareGloryLevelCfg(DonateItemType donateItemType, int energy) {
        ConfigIterator<ShareGloryLevelCfg> iterator =
                HawkConfigManager.getInstance().getConfigIterator(ShareGloryLevelCfg.class);

        ShareGloryLevelCfg cfg = null;
        while (iterator.hasNext()) {
            ShareGloryLevelCfg config = iterator.next();
            if (config.getType() != donateItemType.VAL) {
                //不是当前能量配置，忽略
                continue;
            }
            //能量柱升级了
            if (energy >= config.getExpNeed()) {
                cfg = config;
            }
        }
        return cfg;
    }

    /**
     * 取得玩家所在联盟指定类型的能量值
     *
     * @param playerId
     * @param donateItemType
     * @return
     */
    private int getGuildEnergyByPlayerId(String playerId, DonateItemType donateItemType, int termId) {
        String guildDonatekey = getGuildDonatekey(playerId,termId);
        if (HawkOSOperator.isEmptyString(guildDonatekey)) {
            return 0;
        }

        String member = donateItemType == DonateItemType.typeA ? energyA : energyB;

        int energy = getGuildEnergy(guildDonatekey, member);
        return energy;
    }

    private String getGuildDonateItemkey(String playerId, int itemId, int termId) {
        if (HawkOSOperator.isEmptyString(getGuildId(playerId))) {
            return null;
        }
        return getGuildDonatekey(playerId,termId) + ":" + itemId;
    }

    private String getGuildDonatekey(String playerId, int termId) {
        String guildId = getGuildId(playerId);
        if (HawkOSOperator.isEmptyString(guildId)) {
            return null;
        }
        return ActivityRedisKey.SHARE_GLORY + ":" + termId + ":" + guildId;
    }

    private String getGuildDonateItemkeyByGuildId(String guildId, int itemId) {
        return ActivityRedisKey.SHARE_GLORY + ":" +
                this.getActivityTermId() + ":" + guildId + ":" +
                itemId;
    }

    /**
     * 用道具ID判定是否是可捐献道具
     *
     * @param itemId
     * @return
     */
    private Boolean isDonateItem(int itemId) {
        ShareGloryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ShareGloryKVCfg.class);
        int itemA = cfg.getItemA();
        int itemB = cfg.getItemB();

        return itemId == itemA || itemId == itemB;
    }

    private Action getDonateAction(int itemId) {
        ShareGloryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ShareGloryKVCfg.class);
        return itemId == cfg.getItemA() ? Action.SHARE_GLORY_DONATE_COST_A : Action.SHARE_GLORY_DONATE_COST_B;
    }

    /**
     * 扣除指定数量的指定道具
     *
     * @param playerId
     * @param itemId
     * @param number
     * @return
     */
    private boolean costItem(String playerId, int itemId, int number) {
        RewardItem.Builder consumeItem = RewardItem.newBuilder();
        consumeItem.setItemType(30000);
        consumeItem.setItemId(itemId);
        consumeItem.setItemCount(number);

        List<RewardItem.Builder> consume = ImmutableList.of(consumeItem);
        Action action = getDonateAction(itemId);

        return this.getDataGeter().cost(playerId, consume, 1, action, true);
    }

    /**
     * 取得捐献排行榜的Score名
     *
     * @param playerId
     * @return 组合playerId和playerIcon
     */
    private String getDonateScore(String playerId) {
        return playerId;
    }

    private DonateItemType getDonateItemType(int itemId) {
        ShareGloryKVCfg config = HawkConfigManager.getInstance().getKVInstance(ShareGloryKVCfg.class);
        if (itemId == config.getItemA()) {
            return DonateItemType.typeA;
        } else if (itemId == config.getItemB()) {
            return DonateItemType.typeB;
        }
        return DonateItemType.typeErr;
    }

    /**
     * 处理工会能量柱，分为A能量柱和B能量柱
     */
    private void onAllianceEnergy(String playerId, int itemId, int number,int termId) {
        DonateItemType donateItemType = getDonateItemType(itemId);
        //捐献道具不是规定的类型，不继续处理
        if (donateItemType == DonateItemType.typeErr) {
            return;
        }
        //先更新下玩家捐献排行榜数值
        handlePlayerScore(playerId, itemId, number,termId);
        //处理工会能量柱
        handleEnergy(playerId, itemId, number, donateItemType,termId);
    }

    /**
     * 玩家捐献时处理工会能量柱
     *
     * @param playerId
     * @param itemId
     * @param number
     * @param donateItemType
     */
    private void handleEnergy(String playerId, int itemId, int number, DonateItemType donateItemType , int termId) {
        String guildDonatekey = getGuildDonatekey(playerId,termId);
        if (HawkOSOperator.isEmptyString(guildDonatekey)) {
            return;
        }
        String member = donateItemType == DonateItemType.typeA ? energyA : energyB;
        double tmpDouble = ActivityLocalRedis.getInstance().zIncrbyWithExpire(
                guildDonatekey, member, number, (int)TimeUnit.DAYS.toSeconds(50));
        //给客户端刷新工会能量柱状态
        onAllianceInfoReq(playerId,termId);
        int countCur = (int) tmpDouble;
        int orgCount = countCur - number;
        String guildId = getGuildId(playerId);
        //读表
        List<ShareGloryLevelCfg> levelCfgList = getShareGloryLevelCfg(donateItemType, countCur, orgCount);
        //没取到对应等级的config，不继续处理
        if (levelCfgList.isEmpty()) {
            ShareGloryLevelCfg logLevelCfg = getCurShareGloryLevelCfg(donateItemType, countCur);
            int curLevel = logLevelCfg == null ? 0 : logLevelCfg.getLevel();
            this.getDataGeter().logShareGloryDonate(playerId, this.getActivityTermId(), itemId,
                    number, guildId, orgCount, countCur, curLevel);
            return;
        }

        for(ShareGloryLevelCfg levelCfg : levelCfgList){
            //能量柱升级了，给当前在线的所有联盟成员发奖
            rewardToOnlinePlayer(playerId, levelCfg);
            this.getDataGeter().logShareGloryDonate(playerId, this.getActivityTermId(), itemId,
                    number, guildId, orgCount, countCur, levelCfg.getLevel());
            this.getDataGeter().logShareGloryEnergyLevelup(playerId, this.getActivityTermId(),
                    guildId, levelCfg.getLevel(), itemId);
        }

        //工会频道信息
        //先找到最大的等级，就是这次实际升级的等级
        ShareGloryLevelCfg curLevelCfg = levelCfgList.get(levelCfgList.size()-1);

        Const.NoticeCfgId NoticeCfgId = curLevelCfg.getType() == 1 ?
                Const.NoticeCfgId.SHAREGLORY_ENERGY_LEVELUP_A : Const.NoticeCfgId.SHAREGLORY_ENERGY_LEVELUP_B;
        // 发送请求帮助公告
        getDataGeter().addWorldBroadcastMsg(Const.ChatType.GUILD_HREF,
                getDataGeter().getGuildId(playerId),
                NoticeCfgId, playerId, curLevelCfg.getLevel());
    }

    /**
     * 给当前在线的所有联盟成员发奖
     *
     * @param playerId
     * @param levelCfg
     */
    private void rewardToOnlinePlayer(String playerId, ShareGloryLevelCfg levelCfg) {
        String guildId = getGuildId(playerId);
        //升级了发奖励，先取所有工会在线玩家，发奖励并记录领奖等级
        List<String> onlineMembers = this.getDataGeter().getOnlineGuildMemberIds(guildId);
        for (String onLineplayerId : onlineMembers) {
            Optional<ShareGloryEntity> opDataEntity = getPlayerDataEntity(onLineplayerId);
            if (!opDataEntity.isPresent()) {
                continue;
            }
            ShareGloryEntity dataEntity = opDataEntity.get();
            Set<Integer> rewardEnergyLevel = dataEntity.getRewardEnergyLevel(levelCfg.getType());
            //发放固定奖励,没给过就给
            if(!rewardEnergyLevel.contains(levelCfg.getLevel())){
                fixRewardToPlayer(onLineplayerId, levelCfg, dataEntity);
            }
            //发放返回奖励
            returnRewardToPlayer(onLineplayerId, levelCfg, levelCfg.getLevel(), false,dataEntity);
        }
    }

    /**
     * 发放能量柱升级对应等级的玩家奖励，包括固定奖励和返还奖励
     *
     * @param playerId
     * @param levelCfg
     */
    private void fixRewardToPlayer(String playerId, ShareGloryLevelCfg levelCfg, ShareGloryEntity dataEntity) {
        Object[] content = new Object[]{levelCfg.getLevel()};
        List<RewardItem.Builder> items = levelCfg.getLevelupRewardsItemList();
        //取固定奖励邮件ID
        MailId mailIdFix = levelCfg.getType() == DonateItemType.typeA.VAL ?
                MailId.ALLIANCE_SHARE_GLORY_ENERGYA_LEVELUP_REWARD :
                MailId.ALLIANCE_SHARE_GLORY_ENERGYB_LEVELUP_REWARD;

        //先设置数据库的领奖等级
        dataEntity.setRewardEnergyLevel(levelCfg.getLevel(), levelCfg.getType());
        //发送能量柱升级固定奖励邮件
        this.getDataGeter().sendMail(playerId, mailIdFix,
                null, null, content,
                items, false);
    }

    /**
     *
     * @param playerId    玩家ID
     * @param levelCfg    当前联盟能量柱等级
     * @param energyLevel 已领取奖励的能量等级
     * @param isOnLogin   是否是上线时的领取
     */
    private void returnRewardToPlayer(String playerId, ShareGloryLevelCfg levelCfg,
                                      int energyLevel, boolean isOnLogin,ShareGloryEntity dataEntity) {
        //如果是活动开启期间，并且是玩家上线时，并且玩家已领取能量升级奖励达到了当前联盟的能量柱等级
        //就不执行返还奖励的操作
        if(isOnLogin && energyLevel >= levelCfg.getLevel() && this.isOpening(playerId)){
            HawkLog.logPrintln("shareGlory returnRewardToPlayer isOnLogin return {} {} {}",
                    energyLevel, levelCfg.getLevel(),this.isOpening(playerId));
            return;
        }

        Object[] contentReward = new Object[]{levelCfg.getLevel()};
        //取返还奖励邮件ID
        MailId mailIdReturn = levelCfg.getType() == DonateItemType.typeA.VAL ?
                MailId.ALLIANCE_SHARE_GLORY_RETURN_REWARDA :
                MailId.ALLIANCE_SHARE_GLORY_RETURN_REWARDB;

        //取玩家数据中监控道具
        Map<Integer, Integer> rewardActivityMap = levelCfg.getType() == DonateItemType.typeA.VAL ?
                dataEntity.getRewardActivityMapA() : dataEntity.getRewardActivityMapB();
        //取玩家数据中监控道具已返还
        Map<Integer, Integer> rewardInfoMap = levelCfg.getType() == DonateItemType.typeA.VAL ?
                dataEntity.getRewardInfoMapA() : dataEntity.getRewardInfoMapB();

        Map<Integer, Integer> resultMap = new HashMap<>();

        //根据公式，计算应返还比例，取得返还列表
        for (Map.Entry<Integer, Integer> reward : rewardActivityMap.entrySet()) {
            int itemId = reward.getKey();
            int count = reward.getValue();
            int rewardCount = rewardInfoMap.getOrDefault(itemId, 0);
            int countResult = Math.max((int) (count * levelCfg.getRewardProportion() * 0.0001f) - rewardCount, 0);
            float rule1 = count * levelCfg.getRewardProportion() * 0.0001f;
            HawkLog.logPrintln("shareGlory itemId: {}, count: {}, rewardCount: {}, Proportion: {}, rule1: {}, countResult: {}",
                    itemId, count, rewardCount, levelCfg.getRewardProportion(), rule1, countResult);
            if(countResult <= 0){
                continue;
            }
            resultMap.put(itemId, countResult);
        }
        //没有实际返回，后面逻辑不必要再走了
        if(resultMap.isEmpty()){
            return;
        }
        //得到返还数据后，先在数据库里记录返还
        for (Map.Entry<Integer, Integer> resultReward : resultMap.entrySet()) {
            if (levelCfg.getType() == DonateItemType.typeA.VAL) {
                dataEntity.addRewardInfoA(resultReward.getKey(), resultReward.getValue());
            } else {
                dataEntity.addRewardInfoB(resultReward.getKey(), resultReward.getValue());
            }
        }
        //生成返还道具邮件的奖励内容
        List<RewardItem.Builder> itemsBuilder = new ArrayList<>();
        ShareGloryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ShareGloryKVCfg.class);
        int itemType = cfg.getItemType();
        for(Map.Entry<Integer,Integer> entry : resultMap.entrySet()){
            RewardItem.Builder builder = RewardItem.newBuilder();
            builder.setItemType(itemType);
            builder.setItemId(entry.getKey());
            builder.setItemCount(entry.getValue());
            itemsBuilder.add(builder);
            HawkLog.logPrintln("shareGlory itemsBuilder itemtype: {}, itemid: {}, count: {}", itemType, entry.getKey(), entry.getValue());
        }

        if (itemsBuilder.size() <= 0) {
            return;
        }
        //发送能量柱升级返还奖励邮件
        this.getDataGeter().sendMail(playerId, mailIdReturn, null, null, contentReward, itemsBuilder, false);
    }

    private List<ShareGloryLevelCfg> getShareGloryLevelCfg(DonateItemType donateItemType, int countCur, int orgCount) {
        ConfigIterator<ShareGloryLevelCfg> iterator =
                HawkConfigManager.getInstance().getConfigIterator(ShareGloryLevelCfg.class);
        List<ShareGloryLevelCfg> cfgList = new ArrayList<>();

        while (iterator.hasNext()) {
            ShareGloryLevelCfg config = iterator.next();
            if (config.getType() != donateItemType.VAL) {
                //不是当前能量配置，忽略
                continue;
            }
            int expNeed = config.getExpNeed();
            //能量柱升级了
            if (orgCount < expNeed && expNeed <= countCur) {
                cfgList.add(config);
            }
        }
        return cfgList;
    }

    private void handlePlayerScore(String playerId, int itemId, int number ,int termId) {
        String guildItemKey = getGuildDonateItemkey(playerId, itemId,termId);
        if (HawkOSOperator.isEmptyString(guildItemKey)) {
            return;
        }
        String scoreName = getDonateScore(playerId);
        ActivityLocalRedis.getInstance().zIncrbyWithExpire(guildItemKey, scoreName, number,
                (int)TimeUnit.DAYS.toSeconds(50));
    }

    @Override
    public void onOpen() {

    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        ShareGloryActivity activity = new ShareGloryActivity(
                config.getActivityId(), activityEntity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<ShareGloryEntity> queryList = HawkDBManager.getInstance()
                .query("from ShareGloryEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
            ShareGloryEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        ShareGloryEntity entity = new ShareGloryEntity(playerId, termId);
        return entity;
    }

    /**
     * 同步信息
     *
     * @param playerId
     * @param entity
     */
    public void syncActivityInfo(String playerId, ShareGloryEntity entity) {
    }

    /**
     * 构造工会能量信息PB
     *
     * @param playerId
     * @return
     */
    private PBShareGloryAllianceInfo.Builder buildAllianceInfo(String playerId, int termId) {
        PBShareGloryAllianceInfo.Builder alliancebuilder = PBShareGloryAllianceInfo.newBuilder();
        for (DonateItemType type : DonateItemType.values()) {
            fillAllianceBuilder(playerId, alliancebuilder, type,termId);
        }

        return alliancebuilder;
    }

    /**
     * 填充工会能量信息PB
     *
     * @param playerId
     * @param alliancebuilder
     * @param type
     */
    private void fillAllianceBuilder(String playerId, PBShareGloryAllianceInfo.Builder alliancebuilder, DonateItemType type,int termId) {
        if (type == DonateItemType.typeErr) {
            return;
        }
        PBShareGloryEnergyInfo.Builder energyBuilder = PBShareGloryEnergyInfo.newBuilder();
        energyBuilder.setType(type.VAL);
        int energy = getGuildEnergyByPlayerId(playerId, type,termId);

        if (energy == 0) {
            energyBuilder.setEnergy(energy);
            energyBuilder.setEnergyLevel(1);
            alliancebuilder.addEnergyInfo(energyBuilder);
            return;
        }
        ShareGloryLevelCfg cfg = getCurShareGloryLevelCfg(type, energy);
        int energyLevel = 0;
        if (null != cfg) {
            energyLevel = cfg.getLevel();
        }
        energyBuilder.setEnergy(energy);
        energyBuilder.setEnergyLevel(energyLevel);
        alliancebuilder.addEnergyInfo(energyBuilder);
    }

    private int getLastTermId() {
        long curTime = HawkTime.getMillisecond();
        ShareGloryTimeCfg lastCfg = null;
        List<ShareGloryTimeCfg> list = HawkConfigManager.getInstance().getConfigIterator(ShareGloryTimeCfg.class).toList();
        for(ShareGloryTimeCfg cfg : list){
            if(cfg.getHiddenTimeValue() < curTime){
                if(lastCfg == null){
                    lastCfg = cfg;
                }
                if(cfg.getTermId() > lastCfg.getTermId()){
                    lastCfg = cfg;
                }
            }
        }
        if(lastCfg == null){
            return  0;
        }
        return lastCfg.getTermId();
    }

    String getGuildId(String playerId)
    {
        int activityTermId = this.getActivityTermId();
        //取玩家数据库对象
        ShareGloryEntity entity = getRealPlayerEntity(playerId, activityTermId);
        if(null == entity){
            return "";
        }
        String guildId = entity.getGuildid();
        //说明是在线期间活动开启的，需要保存下工会ID
        if(guildId.isEmpty() && this.isOpening(playerId)){
            guildId = savePlayerGuildId(playerId);
        }
        //如果还为空有两个可能，一个是活动期间一直没上线，还一个是一直没在工会，不需要再处理
        return guildId;
    }

    private ShareGloryEntity getRealPlayerEntity(String playerId, int activityTermId){
        Optional<HawkDBEntity> opEntity = null;

        if(activityTermId == 0){
            activityTermId = getLastTermId();
            // 缓存获取
            HawkDBEntity entity = PlayerDataHelper.getInstance().getActivityDataEntity(playerId, getActivityType());
            if (entity != null) {
                IActivityDataEntity dataEntity = (IActivityDataEntity) entity;
                if (dataEntity.getTermId() == activityTermId) {
                    opEntity = Optional.of(entity);
                }
            }
            if (opEntity == null) {
                opEntity = Optional.ofNullable(loadFromDB(playerId, activityTermId));
                if (opEntity.isPresent()) {
                    PlayerDataHelper.getInstance().putActivityDataEntity(playerId, getActivityType(),opEntity.get());
                }
            }
        }else{
            opEntity = getPlayerDataEntity(playerId);
        }

        if (!opEntity.isPresent()) {
            HawkLog.logPrintln("shareGlory error rewardOfflinePlayer !opEntity.isPresent()");
            return null;
        }
        ShareGloryEntity entity = (ShareGloryEntity) opEntity.get();
        return entity;
    }

    private String savePlayerGuildId(String playerId) {
        if(!this.isOpening(playerId)){
            return "";
        }
        //刷新下玩家工会ID
        String guildId = this.getDataGeter().getGuildId(playerId);
        Optional<ShareGloryEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return "";
        }
        ShareGloryEntity entity = opDataEntity.get();
        entity.setGuildid(guildId);
        return guildId;
    }
}

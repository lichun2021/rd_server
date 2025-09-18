package com.hawk.activity.type.impl.guildDragonAttack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.GuildDragonAttackScoreEvent;
import com.hawk.activity.event.impl.GuildDragonAttackScoreMaxEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.guildDragonAttack.cfg.GuildDragonAttackAchieveCfg;
import com.hawk.activity.type.impl.guildDragonAttack.entity.GuildDragonAttackEntry;
import com.hawk.activity.type.impl.guildDragonAttack.entity.GuildDragonTrapData;
import com.hawk.game.protocol.Activity.PBDamageRank;
import com.hawk.game.protocol.Activity.PBDamageRankResp;
import com.hawk.game.protocol.Activity.PBGuildDragonAttackInfo;
import com.hawk.game.protocol.HP;
import com.hawk.log.Action;

/**
 * 巨龙陷阱
 * 
 * @author che
 *
 */
public class GuildDragonAttackActivity extends ActivityBase implements AchieveProvider {

	public GuildDragonAttackActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.GUILD_DRAGON_ATTACK;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		GuildDragonAttackActivity activity = new GuildDragonAttackActivity(config.getActivityId(), activityEntity);
        AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<GuildDragonAttackEntry> queryList = HawkDBManager.getInstance()
				.query("from GuildDragonAttackEntry where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			GuildDragonAttackEntry entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		GuildDragonAttackEntry entity = new GuildDragonAttackEntry(playerId, termId);
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
        Optional<GuildDragonAttackEntry> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        GuildDragonAttackEntry entity = opEntity.get();
        if (entity.getItemList().isEmpty()) {
            this.initAchieve(playerId);
        }
        AchieveItems items = new AchieveItems(entity.getItemList(), entity);
        return Optional.of(items);
	}


	 /**
    * 初始化成就数据
    * @param playerId 玩家id
    */
   private void initAchieve(String playerId) {
       Optional<GuildDragonAttackEntry> opEntity = getPlayerDataEntity(playerId);
       if (!opEntity.isPresent()) {
           return;
       }
       GuildDragonAttackEntry entity = opEntity.get();
       if (!entity.getItemList().isEmpty()) {
           return;
       }
       ConfigIterator<GuildDragonAttackAchieveCfg> iterator = HawkConfigManager.getInstance()
    		   .getConfigIterator(GuildDragonAttackAchieveCfg.class);
       List<AchieveItem> list = new ArrayList<>();
       while (iterator.hasNext()) {
	       GuildDragonAttackAchieveCfg cfg = iterator.next();
	       AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
	       list.add(item);
       }
       entity.setItemList(list);
   }
   
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(GuildDragonAttackAchieveCfg.class, achieveId);
        return config;
	}

	@Override
	public Action takeRewardAction() {
		return Action.GUILD_DRAGON_ATTACK_ACHIEVE;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<GuildDragonAttackEntry> opEntity = getPlayerDataEntity(playerId);
	    if (!opEntity.isPresent()) {
	    	return;
	    }
        GuildDragonAttackEntry entity = opEntity.get();
		PBGuildDragonAttackInfo.Builder builder = PBGuildDragonAttackInfo.newBuilder();
		builder.setState(0);
		builder.setAppointmentTime(0);
		builder.setOpenTime(0);
		builder.setEndTime(0);
		builder.setOpenLimitTime(0);
		builder.setDamageMax(entity.getDamageMax());
		builder.setDamageCur(0);
		builder.setDamageGuild(0);
		builder.setWorldPosx(0);
		builder.setWorldPosy(0);
		GuildDragonTrapData data = this.getDataGeter().getGuildDragonTrapData(playerId);
		if(Objects.nonNull(data)){
			builder.setState(data.inFight?1:0);
			builder.setAppointmentTime(data.appointmentTime);
			builder.setOpenTime(data.openTime);
			builder.setEndTime(data.endTime);
			builder.setOpenLimitTime(data.openTimeLimit);
			builder.setDamageCur(data.playerDamage);
			builder.setDamageGuild(data.guildDamage);
			builder.setWorldPosx(data.worldPosx);
			builder.setWorldPosy(data.worldPosy);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.ACTIVITY_GUILD_DRAGON_ATTACK_INFO_RESP, builder));
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		this.syncActivityDataInfo(playerId);
	}
	/**
	 * 开启
	 * @param playerId
	 */
	public void openAttack(String playerId,int hp){
		this.getDataGeter().guildDragonTrapOp(playerId, 1,String.valueOf(hp));
	}
	
	/**
	 * 预约开启
	 * @param playerId
	 * @param appointmentTime
	 */
	public void appointmentTimeSet(String playerId,long appointmentTime,int hp){
		this.getDataGeter().guildDragonTrapOp(playerId, 2,
				String.valueOf(appointmentTime),String.valueOf(hp));
	}

	/**
	 * 获取伤害排行
	 * @param playerId
	 */
	public void getDamageRankInfo(String playerId){
		PBDamageRankResp.Builder builder = PBDamageRankResp.newBuilder();
		List<PBDamageRank> list = this.getDataGeter().guildDragonAttackRank(playerId);
		String playerName = this.getDataGeter().getPlayerName(playerId);
		PBDamageRank.Builder self = PBDamageRank.newBuilder();
		self.setRank(0);
		self.setPlayerName(playerName);
		self.setScore(0);
		self.setPlayerId(playerId);
		if(Objects.nonNull(list) && !list.isEmpty()){
			for(PBDamageRank rank : list){
				builder.addRanks(rank);
				if(playerId.equals(rank.getPlayerId())){
					self = rank.toBuilder().clone();
				}
			}
		}
		builder.setSelfRank(self);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.ACTIVITY_GUILD_DRAGON_ATTACK_RANK_RESP_VALUE, builder));
	}
	
	@Subscribe
	public void onDamage(GuildDragonAttackScoreEvent event){
		Optional<GuildDragonAttackEntry> opEntity = getPlayerDataEntity(event.getPlayerId());
	    if (!opEntity.isPresent()) {
	    	return;
	    }
	    GuildDragonAttackEntry entry = opEntity.get();
	    if(event.getScore() > entry.getDamageMax()){
	    	 entry.setDamageMax(event.getScore());
	    }
	    ActivityManager.getInstance().postEvent(new GuildDragonAttackScoreMaxEvent(event.getPlayerId(), (int)entry.getDamageMax()), true);
	    
	}
	
}

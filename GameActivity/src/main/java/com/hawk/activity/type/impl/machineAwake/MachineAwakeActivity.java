package com.hawk.activity.type.impl.machineAwake;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.GuildDismissEvent;
import com.hawk.activity.event.impl.MachineAwakeAttackEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.msg.ActivityStateChangeMsg;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.machineAwake.cfg.MachineAwakeAchieve;
import com.hawk.activity.type.impl.machineAwake.cfg.MachineAwakeActivityKVCfg;
import com.hawk.activity.type.impl.machineAwake.cfg.MachineAwakeRankRewardCfg;
import com.hawk.activity.type.impl.machineAwake.entity.MachineAwakeEntity;
import com.hawk.activity.type.impl.machineAwake.rank.DamageRankObject;
import com.hawk.game.protocol.Activity.ActivityState;
import com.hawk.game.protocol.Activity.DamageRankType;
import com.hawk.game.protocol.Activity.GetDamageRankInfoResp;
import com.hawk.game.protocol.Activity.GetMachineAwakePageInfoResp;
import com.hawk.game.protocol.Activity.MachineAwakeState;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

import redis.clients.jedis.Tuple;

public class MachineAwakeActivity extends ActivityBase implements AchieveProvider {

	public MachineAwakeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.MACHINE_AWAKE_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.MACHINE_AWAKE_ACHIEVE_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	/** 上次排行刷新时间*/
	private long lastCheckTime = 0;
	
	/** 个人伤害排行*/
	private DamageRankObject selfRank= new DamageRankObject(DamageRankType.SELF_DAMAGE_RANK);
	
	/** 联盟伤害排行*/
	private DamageRankObject guildRank= new DamageRankObject(DamageRankType.GUILD_DAMAGE_RANK);

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		MachineAwakeActivity activity = new MachineAwakeActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<MachineAwakeEntity> queryList = HawkDBManager.getInstance()
				.query("from MachineAwakeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			MachineAwakeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		MachineAwakeEntity entity = new MachineAwakeEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		selfRank = new DamageRankObject(DamageRankType.SELF_DAMAGE_RANK);
		guildRank = new DamageRankObject(DamageRankType.GUILD_DAMAGE_RANK);
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_MACHINE_AWAKE, () -> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<MachineAwakeEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		MachineAwakeEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<MachineAwakeAchieve> configIterator = HawkConfigManager.getInstance().getConfigIterator(MachineAwakeAchieve.class);
		while (configIterator.hasNext()) {
			MachineAwakeAchieve next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		entity.notifyUpdate();
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
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
		Optional<MachineAwakeEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return Optional.empty();
		}
		MachineAwakeEntity playerDataEntity = opPlayerDataEntity.get();
		if(playerDataEntity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(MachineAwakeAchieve.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(MachineAwakeAchieve.class, achieveId);
		}
		return config;
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	/**
	 * 跨天事件
	 * @param event
	 */
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		MachineAwakeActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(MachineAwakeActivityKVCfg.class);
		if (!kvCfg.isDailyReset()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<MachineAwakeEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		MachineAwakeEntity entity = opPlayerDataEntity.get();
		List<AchieveItem> items = new ArrayList<>();
		ConfigIterator<MachineAwakeAchieve> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(MachineAwakeAchieve.class);
		while (achieveIterator.hasNext()) {
			MachineAwakeAchieve achieveCfg = achieveIterator.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			items.add(item);
		}
		entity.resetItemList(items);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		// 推送活动界面信息
		pullPageInfo(playerId);
	}

	
	/**
	 * 攻击boss
	 * @param event
	 */
	@Subscribe
	public void onAttackBoss(MachineAwakeAttackEvent event) {
		String playerId = event.getPlayerId();
		String guildId = event.getGuildId();
		int killCnt = event.getKillCnt();
		if (killCnt > 0) {
			Optional<MachineAwakeEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
			if (!opPlayerDataEntity.isPresent()) {
				return;
			}
			MachineAwakeEntity dataEntity = opPlayerDataEntity.get();
			dataEntity.setDamage(dataEntity.getDamage() + killCnt);
			int termId = getActivityTermId();
			long selfTotalDamage = selfRank.addRankScore(playerId, termId, killCnt);
			getDataGeter().logMachineAwakePersonDamage(playerId, getActivityId(), termId, killCnt, selfTotalDamage);
			guildRank.addRankScore(guildId, termId, killCnt);
			// 推送主界面信息
			pullPageInfo(playerId);
		}
	}
	
	/**
	 * 联盟解散
	 * @param event
	 */
	@Subscribe
	public void onGuildDismiss(GuildDismissEvent event){
		guildRank.removeRank(event.getGuildId(), getActivityTermId());
	}
	
	@Override
	public void onTick() {
		long currTime = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		long endTime = getTimeControl().getEndTimeByTermId(termId);
		if (currTime >= endTime) {
			return;
		}
		MachineAwakeActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(MachineAwakeActivityKVCfg.class);
		long gap = kvCfg.getRankPeriod();
		
		if (currTime - lastCheckTime > gap) {
			refreshRankInfo(termId);
			lastCheckTime = currTime;
		}
	}
	
	
	// 刷新榜单数据
	private void refreshRankInfo(int termId) {
		selfRank.refreshRank(termId);
		guildRank.refreshRank(termId);
		
	}

	@Override
	public void onEnd() {
		int termId = getActivityTermId();
		// 通知GameServer活动结束
		HawkTaskManager.getInstance().postMsg(HawkXID.valueOf(ObjType.MANAGER, ObjType.ID_SERVER_ACTIVITY),
				ActivityStateChangeMsg.valueOf(getActivityType(), termId, ActivityState.END));
		sendSelfRankReward(termId);
		sendGuildRankReward(termId);
	}
	
	/**
	 * 发送个人排行奖励
	 * @param termId
	 */
	private void sendSelfRankReward(int termId) {
		try {
			selfRank.refreshRank(termId);
			MailId mailId = MailId.GUNDAM_PLAYER_RANK;
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			Set<Tuple> rankTuple = selfRank.getRankTuples();
			int rank = 0;
			for (Tuple tuple : rankTuple) {
				rank++;
				String playerId = tuple.getElement();
				MachineAwakeRankRewardCfg rankCfg = getRankReward(rank, selfRank.rankType);
				if (rankCfg == null) {
					HawkLog.errPrintln("MachineAwakeActivity self rank cfg error! playerId: {}, rank :{}", playerId, rank);
					continue;
				}
				// 邮件发送奖励
				Object[] content;
				content = new Object[2];
				content[0] = getActivityCfg().getActivityName();
				content[1] = rank;

				sendMailToPlayer(playerId, mailId, title, subTitle, content, rankCfg.getRewardList());
				HawkLog.logPrintln("MachineAwakeActivity send self rankReward, playerId: {}, rank: {}, cfgId: {}", playerId, rank, rankCfg.getId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 发送联盟排行奖励
	 * @param termId
	 */
	private void sendGuildRankReward(int termId) {
		try {
			guildRank.refreshRank(termId);
			MailId mailId = MailId.GUNDAM_GUILD_RANK;
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			Set<Tuple> rankTuple = guildRank.getRankTuples();
			int rank = 0;
			for (Tuple tuple : rankTuple) {
				rank++;
				String guildId = tuple.getElement();
				MachineAwakeRankRewardCfg rankCfg = getRankReward(rank, guildRank.rankType);
				if (rankCfg == null) {
					HawkLog.errPrintln("MachineAwakeActivity guild rank cfg error! guildId: {}, rank :{}", guildId, rank);
					continue;
				}
				// 邮件发送奖励
				Object[] content;
				content = new Object[2];
				content[0] = getActivityCfg().getActivityName();
				content[1] = rank;
				Collection<String> ids = getDataGeter().getGuildMemberIds(guildId);
				for(String playerId : ids){
					sendMailToPlayer(playerId, mailId, title, subTitle, content, rankCfg.getRewardList());
					HawkLog.logPrintln("MachineAwakeActivity send guild rankReward, guildId: {}, playerId: {}, rank: {}, cfgId: {}", guildId, playerId, rank, rankCfg.getId());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取排行奖励配置
	 * @param rank
	 * @return
	 */
	private MachineAwakeRankRewardCfg getRankReward(int rank, DamageRankType rankType) {
		MachineAwakeRankRewardCfg rankCfg = null;
		ConfigIterator<MachineAwakeRankRewardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(MachineAwakeRankRewardCfg.class);
		for (MachineAwakeRankRewardCfg cfg : configIterator) {
			if (rankType.getNumber() == cfg.getRankType() && rank <= cfg.getRankLower() && rank >= cfg.getRankUpper()) {
				rankCfg = cfg;
				break;
			}
		}
		return rankCfg;
	}

	/**
	 * 拉取活动界面信息
	 * @param playerId
	 */
	public void pullPageInfo(String playerId) {
		Optional<MachineAwakeEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		ActivityDataProxy dataGeter = getDataGeter();
		// 获取当前机甲状态及下批机甲刷新时间
		HawkTuple2<MachineAwakeState, Long> tuple2 = dataGeter.getMachineAwakeInfo(getActivityType());
		MachineAwakeState state = tuple2.first;
		long nextRefreshTime = tuple2.second;
		int termId = getActivityTermId();
		long endTime = getTimeControl().getEndTimeByTermId(termId);

		// 最后一批刷新的机甲已被击杀,则进入结束状态
		if (state == MachineAwakeState.SLEEP && nextRefreshTime > endTime) {
			state = MachineAwakeState.FINISHED;
		}
		
		GetMachineAwakePageInfoResp.Builder builder = GetMachineAwakePageInfoResp.newBuilder();
		builder.setState(state);
		// 特殊处理,刷新倒计时多加5s显示
		builder.setNextFreshTime(nextRefreshTime + 5000);
		builder.setTotalDamage(opPlayerDataEntity.get().getDamage());
		pushToPlayer(playerId, HP.code.MACHINE_AWAKE_GET_PAGE_INFO_S_VALUE, builder);
	}

	/**
	 * 拉取榜单信息
	 * @param playerId
	 * @param rankType
	 */
	public void pullRankInfo(String playerId, DamageRankType rankType) {
		Optional<MachineAwakeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		MachineAwakeEntity entity = opEntity.get();
		GetDamageRankInfoResp.Builder builder = null;
		switch (rankType) {
		case SELF_DAMAGE_RANK:
			builder = selfRank.buildRankInfoResp(entity);
			break;

		case GUILD_DAMAGE_RANK:
			builder = guildRank.buildRankInfoResp(entity);
			break;
		}
		
		if(builder != null){
			pushToPlayer(playerId, HP.code.MACHINE_AWAKE_GET_RANK_INFO_S_VALUE, builder);
		}
		
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
}

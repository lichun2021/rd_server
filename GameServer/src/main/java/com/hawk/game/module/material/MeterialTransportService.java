package com.hawk.game.module.material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.tools.ListSplitter;
import com.hawk.activity.type.impl.materialTransport.cfg.MaterialTransportKVCfg;
import com.hawk.activity.type.impl.materialTransport.cfg.MaterialTransportResCfg;
import com.hawk.activity.type.impl.materialTransport.entity.MaterialTransportEntity;
import com.hawk.activity.type.impl.materialTransport.entity.NumberType;
import com.hawk.common.CommonConst.ServerType;
import com.hawk.common.ServerInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleAnalysisInfo;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.battle.battleIncome.IBattleIncome;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.championship.member.CHAMPlayer;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.material.MTConst.MTMarchIndex;
import com.hawk.game.module.material.MTConst.MTTruckState;
import com.hawk.game.module.material.MTConst.MTTruckType;
import com.hawk.game.module.material.data.MTGuildData;
import com.hawk.game.module.material.data.MTMember;
import com.hawk.game.module.material.data.MTTruck;
import com.hawk.game.module.material.data.MTTruckBattleRecord;
import com.hawk.game.module.material.data.MTTruckGroup;
import com.hawk.game.module.material.data.MTTruckRob;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.Const.FightResult;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.GuildChampionship.PBChampionPlayer;
import com.hawk.game.protocol.GuildChampionship.PBChampionSoldier;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.game.protocol.Mail.FightMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MaterialTransport.PBMTTruck;
import com.hawk.game.protocol.MaterialTransport.PBMaterialServiceReq;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.service.MailService;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.util.AlgorithmUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.log.Action;

import redis.clients.jedis.Jedis;

/**
 * 同组抢锁,唯一
 */
public class MeterialTransportService extends HawkAppObj {
	private Map<String, MTTruck> worldTrucks = new ConcurrentHashMap<>();
	/**自己相关的*/
	private Multimap<String, String> playerTruck = HashMultimap.create();

	private Map<String, MTTruck> guildTruckPre = new HashMap<>();
	/**联盟集结掠夺 只有本服的*/
	private CopyOnWriteArrayList<MTTruckRob> guildMass = new CopyOnWriteArrayList<>();
	/** 联盟发车数据*/
	private Map<String, MTGuildData> guildData = new ConcurrentHashMap<>();

	private List<MTTruck> marchTrucks;

	private int initTermId;
	int groupId = -1;
	List<String> groupServer = new ArrayList<>();
	private long lastCheckTime = 0;
	private static MeterialTransportService instance;

	public MeterialTransportService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	public static MeterialTransportService getInstance() {
		return instance;
	}

	public boolean canQuitGuild(Player player) {
		try {

			String guildId = player.getGuildId();
			long cur = HawkTime.getMillisecond();
			for (MTTruck truck : marchTrucks) {
				if (Objects.equals(truck.getGuildId(), guildId) && truck.getEndTime() > cur && truck.isMember(player.getId())) {
					return false;
				}
			}
			MTTruck pre = getGuildTruckPre(player.getGuildId());
			if (pre != null && pre.getState() == MTTruckState.PRE && pre.isMember(player.getId())) {
				return false;
			}
//			for (MTTruckRob rob : guildMass) {
//				if (rob.getGuildId().equals(player.getGuildId()) && rob.getBattleDataList().contains(player.getId())) {
//					return false;
//				}
//
//			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return true;
	}

	public MaterialTransportEntity getDBEntity(String playerId) {
		Optional<ActivityBase> activity = ActivityManager.getInstance().getActivity(Activity.ActivityType.MATERIAL_TRANSPORT_VALUE);
		return (MaterialTransportEntity) activity.get().getPlayerDataEntity(playerId).get();
	}

	public boolean init() {
		checkAndLoad();
		return true;
	}

	@Override
	public boolean onTick() {
		long currentTime = HawkTime.getMillisecond();
		if (currentTime - lastCheckTime < 1_000) {
			return true;
		}
		lastCheckTime = currentTime;

		
		for(MTTruck truck: worldTrucks.values()){
			if(currentTime - truck.getStartTime() > GsConst.DAY_MILLI_SECONDS* 2){
				worldTrucks.remove(truck.getId());
			}
		}
		marchTrucks = worldTrucks.values().stream().filter(t -> t.getState() == MTTruckState.MARCH).collect(Collectors.toList());
		checkAndLoad();
		truckTick();
		robTick();

		long tickEnd = HawkTime.getMillisecond();
		long tickCost = tickEnd - lastCheckTime;
		if (tickCost > 200) {
			DungeonRedisLog.log("MeterialTransportService", "tick cost {} size:{}", tickCost , worldTrucks.size());
		}

		return super.onTick();
	}

	private void robTick() {
		for (MTTruckRob rob : guildMass) {
			if (GsApp.getInstance().getCurrentTime() > rob.getEndTime() && !rob.isOver()) {
				truckRob(rob);
				guildMass.remove(rob);
			}
		}
	}

	/**抢劫列车 
	 * @return */
	public MTTruckBattleRecord truckRob(MTTruckRob rob) {
		try {
			rob.setOver(true);
			MTTruck truck = getTruck(rob.getTarTruckId());
			if (truck == null || truck.getState() != MTTruckState.MARCH) {
				return null; // 出错了, 不管了
			}
			String npcId = "rghjkergljk93298";
			List<PBChampionPlayer> atkDataList = rob.getBattleDataList();
			List<PBChampionPlayer> defDataList = truck.getBattleDataList();

			final String battleId = HawkUUIDGenerator.genUUID();
			BattleOutcome out = MeterialTransportService.getInstance().doFight(npcId, battleId, atkDataList, defDataList, rob.getMass().get(0).getName(),
					truck.getLeader().getName());
			boolean atkWin = out != null ? out.isAtkWin() : true;

			String robItemStr = "";
			if (atkWin) {
				List<ItemInfo> robItem = truck.rob();
				robItemStr = ItemInfo.toString(robItem);

			}
			MTTruckBattleRecord battleRecord = new MTTruckBattleRecord();
			battleRecord.setBattleTime(rob.getEndTime());
			battleRecord.setWin(atkWin);
			battleRecord.setBattleId(battleId);
			battleRecord.setRewardGet(robItemStr);
			battleRecord.getAttacker().addAll(rob.getMass());

			truck.getBattleRecord().add(battleRecord);

			MeterialTransportService.getInstance().addOrUpdateTruck(truck);

			sendRobReward(battleRecord, truck);

			Player leader = GlobalData.getInstance().makesurePlayer(atkDataList.get(0).getPlayerInfo().getPlayerId());
			LogUtil.logMaterialTransport(leader, 5, truck);

			return battleRecord;

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return null;
	}

	private void sendRobReward(MTTruckBattleRecord battleRecord, MTTruck truck) {
		for (MTMember atk : battleRecord.getAttacker()) {
			try {
				if (battleRecord.isWin()) {
					NumberType robnumbermap;
					int numberLimist;
					if (truck.getType() == MTTruckType.SINGLE) {
						robnumbermap = NumberType.truckRobNumberMap;
						numberLimist = MaterialTransportKVCfg.getInstance().getTruckRobNumber();
					} else {
						robnumbermap = NumberType.trainRobNumberMap;
						numberLimist = MaterialTransportKVCfg.getInstance().getTrainRobNumber();
					}
					
					int number = getDBEntity(atk.getPlayerId()).getNumber(robnumbermap);
					if (number < numberLimist) {
						MailParames mailParames = MailParames.newBuilder()
								.setMailId(MailId.MT_ROB_WIN)
								.setPlayerId(atk.getPlayerId())
								.setAwardStatus(MailRewardStatus.GET) // 直接发了 好查流水
								.addSubTitles(truck.getStartTime(),truck.getType().getNumber(), truck.getQuality())
								.addContents(truck.getStartTime(), truck.getType().getNumber(),truck.getQuality())
								.setRewards(battleRecord.getRewardGet())
								.build();
						MailService.getInstance().sendMail(mailParames);

						AwardItems awardItem = AwardItems.valueOf();
						awardItem.addItemInfos(ItemInfo.valueListOf(battleRecord.getRewardGet()));
						Player player = GlobalData.getInstance().makesurePlayer(atk.getPlayerId());
						awardItem.rewardTakeAffectAndPush(player, Action.MT_TRAIN_ROB, RewardOrginType.MT_TRUCK_REACH);

						getDBEntity(atk.getPlayerId()).incNumber(robnumbermap);
					} else {
						MailParames mailParames = MailParames.newBuilder()
								.setMailId(MailId.MT_WIN_LIMIT)
								.setPlayerId(atk.getPlayerId())
								.setAwardStatus(MailRewardStatus.GET)
								.addSubTitles(truck.getStartTime(),truck.getType().getNumber(), truck.getQuality())
								.addContents(truck.getStartTime(), truck.getType().getNumber(),truck.getQuality())
								.build();
						MailService.getInstance().sendMail(mailParames);
					}

				} else {
					MailParames mailParames = MailParames.newBuilder()
							.setMailId(MailId.MT_ROB_LOSE)
							.setPlayerId(atk.getPlayerId())
							.setAwardStatus(MailRewardStatus.GET)
							.addSubTitles(truck.getStartTime(),truck.getType().getNumber(), truck.getQuality())
							.addContents(truck.getStartTime(), truck.getType().getNumber(),truck.getQuality())
							.build();
					MailService.getInstance().sendMail(mailParames);
				}

			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

	}

	/**取得当前准备中的联盟列车*/
	public MTTruck getGuildTruckPre(String guildId) {
		return guildTruckPre.get(guildId);

	}

	/** 货车逻辑tick*/
	private void truckTick() {
		for (MTTruck truck : guildTruckPre.values()) {
			if (GlobalData.getInstance().isLocalServer(truck.getServerId())) {
				guildMarchStartCheck(truck);
			}
		}

		for (MTTruck truck : marchTrucks) {
			reacheCheck(truck);
		}
	}

	private void guildMarchStartCheck(MTTruck truck) {
		try {

			if (truck.getState() != MTTruckState.PRE || truck.getType() == MTTruckType.SINGLE) {
				return;
			}
			long now = HawkTime.getMillisecond();
			if (truck.getStartTime() > now) {
				return;
			}
			Player player = GlobalData.getInstance().makesurePlayer(truck.getLeader().getPlayerId());

			truck.setOrigionX(player.getPosXY()[0]);
			truck.setOrigionY(player.getPosXY()[1]);
			int[] tarpos = MeterialTransportService.getInstance().farestSuperWeapon(player.getPosXY());
			truck.setTerminalX(tarpos[0]);
			truck.setTerminalY(tarpos[1]);

			truck.setState(MTTruckState.MARCH);

			MeterialTransportService.getInstance().addOrUpdateTruck(truck);

			for (PBChampionPlayer mem : truck.getBattleDataList()) {
				getDBEntity(mem.getPlayerInfo().getPlayerId()).incNumber(getTrainNumberMap(truck.getType()).first);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public void reacheCheck(MTTruck truck) {

		if (truck.getState() != MTTruckState.MARCH) {
			return;
		}
		if (truck.getEndTime() > GsApp.getInstance().getCurrentTime()) {
			return;
		}
		truck.setState(MTTruckState.REACHED);
		if (GlobalData.getInstance().isLocalServer(truck.getServerId())) {
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					sendReachReward(truck);
					return null;
				}
			});

			MeterialTransportService.getInstance().addOrUpdateTruck(truck);
			Player leader = GlobalData.getInstance().makesurePlayer(truck.getPlayerId());
			LogUtil.logMaterialTransport(leader, 4, truck);
		}
	}

	private void sendReachReward(MTTruck truck) {
		// 到了, 发奖励邮件
		if (truck.getType() == MTTruckType.SINGLE) {
			List<ItemInfo> rewards = truck.getCompartments().get(0).getRewards();
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(truck.getPlayerId())
					.setMailId(MailId.MT_SINGLE_REACH)
					.setAwardStatus(MailRewardStatus.GET)
					.addSubTitles(truck.getStartTime(),truck.getType().getNumber(), truck.getQuality())
					.addContents(truck.getStartTime(), truck.getType().getNumber(),truck.getQuality())
					.setRewards(rewards)
					.build());
			AwardItems awardItem = AwardItems.valueOf();
			awardItem.addItemInfos(rewards);
			Player player = GlobalData.getInstance().makesurePlayer(truck.getPlayerId());
			awardItem.rewardTakeAffectAndPush(player, Action.MT_TRUCK_REACH, RewardOrginType.MT_TRUCK_REACH);
			return;
		}

		// 车头奖励
		{
			HawkTuple2<NumberType, Integer> trainNumberMap = MeterialTransportService.getInstance().getTrainNumberMap(truck.getType());
			if (getDBEntity(truck.getLeader().getPlayerId()).getNumber(trainNumberMap.first) <= trainNumberMap.second) {
				List<ItemInfo> awards = new ArrayList<>();
				awards.addAll(ItemInfo.valueListOf(truck.getTrainReward()));
				awards.addAll(truck.getCompartment(1).getRewards());
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(truck.getLeader().getPlayerId())
						.setMailId(MailId.MT_SINGLE_REACH)
						.setAwardStatus(MailRewardStatus.GET)
						.addSubTitles(truck.getStartTime(),truck.getType().getNumber(), truck.getQuality())
						.addContents(truck.getStartTime(), truck.getType().getNumber(),truck.getQuality())
						.setRewards(awards)
						.build());

				AwardItems awardItem = AwardItems.valueOf();
				awardItem.addItemInfos(awards);
				Player player = GlobalData.getInstance().makesurePlayer(truck.getLeader().getPlayerId());
				awardItem.rewardTakeAffectAndPush(player, Action.MT_TRUCK_REACH, RewardOrginType.MT_TRUCK_REACH);
			}
		}
		for (MTTruckGroup group : truck.getCompartments()) {
			List<ItemInfo> award = group.getRewards();
			for (MTMember member : group.getMemberList()) {
				try {
					HawkTuple2<NumberType, Integer> trainNumberMap = MeterialTransportService.getInstance().getTrainNumberMap(truck.getType());
					if (getDBEntity(member.getPlayerId()).getNumber(trainNumberMap.first) <= trainNumberMap.second) {
						SystemMailService.getInstance().sendMail(MailParames.newBuilder()
								.setPlayerId(member.getPlayerId())
								.setMailId(MailId.MT_SINGLE_REACH)
								.addSubTitles(truck.getStartTime(),truck.getType().getNumber(), truck.getQuality())
								.addContents(truck.getStartTime(), truck.getType().getNumber(),truck.getQuality())
								.setRewards(award)
								.build());
						AwardItems awardItem = AwardItems.valueOf();
						awardItem.addItemInfos(award);
						Player player = GlobalData.getInstance().makesurePlayer(member.getPlayerId());
						awardItem.rewardTakeAffectAndPush(player, Action.MT_TRUCK_REACH, RewardOrginType.MT_TRUCK_REACH);

					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}

		}
	}

	private void checkAndLoad() {
		if (groupId > -1 && initTermId == getTermId()) {
			return;
		}

		String serverId = GsConfig.getInstance().getServerId();
		String lockKey = "mtlockcxweoi33" + getTermId();
		String result = RedisProxy.getInstance().getRedisSession().hGet(lockKey, serverId);
		if (StringUtils.isEmpty(result)) {
			List<String> serverList = getOpenServerList();
			MaterialTransportKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(MaterialTransportKVCfg.class);
			int amount = kvCfg.getServerNumber(); // - 服务器id相邻n个服参与匹配 - n读取 material_transport_cfg 表 serverNumber 字段
			List<List<String>> list = ListSplitter.splitList(serverList, amount);
			for (int i = 0; i < list.size(); i++) {
				List<String> groupServerList = list.get(i);
				for (String serv : groupServerList) {
					RedisProxy.getInstance().getRedisSession().hSetNx(lockKey, serv, Integer.valueOf(i + 1).toString());
				}
			}
		}

		groupId = NumberUtils.toInt(RedisProxy.getInstance().getRedisSession().hGet(lockKey, serverId));
		Map<String, String> map = RedisProxy.getInstance().getRedisSession().hGetAll(lockKey);
		List<String> groupServerList = new ArrayList<>();
		for (Entry<String, String> ent : map.entrySet()) {
			if (ent.getValue().equals(groupId + "")) {
				groupServerList.add(ent.getKey());
			}
		}
		groupServer = groupServerList;
		initTermId = getTermId();

		worldTrucks = new ConcurrentHashMap<>();
		/**自己发的, 排队的*/
		playerTruck = HashMultimap.create();

		Map<byte[], byte[]> all = RedisProxy.getInstance().getRedisSession().hGetAllBytes(truckRedisKey());// 活动期间所有的 发车.预计总量5000. 内存压力应该不大. 如有压力, 考虑 truckRedisKey() 只存id. 本体以key value形式分开存放
		for (byte[] data : all.values()) {
			MTTruck truck = new MTTruck();
			PBMTTruck.Builder builder = PBMTTruck.newBuilder();
			try {
				builder.mergeFrom(data);
			} catch (InvalidProtocolBufferException e) {
				HawkException.catchException(e);
			}
			truck.mergeFrom(builder.build());
			addTruck(truck);
		}
	}

	public int getTermId() {
		Optional<ActivityBase> activity = ActivityManager.getInstance().getActivity(Activity.ActivityType.MATERIAL_TRANSPORT_VALUE);
		if (activity.isPresent()) {
			return activity.get().getActivityTermId();
		}
		return 0;
	}

	/**
	 * 获取符合开启条件的区服
	 */
	private List<String> getOpenServerList() {
		List<ServerInfo> serverInfoList = RedisProxy.getInstance().getServerList().stream().filter(s -> GlobalData.getInstance().isMainServer(s.getId()))
				.filter(s -> s.getServerType() == ServerType.NORMAL).collect(Collectors.toList());
		Collections.sort(serverInfoList, Comparator.comparing(ServerInfo::getOpenTime));

		List<String> result = new ArrayList<>();
		long now = HawkTime.getMillisecond();
		long serverDelay = MaterialTransportKVCfg.getInstance().getServerDelay();
		for (ServerInfo sinfo : serverInfoList) {
			long timeLimit = HawkTime.parseTime(sinfo.getOpenTime()) + serverDelay;
			if (timeLimit < now) {
				result.add(sinfo.getId());
			}
		}

		return result;
	}

	private byte[] truckRedisKey() {
		return ("MTTruckss2:" + getTermId() + ":" + groupId).getBytes();
	}

	private String keyPlayerBattleData(String playerId, MTMarchIndex index) {
		final String key = "MTbattledata:" + playerId + ":" + index.getNumber();
		return key;
	}

	/** 玩家预设行军战斗用*/
	public void updateBattleData(String playerId, MTMarchIndex index, PBChampionPlayer data) {
		final String key = keyPlayerBattleData(playerId, index);
		RedisProxy.getInstance().getRedisSession().setBytes(key, data.toByteArray());
	}

	public PBChampionPlayer getBattleData(MTMarchIndex index, String playerId) {
		final String key = keyPlayerBattleData(playerId, index);
		byte[] value = RedisProxy.getInstance().getRedisSession().getBytes(key.getBytes());
		PBChampionPlayer.Builder result = PBChampionPlayer.newBuilder();
		try {
			if (Objects.nonNull(value)) {
				result.mergeFrom(value);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return result.build();
	}

	public List<PBChampionPlayer> getBattleData(MTMarchIndex index, List<String> playerIds) {
		if (playerIds.isEmpty()) {
			return Collections.emptyList();
		}
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis()) {
			List<PBChampionPlayer> result = new ArrayList<>();
			List<byte[]> keys = playerIds.stream()
					.filter(StringUtils::isNotEmpty)
					.map(pid -> keyPlayerBattleData(pid, index).getBytes()).collect(Collectors.toList());
			List<byte[]> values = jedis.mget(keys.toArray(new byte[0][0]));
			for (byte[] value : values) {
				if (Objects.nonNull(value)) {
					result.add(PBChampionPlayer.newBuilder().mergeFrom(value).build());
				}
			}
			return result;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return Collections.emptyList();
	}

	public void addOrUpdateTruck(MTTruck truck) {
		truck.setPbObj(null);

		RedisProxy.getInstance().getRedisSession().hSetBytes(truckRedisKey(), truck.getId().getBytes(), truck.toPBObj().toByteArray(), 24 * 3600 * 30);

		addTruck(truck);

		for (String toServerId : groupServer) { // 通知其它服A->B
			if (toServerId.equals(GsConfig.getInstance().getServerId())) {
				continue;
			}
			PBMaterialServiceReq.Builder csReq = PBMaterialServiceReq.newBuilder();
			csReq.setProtoclType(HP.code2.MT_SERVICE_REQ_VALUE);
			csReq.setReq(truck.toPBObj().toByteString());
			HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code2.MT_SERVICE_REQ_VALUE, csReq);
			CrossProxy.getInstance().sendNotify(hawkProtocol, toServerId, null);
		}
	}

	/**B 接收*/
	@ProtocolHandler(code = HP.code2.MT_SERVICE_REQ_VALUE)
	public void onServerReq(HawkProtocol hawkProtocol) {
		PBMaterialServiceReq req = hawkProtocol.parseProtocol(PBMaterialServiceReq.getDefaultInstance());
		final int protoType = req.getProtoclType();
		switch (protoType) {
		case HP.code2.MT_SERVICE_REQ_VALUE:
			MTTruck truck = new MTTruck();
			PBMTTruck.Builder builder = PBMTTruck.newBuilder();
			try {
				builder.mergeFrom(req.getReq());
			} catch (InvalidProtocolBufferException e) {
				HawkException.catchException(e);
			}
			truck.mergeFrom(builder.build());
			addTruck(truck);
			break;

		default:
			break;
		}
	}

	private void addTruck(MTTruck truck) {
		worldTrucks.put(truck.getId(), truck);

		playerTruck.put(truck.getLeader().getPlayerId(), truck.getId());
		for (MTTruckGroup group : truck.getCompartments()) {
			for (MTMember member : group.getMemberList()) {
				playerTruck.put(member.getPlayerId(), truck.getId());
			}
		}

		for (MTTruckBattleRecord bt : truck.getBattleRecord()) {
			for (MTMember member : bt.getAttacker()) {
				playerTruck.put(member.getPlayerId(), truck.getId());
			}
		}

	}

	public MTTruck getTruck(String truckId) {
		return worldTrucks.get(truckId);
	}

	public BattleOutcome doFight(String npcId, String battleId, List<PBChampionPlayer> atkDataList, List<PBChampionPlayer> defDataList, String playerA, String playerB) {
		try {

			List<Player> atkPlayers = new ArrayList<>();
			// 防守方玩家
			List<Player> defPlayers = new ArrayList<>();
			// 进攻方行军
			List<IWorldMarch> atkMarchs = new ArrayList<>();
			// 防守方行军
			List<IWorldMarch> defMarchs = new ArrayList<>();

			for (PBChampionPlayer atkData : atkDataList) {
				IWorldMarch atkMarch = buildMarch(atkData);
				atkPlayers.add(atkMarch.getPlayer());
				atkMarchs.add(atkMarch);

			}

			for (PBChampionPlayer defData : defDataList) {
				IWorldMarch defMarch = buildMarch(defData);
				defPlayers.add(defMarch.getPlayer());
				defMarchs.add(defMarch);
			}

			// 战斗数据输入
			PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.GUILD_CHAMPIONSHIP, 0, atkPlayers, defPlayers, atkMarchs, defMarchs,
					BattleSkillType.BATTLE_SKILL_NONE);
			battleIncome.getBattle().setDuntype(DungeonMailType.TBLY);
			// 战斗数据输出
			BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
			recordGcMail(npcId, battleId, battleIncome, battleOutcome, playerA, playerB);
			return battleOutcome;

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	public void recordGcMail(String npcId, String battleId, IBattleIncome battleIncome, BattleOutcome battleOutcome, String playerA, String playerB) {
		try {

			String playerId = npcId;
			boolean isAtksWin = battleOutcome.isAtkWin();
			// 战斗邮件
			FightMail.Builder attackMail = MailBuilderUtil.createFightMail(battleIncome, battleOutcome, true);
			attackMail.getSelfPlayerBuilder().setName(playerA);
			FightMail.Builder defenseMail = MailBuilderUtil.createFightMail(battleIncome, battleOutcome, false);
			defenseMail.getSelfPlayerBuilder().setName(playerB);
			MailId mailId = null;
			if (isAtksWin) {
				attackMail.setResult(FightResult.ATTACK_SUCC);
				mailId = MailId.METERIAL_TRANSPORT_ATK_WIN;
			} else {
				attackMail.setResult(FightResult.ATTACK_FAIL);
				mailId = MailId.METERIAL_TRANSPORT_ATK_FAILED;
			}
			BattleAnalysisInfo analysisInfo = FightMailService.getInstance().makeBattleAnalysis(battleIncome, battleOutcome, isAtksWin);

			attackMail.setBattleAnalysis(analysisInfo.getAtkAnalysis());
			attackMail.setBattlePrompt(analysisInfo.getAtkPrompt());
			attackMail.setBattleGreat(analysisInfo.getBattleGreat());

			attackMail.setOppFight(defenseMail.getSelfFight());
			attackMail.addAllOppArmy(defenseMail.getSelfArmyList());
			attackMail.setOppPlayer(defenseMail.getSelfPlayer());
			attackMail.addAllOppfEffs(defenseMail.getSelfEffsList());

			MailParames.Builder paraBuilder = MailParames.newBuilder()
					.setPlayerId(playerId)
					.setUuid(battleId)
					.setMailId(mailId)
					.addContents(attackMail);
			FightMailService.getInstance().sendMail(paraBuilder.build());

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public IWorldMarch buildMarch(PBChampionPlayer sourceData) {
		TemporaryMarch atkMarch = new TemporaryMarch();
		List<ArmyInfo> armys = new ArrayList<>(sourceData.getSoldiersCount());
		for (PBChampionSoldier sd : sourceData.getSoldiersList()) {
			if (sd.getCount() > 0) {
				armys.add(new ArmyInfo(sd.getArmyId(), sd.getCount()));
			}
		}

		CHAMPlayer player = new CHAMPlayer(HawkXID.nullXid(), sourceData);
		atkMarch.setArmys(armys);
		atkMarch.setPlayer(player);
		atkMarch.getMarchEntity().setArmourSuit(ArmourSuitType.ONE_VALUE);
		atkMarch.getMarchEntity().setMechacoreSuit(MechaCoreSuitType.MECHA_ONE_VALUE);
		atkMarch.getMarchEntity().setHeroIdList(sourceData.getHerosList().stream().map(PBHeroInfo::getHeroId).collect(Collectors.toList()));
		atkMarch.getMarchEntity().setSuperSoldierId(sourceData.getSuperSoldier().getSuperSoldierId());
		atkMarch.setHeros(player.getHeroByCfgId(null));
		atkMarch.getMarchEntity().setDressList(sourceData.getDressIdList());
		return atkMarch;
	}

	public Map<String, MTTruck> getWorldTrucks() {
		return worldTrucks;
	}

	public void setWorldTrucks(Map<String, MTTruck> worldTrucks) {
		this.worldTrucks = worldTrucks;
	}

	public Multimap<String, String> getPlayerTruck() {
		return playerTruck;
	}

	public void setPlayerTruck(Multimap<String, String> playerTruck) {
		this.playerTruck = playerTruck;
	}

	public Map<String, MTTruck> getGuildTruckPre() {
		return guildTruckPre;
	}

	public void setGuildTruckPre(Map<String, MTTruck> guildTruckPre) {
		this.guildTruckPre = guildTruckPre;
	}

	public int getInitTermId() {
		return initTermId;
	}

	public void setInitTermId(int initTermId) {
		this.initTermId = initTermId;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public List<String> getGroupServer() {
		return groupServer;
	}

	public void setGroupServer(List<String> groupServer) {
		this.groupServer = groupServer;
	}

	private static void setInstance(MeterialTransportService instance) {
	}

	public Set<IWorldMarch> calcInviewMarchs(int x, int y) {
		return Collections.emptySet();
	}

	public List<MTTruckRob> getGuildMass(String guildId) {
		return guildMass.stream().filter(r -> r.getGuildId().equals(guildId)).collect(Collectors.toList());
	}

	public void recordGuildMass(MTTruckRob rob) {
		guildMass.add(rob);
	}

	public List<MTTruck> getMarchTrucks() {

		return marchTrucks;
	}

	/** 当日发车数*/
	public int getGuildNum(String guildId, MTTruckType type) {
		MTGuildData data = getGuildData(guildId);
		switch (type) {
		case GUILD:
			return data.getAllianceCommonTrainNumber();

		case GUILDBIG:
			return data.getAllianceSpecialTrainNumber();

		default:
			break;
		}
		return 0;
	}

	public void incGuildNumber(String guildId, MTTruckType type) {
		MTGuildData data = getGuildData(guildId);
		switch (type) {
		case GUILD:
			data.setAllianceCommonTrainNumber(data.getAllianceCommonTrainNumber() + 1);
			break;
		case GUILDBIG:
			data.setAllianceSpecialTrainNumber(data.getAllianceSpecialTrainNumber() + 1);
			break;
		default:
			break;
		}
		updataGuildData(data);
	}

	private MTGuildData getGuildData(String guildId) {
		MTGuildData data = guildData.get(guildId);
		int yearDay = HawkTime.getYearDay();
		if (data == null || data.getYearDay() != yearDay) {
			data = new MTGuildData();
			data.setYearDay(yearDay);
			data.setGuildId(guildId);
			String str = RedisProxy.getInstance().getRedisSession().getString(guildDataKey(guildId, yearDay));
			if (StringUtils.isNotEmpty(str)) {
				data.mergeFrom(str);
			}
			guildData.put(guildId, data);
		}
		return data;
	}

	public void updataGuildData(MTGuildData data) {
		String key = guildDataKey(data.getGuildId(), data.getYearDay());
		RedisProxy.getInstance().getRedisSession().setString(key, data.serializStr(), 72 * 3600 * 1000);
	}

	private String guildDataKey(String guildId, int yearDay) {
		return "mtgdatacnt:" + guildId + ":" + yearDay;
	}

	public int[] farestSuperWeapon(int[] opos) {
		int[] result = null;
		for (int pointId : SuperWeaponService.getInstance().getSuperWeaponPoints()) {
			int[] pos = GameUtil.splitXAndY(pointId);
			if (result == null) {
				result = pos;
				continue;
			}
			if (AlgorithmUtil.lineDistance(opos[0], opos[1], pos[0], pos[1]) > AlgorithmUtil.lineDistance(opos[0], opos[1], result[0], result[1])) {
				result = pos;
			}
		}
		return result;
	}

	public long getNeedTime(MTTruck truck) {
		ConfigIterator<MaterialTransportResCfg> it = HawkConfigManager.getInstance().getConfigIterator(MaterialTransportResCfg.class);
		try {
			if (truck.getType() == MTTruckType.SINGLE) {
				int quility = truck.getQuality();
				return it.stream().filter(c -> c.getId() == quility).findAny().get().getNeedTime() * 1000;
			}
			return it.stream().filter(c -> c.getType() == truck.getType().getNumber()).findAny().get().getNeedTime() * 1000;

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return it.next().getNeedTime() * 1000;
	}

	/**
	 * .普通联盟车和豪华联盟车上车奖励次数分开统计
	 */
	public HawkTuple2<NumberType, Integer> getTrainNumberMap(MTTruckType type) {
		switch (type) {
		case GUILD:
			return HawkTuples.tuple(NumberType.trainNumberMap, MaterialTransportKVCfg.getInstance().getTrainNumber());
		case GUILDBIG:
			return HawkTuples.tuple(NumberType.specialTrainNumberMap, MaterialTransportKVCfg.getInstance().getSpecialTrainNumber());
		default:
			break;
		}
		return null;
	}

}

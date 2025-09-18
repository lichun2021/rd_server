package com.hawk.game.module.material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.activity.type.impl.materialTransport.cfg.MaterialTransportKVCfg;
import com.hawk.activity.type.impl.materialTransport.entity.MaterialTransportEntity;
import com.hawk.activity.type.impl.materialTransport.entity.NumberType;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.march.MarchSet;
import com.hawk.game.module.PlayerMarchModule;
import com.hawk.game.module.material.MTConst.MTMarchIndex;
import com.hawk.game.module.material.MTConst.MTTruckState;
import com.hawk.game.module.material.MTConst.MTTruckType;
import com.hawk.game.module.material.data.MTMember;
import com.hawk.game.module.material.data.MTTruck;
import com.hawk.game.module.material.data.MTTruckBattleRecord;
import com.hawk.game.module.material.data.MTTruckGroup;
import com.hawk.game.module.material.data.MTTruckRob;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildChampionship.PBChampionEff;
import com.hawk.game.protocol.GuildChampionship.PBChampionPlayer;
import com.hawk.game.protocol.GuildChampionship.PBChampionSoldier;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MaterialTransport.PBGuildTruckCreateReq;
import com.hawk.game.protocol.MaterialTransport.PBGuildTruckLeaderReq;
import com.hawk.game.protocol.MaterialTransport.PBGuildTruckMassRobReq;
import com.hawk.game.protocol.MaterialTransport.PBGuildTruckPaiDuiReq;
import com.hawk.game.protocol.MaterialTransport.PBGuildTruckRefreshReq;
import com.hawk.game.protocol.MaterialTransport.PBMTAcvityInfoSync;
import com.hawk.game.protocol.MaterialTransport.PBMTHistoryReq;
import com.hawk.game.protocol.MaterialTransport.PBMTHistoryResp;
import com.hawk.game.protocol.MaterialTransport.PBMTOtherTruckRefrashReq;
import com.hawk.game.protocol.MaterialTransport.PBMTRefreshItemBuyReq;
import com.hawk.game.protocol.MaterialTransport.PBMTTruckListResp;
import com.hawk.game.protocol.MaterialTransport.PBMTTruckShareReq;
import com.hawk.game.protocol.MaterialTransport.PBSelfTruckCreateReq;
import com.hawk.game.protocol.MaterialTransport.PBSelfTruckRefreshReq;
import com.hawk.game.protocol.MaterialTransport.PBSelfTruckRobReq;
import com.hawk.game.protocol.MaterialTransport.PBSelfTruckRobResp;
import com.hawk.game.protocol.MaterialTransport.PBSelfTruckRobResp.Builder;
import com.hawk.game.protocol.MaterialTransport.PBSelfTruckStartReq;
import com.hawk.game.protocol.MaterialTransport.PBTRUCKPreMarchResp;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.MarchData;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.MarchEventSync;
import com.hawk.game.protocol.World.PlayerEnterWorld;
import com.hawk.game.protocol.World.PlayerWorldMove;
import com.hawk.game.protocol.World.PresetMarchInfo;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GsConst.ModuleType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.log.Action;

public class PlayerMeterialModule extends PlayerModule {
	final String MTPreMarch = "medalfctorywejsdl:";
	final int TRUCK1 = 1;
	final int TRUCK2 = 2;
	MTTruck pretruck1 = null;
	MTTruck pretruck2 = null;
	private int moveCnt;
	private List<MTTruck> refreshList = new ArrayList<>();
	long lastRefresh;
	private MarchSet inviewMarchs = new MarchSet();

	@Override
	protected boolean onPlayerLogin() {
		return super.onPlayerLogin();
	}

	public PlayerMeterialModule(Player player) {
		super(player);
	}

	@Override
	public boolean isListenProto(int proto) {
		if (player instanceof CsPlayer) {
			return false;
		}
		return super.isListenProto(proto);
	}

	/**分享*/
	@ProtocolHandler(code = HP.code2.MT_TRUCK_SHARE_REQ_VALUE)
	private boolean onMT_TRUCK_SHARE_REQ(HawkProtocol protocol) {
		PBMTTruckShareReq req = protocol.parseProtocol(PBMTTruckShareReq.getDefaultInstance());
		MTTruck truck = MeterialTransportService.getInstance().getTruck(req.getTruckId());
		shareToGuild(truck, NoticeCfgId.MT_TRUCK_SHARE);
		player.responseSuccess(protocol.getType());
		return true;
	}

	private void shareToGuild(MTTruck truck, NoticeCfgId noticeId) {
		ChatParames chatParams = ChatParames.newBuilder()
				.setChatType(Const.ChatType.CHAT_ALLIANCE)
				.setKey(noticeId)
				.setGuildId(player.getGuildId())
				.setPlayer(player)
				.addParms(truck.getLeader().getName())
				.addParms(truck.getId())
				.addParms(truck.getServerId())
				.addParms(truck.getGuildName())
				.addParms(truck.getType().getNumber())
				.addParms(truck.getQuality())
				.build();
		ChatService.getInstance().addWorldBroadcastMsg(chatParams);
	}

	/**
	 * 购买刷新道具
	 */
	@ProtocolHandler(code = HP.code2.MT_BUY_REFRESH_ITEM_VALUE)
	private boolean onBuyRefreshCost(HawkProtocol protocol) {
		PBMTRefreshItemBuyReq cmd = protocol.parseProtocol(PBMTRefreshItemBuyReq.getDefaultInstance());

		ConsumeItems consumeItems = ConsumeItems.valueOf();
		List<ItemInfo> valueListOf = ItemInfo.valueListOf(MaterialTransportKVCfg.getInstance().getBuyRefreshCost());
		valueListOf.forEach(item -> item.setCount(item.getCount() * cmd.getCount()));
		consumeItems.addConsumeInfo(valueListOf, false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return false;
		}
		consumeItems.consumeAndPush(player, Action.MT_TRAIN_REFRESH);

		AwardItems awardItem = AwardItems.valueOf();
		List<ItemInfo> valueListOf2 = ItemInfo.valueListOf(MaterialTransportKVCfg.getInstance().getTruckRefreshCost());
		valueListOf2.forEach(item -> item.setCount(item.getCount() * cmd.getCount()));
		awardItem.addItemInfos(valueListOf2);
		awardItem.rewardTakeAffectAndPush(player, Action.MT_TRAIN_REFRESH);

		return true;
	}

	/**
	 * 玩家在世界地图滑动视野(移动)
	 */
	@ProtocolHandler(code = HP.code.PLAYER_WORLD_MOVE_VALUE)
	private boolean onPlayerWorldMove(HawkProtocol protocol) {
		PlayerWorldMove cmd = protocol.parseProtocol(PlayerWorldMove.getDefaultInstance());
		if (cmd.getSpeed() == 0.01f && moveCnt++ % 2 == 0) { // 快速滑动
			return true;
		}
		onPlayerMove(player, cmd.getX(), cmd.getY());

		return true;
	}

	/**
	 * 进入世界地图
	 */
	@ProtocolHandler(code = HP.code.PLAYER_ENTER_WORLD_VALUE)
	private boolean onPlayerEnterWorld(HawkProtocol protocol) {
		PlayerEnterWorld cmd = protocol.parseProtocol(PlayerEnterWorld.getDefaultInstance());

		onPlayerMove(player, cmd.getX(), cmd.getY());

		return true;
	}

	/**
	 * 离开世界地图
	 */
	@ProtocolHandler(code = HP.code.PLAYER_LEAVE_WORLD_VALUE)
	private boolean onPlayerLeaveWorld(HawkProtocol protocol) {

		inviewMarchs.clear();

		return true;
	}

	public void onPlayerMove(Player player, int x, int y) {
		// 当前的
		MarchEventSync.Builder addbuilder = MarchEventSync.newBuilder();
		addbuilder.setEventType(MarchEvent.MARCH_ADD_VALUE);

		MarchEventSync.Builder delbuilder = MarchEventSync.newBuilder();
		delbuilder.setEventType(MarchEvent.MARCH_DELETE_VALUE);
		MarchSet currentSet = new MarchSet();
		for (MTTruck march : MeterialTransportService.getInstance().getMarchTrucks()) {
			// WorldMarchRelation relation = march.getRelation(player);
			// if (relation.equals(WorldMarchRelation.SELF)) {
			// continue;
			// }
			boolean hasPush = getInviewMarchs().contains(march.getMarchId());
			boolean inview = march.isInview(x, y);
			if (inview) {
				currentSet.add(march.getMarchId());
			}
			if (inview && !hasPush) {
				WorldMarchRelation relation = march.getRelation(player);

				MarchData.Builder dataBuilder = MarchData.newBuilder();
				dataBuilder.setMarchId(march.getMarchId());
				dataBuilder.setMarchPB(march.toBuilder(relation));
				addbuilder.addMarchData(dataBuilder);
			}

			if (!inview && hasPush) {
				MarchData.Builder dataBuilder = MarchData.newBuilder();
				dataBuilder.setMarchId(march.getMarchId());
				delbuilder.addMarchData(dataBuilder);
			}
		}
		if (addbuilder.getMarchDataCount() > 0) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_EVENT_SYNC_VALUE, addbuilder));
		}
		if (delbuilder.getMarchDataCount() > 0) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_EVENT_SYNC_VALUE, delbuilder));
		}

		setInviewMarchs(currentSet);
	}

	/** 登录同步*/
	private void sync() {
		// //# 每日普通货车发车次数
		// optional int32 truckNumber = 1;
		// //# 联盟每日普通列车发车次数
		// optional int32 allianceCommonTrainNumber = 2;
		// // # 联盟每日豪华列车发车次数
		// optional int32 allianceSpecialTrainNumber = 3;
		// // # 个人每日联盟列车参与次数
		// optional int32 trainNumber = 4;
		// // # 每日普通货车抢夺次数
		// optional int32 truckRobNumber = 5;
		// // # 每日联盟列车抢夺次数
		PBMTAcvityInfoSync.Builder builder = PBMTAcvityInfoSync.newBuilder();
		if (player.hasGuild()) {
			builder.setAllianceCommonTrainNumber(MeterialTransportService.getInstance().getGuildNum(player.getGuildId(), MTTruckType.GUILD));
			builder.setAllianceSpecialTrainNumber(MeterialTransportService.getInstance().getGuildNum(player.getGuildId(), MTTruckType.GUILDBIG));
		}
		MaterialTransportEntity entity = getDBEntity(player.getId());
		builder.setTruckNumber(entity.getNumber(NumberType.truckNumberMap));
		builder.setTruckRobNumber(entity.getNumber(NumberType.truckRobNumberMap));
		builder.setTrainNumber(entity.getNumber(NumberType.trainNumberMap));
		builder.setTrainRobNumber(entity.getNumber(NumberType.trainRobNumberMap));
		builder.setSpecialTrainNumber(entity.getNumber(NumberType.specialTrainNumberMap));
		builder.addAllServerlist(MeterialTransportService.getInstance().getGroupServer());

		sendProtocol(HawkProtocol.valueOf(HP.code2.MT_ACVITY_SYNC_VALUE, builder));
	}

	/** 历史记录*/
	@ProtocolHandler(code = HP.code2.MT_TRUCK_HISTORY_C_VALUE)
	private boolean onMT_TRUCK_HISTORY_C(HawkProtocol protocol) {
		PBMTHistoryReq req = protocol.parseProtocol(PBMTHistoryReq.getDefaultInstance());

		PBMTHistoryResp.Builder resp = PBMTHistoryResp.newBuilder();
		resp.setType(req.getType());
		for (String tid : MeterialTransportService.getInstance().getPlayerTruck().get(player.getId())) {
			MTTruck truck = MeterialTransportService.getInstance().getWorldTrucks().get(tid);
			if (truck == null) {
				continue;
			}
			if (req.getType() == 1 && truck.isMember(player.getId())) {
				resp.addTrucks(truck.toPBObj());
			}
			if (req.getType() == 2 && truck.isRober(player.getId())) {
				resp.addTrucks(truck.toPBObj());
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.MT_TRUCK_HISTORY_S, resp));
		player.responseSuccess(protocol.getType());
		return false;
	}

	/** 计数*/
	@ProtocolHandler(code = HP.code2.MT_ACVITY_SYNC_REQ_VALUE)
	private boolean onMT_ACVITY_SYNC_REQ(HawkProtocol protocol) {
		sync();
		return false;
	}

	/** 三个车厢*/
	@ProtocolHandler(code = HP.code2.MT_TRUCK_LIST_C_VALUE)
	private boolean onTruckList(HawkProtocol protocol) {

		syncTruckList();
		return false;
	}

	/** 获取单个车*/
	@ProtocolHandler(code = HP.code2.MT_Truck_INFO_C_VALUE)
	private boolean onMT_Truck_INFO_C(HawkProtocol protocol) {
		PBGuildTruckMassRobReq req = protocol.parseProtocol(PBGuildTruckMassRobReq.getDefaultInstance());
		MTTruck truck = MeterialTransportService.getInstance().getTruck(req.getTruckId());

		sendProtocol(HawkProtocol.valueOf(HP.code2.MT_Truck_INFO_S, truck.toPBObj().toBuilder()));
		return false;
	}

	/** 刷新他人车 PBMTOtherTruckRefrashReq  MT_TRUCK_LIST_S*/
	@ProtocolHandler(code = HP.code2.MT_TRUCK_REFRESH_REQ_VALUE)
	private boolean onTruckRefreshList(HawkProtocol protocol) {
		long now = HawkTime.getMillisecond();
		if (now - lastRefresh < 1000) {
			return false;
		}
		lastRefresh = now;

		PBMTOtherTruckRefrashReq req = protocol.parseProtocol(PBMTOtherTruckRefrashReq.getDefaultInstance());
		int type = req.getType();
		otherTruckrefresh(type);

		syncTruckList();
		return false;
	}

	private void otherTruckrefresh(int type) {
		int max = 8;

		List<MTTruck> allTrucks = new ArrayList<>(MeterialTransportService.getInstance().getMarchTrucks());
		Collections.shuffle(allTrucks);
		boolean hasguildTruck = false;
		List<MTTruck> refreshList = new ArrayList<>();
		for (MTTruck truck : allTrucks) {
			if (type == 1 && GlobalData.getInstance().isLocalServer(truck.getServerId())) {
				continue;
			}
			if (truck.getState() != MTTruckState.MARCH) {
				continue;
			}
			if (player.getId().equals(truck.getPlayerId())) {
				continue;
			}
			if (GuildService.getInstance().isInTheSameGuild(player.getId(), truck.getPlayerId())) {
				continue;
			}
			if (hasguildTruck && truck.getType() != MTTruckType.SINGLE) {
				continue;
			}
			if (truck.getType() != MTTruckType.SINGLE && max >= 2) {
				hasguildTruck = true;
				refreshList.add(truck);
				max -= 2;
			} else if (truck.getType() == MTTruckType.SINGLE && max >= 1) {
				refreshList.add(truck);
				max -= 1;
			}

			if (0 >= max) {
				break;
			}
		}
		this.refreshList = refreshList;
	}

	private void syncTruckList() {
		if (refreshList.isEmpty()) {
			otherTruckrefresh(1);
		}

		PBMTTruckListResp.Builder resp = PBMTTruckListResp.newBuilder();

		for (MTTruck truck : refreshList) {
			try {
				resp.addOtherTrucks(truck.toPBObj());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		MTTruck truck1 = getTruck(TRUCK1);
		if (truck1 != null) {
			try {
				resp.addSelfTrucks(truck1.toPBObj());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		MTTruck truck2 = getTruck(TRUCK2);
		if (truck2 != null) {
			try {
				resp.addSelfTrucks(truck2.toPBObj());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		MTTruck guildTruck = MeterialTransportService.getInstance().getGuildTruckPre(player.getGuildId());
		if (guildTruck != null && guildTruck.getState() == MTTruckState.PRE) {
			try {
				resp.setGuildTruck(guildTruck.toPBObj());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		Collection<MTTruckRob> robs = MeterialTransportService.getInstance().getGuildMass(player.getGuildId());
		for (MTTruckRob rob : robs) {
			try {
				resp.addMassMarch(rob.toPBObj());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		sendProtocol(HawkProtocol.valueOf(HP.code2.MT_TRUCK_LIST_S, resp));
	}

	/** 单个列车变更*/
	private void notifyTruckUpdate(MTTruck truck) {
		truck.setPbObj(null);
		sendProtocol(HawkProtocol.valueOf(HP.code2.MT_TRUCK_UPDATE_VALUE, truck.toPBObj().toBuilder()));

	}

	/**预设行军*/
	private void sendPreMarch(String playerId) {
		PBTRUCKPreMarchResp.Builder resp = PBTRUCKPreMarchResp.newBuilder();

		final String key = MTPreMarch + playerId;
		List<byte[]> dataList = RedisProxy.getInstance().getRedisSession().hmGet(key.getBytes(), (MTMarchIndex.YACHE.getNumber() + "").getBytes(),
				(MTMarchIndex.LUEDUO.getNumber() + "").getBytes());
		for (byte[] data : dataList) {
			try {
				if (data != null) {
					PresetMarchInfo info = PresetMarchInfo.newBuilder().mergeFrom(data).build();
					if (info.getIdx() == MTMarchIndex.LUEDUO.getNumber()) {
						resp.setCaijiMarch(info);
					} else {
						resp.setQuganMarch(info);
					}
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.MT_TRUCK_PREMARCH_SYNC, resp));
	}

	/** 创建联盟车*/
	@ProtocolHandler(code = HP.code2.MT_GUILD_TRUCK_CREATE_C_VALUE)
	private boolean onGuildfMarchCreate(HawkProtocol protocol) {
		for(MTTruck truck : MeterialTransportService.getInstance().getMarchTrucks()){
			if(truck.getType()!= MTTruckType.SINGLE && truck.getGuildId().equals(player.getGuildId()) && truck.isMember(player.getId())){
				sendError(protocol.getType(), 5283612);
				return false;
			}
		}
		
		
		PBGuildTruckCreateReq req = protocol.parseProtocol(PBGuildTruckCreateReq.getDefaultInstance());
		if (!player.hasGuild()) {
			return false;
		}
		Optional<PresetMarchInfo> preSetInfo = getPresetInfo(player.getId(), MTMarchIndex.YACHE);
		if (!preSetInfo.isPresent()) { // 没有预设行军, 不能出征
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_PRE_MARCH_VALUE);
			return false;
		}
		MTTruckType type = req.getType() == 2 ? MTTruckType.GUILD : MTTruckType.GUILDBIG;
		HawkTuple2<NumberType, Integer> trainNumberMap = MeterialTransportService.getInstance().getTrainNumberMap(type);
		if (type == MTTruckType.GUILDBIG && getDBEntity(player.getId()).getNumber(trainNumberMap.first) >= trainNumberMap.second) {
			sendError(protocol.getType(), Status.MTTError.MT_GUILD_AWARD_MAX_VALUE);
			return false;
		}
		int maxLimit = type == MTTruckType.GUILD ? MaterialTransportKVCfg.getInstance().getAllianceCommonTrainNumber()
				: MaterialTransportKVCfg.getInstance().getAllianceSpecialTrainNumber();

		if (MeterialTransportService.getInstance().getGuildNum(player.getGuildId(), type) >= maxLimit) {
			sendError(protocol.getType(), Status.MTTError.MT_GUILD_CREATE_MAX_VALUE);
			return false;
		}
		String trainReward = req.getType() == 2 ? MaterialTransportKVCfg.getInstance().getCommonTrainReward() : MaterialTransportKVCfg.getInstance().getSpecialTrainReward();
		String guildId = player.getGuildId();
		synchronized (guildId) {
			MTTruck truck = MeterialTransportService.getInstance().getGuildTruckPre().get(guildId);
			if (truck != null && truck.getState() == MTTruckState.PRE) {
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.MT_GUILD_TRUCK_CREATE_S, truck.toPBObj().toBuilder()));
				return false;
			}

			if (type == MTTruckType.GUILDBIG) {
				ConsumeItems consumeItems = ConsumeItems.valueOf();
				consumeItems.addConsumeInfo(ItemInfo.valueListOf(MaterialTransportKVCfg.getInstance().getSpecialTrainCost()), false);
				if (!consumeItems.checkConsume(player, protocol.getType())) {
					return false;
				}
				consumeItems.consumeAndPush(player, Action.MT_SPECIAL_TRAIN);
			}

			truck = new MTTruck();
			truck.setTrainReward(trainReward);
			truck.setPlayerId(player.getId());
			truck.setType(type);
			truck.setServerId(GsConfig.getInstance().getServerId());
			truck.setGuildId(guildId);
			truck.setGuildName(player.getGuildName());
			long now = HawkTime.getMillisecond();
			truck.setStartTime(now + MaterialTransportKVCfg.getInstance().getTrainPrepareTime());
			truck.setEndTime(now + MaterialTransportKVCfg.getInstance().getTrainPrepareTime() + MeterialTransportService.getInstance().getNeedTime(truck));

			MTMember member = MTMember.valueOf(player, MeterialTransportService.getInstance().getBattleData(MTMarchIndex.YACHE, player.getId()));

			truck.setLeader(member);

			for (int index = 1; index <= 4; index++) {
				MTTruckGroup chexiang = new MTTruckGroup();
				chexiang.setIndex(index);
				chexiang.refreshAward(truck.getType());
				chexiang.setRefreshCnt(0);
				truck.getCompartments().add(chexiang);
			}

			MeterialTransportService.getInstance().getGuildTruckPre().put(guildId, truck);
			MeterialTransportService.getInstance().incGuildNumber(player.getGuildId(), type);
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.MT_GUILD_TRUCK_CREATE_S, truck.toPBObj().toBuilder()));
			player.responseSuccess(protocol.getType());
			shareToGuild(truck, NoticeCfgId.MT_TRUCK_CREATE_SHARE);
			
			LogUtil.logMaterialTransport(player, 1, truck);
			return true;
		}
	}

	/** 联盟车指定车长*/
	@ProtocolHandler(code = HP.code2.MT_GUILD_TRUCK_HEAD_REQ_VALUE)
	private boolean onGuildMarchLeader(HawkProtocol protocol) {
		PBGuildTruckLeaderReq req = protocol.parseProtocol(PBGuildTruckLeaderReq.getDefaultInstance());
		final String leaderId = req.getPlayerId();
		
		for(MTTruck truck : MeterialTransportService.getInstance().getMarchTrucks()){
			if(truck.getType()!= MTTruckType.SINGLE && truck.getGuildId().equals(player.getGuildId()) && truck.isMember(leaderId)){
				sendError(protocol.getType(), 5283612);
				return false;
			}
		}
		
		if (!GuildService.getInstance().isInTheSameGuild(player.getId(), leaderId)) {
			sendError(protocol.getType(), Status.Error.GUILD_NOT_SAME_VALUE);
			return false;
		}

		Optional<PresetMarchInfo> preSetInfo = getPresetInfo(leaderId, MTMarchIndex.YACHE);
		if (!preSetInfo.isPresent()) { // 没有预设行军, 不能出征
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_PRE_MARCH_VALUE);
			return false;
		}

		MTTruck truck = MeterialTransportService.getInstance().getGuildTruckPre().get(player.getGuildId());
		if (truck == null || truck.getState() != MTTruckState.PRE || truck.getType() != MTTruckType.GUILD) {
			return false;
		}
//		HawkTuple2<NumberType, Integer> trainNumberMap = MeterialTransportService.getInstance().getTrainNumberMap(truck.getType());
//		if (getDBEntity(leaderId).getNumber(trainNumberMap.first) >= trainNumberMap.second) {
//			sendError(protocol.getType(), Status.MTTError.MT_GUILD_AWARD_MAX_VALUE);
//			return false;
//		}
//		if (!Objects.equals(player.getId(), truck.getLeader().getPlayerId())) { // 已指定车长, 不可变更
//			sendError(protocol.getType(), Status.MTTError.MT_HEAD_ONE_VALUE);
//			return false;
//		}

		Player leader = GlobalData.getInstance().makesurePlayer(leaderId);

		MTMember member = MTMember.valueOf(leader, MeterialTransportService.getInstance().getBattleData(MTMarchIndex.YACHE, player.getId()));
		truck.setLeader(member);
		notifyTruckUpdate(truck);
		player.responseSuccess(protocol.getType());
		LogUtil.logMaterialTransport(player, 2, truck);
		return true;
	}

	/** 联盟排队车厢*/
	@ProtocolHandler(code = HP.code2.MT_GUILD_TRUCK_PAIDUI_REQ_VALUE)
	private boolean onGuildMarchPaiDui(HawkProtocol protocol) {
		for(MTTruck truck : MeterialTransportService.getInstance().getMarchTrucks()){
			if(truck.getType()!= MTTruckType.SINGLE && truck.getGuildId().equals(player.getGuildId()) && truck.isMember(player.getId())){
				sendError(protocol.getType(), 5283612);
				return false;
			}
		}
		
		PBGuildTruckPaiDuiReq req = protocol.parseProtocol(PBGuildTruckPaiDuiReq.getDefaultInstance());
		final int cmIndex = req.getGroupIndex();

		Optional<PresetMarchInfo> preSetInfo = getPresetInfo(player.getId(), MTMarchIndex.YACHE);
		if (!preSetInfo.isPresent()) { // 没有预设行军, 不能出征
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_PRE_MARCH_VALUE);
			return false;
		}

		MTTruck truck = MeterialTransportService.getInstance().getGuildTruckPre().get(player.getGuildId());
		if (truck.getLeader().getPlayerId().equals(player.getId())) {
			return false;
		}

		if (truck == null || truck.getState() != MTTruckState.PRE) {
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_PRE_MARCH_VALUE);
			return false;
		}
		MTTruckGroup tarcompartment = truck.getCompartment(cmIndex);
		if (tarcompartment.getMemberList().size() >= MaterialTransportKVCfg.getInstance().getTrainCarryNumber()) {
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_NUMBER_VALUE);
			return false;
		}

		for (MTTruckGroup compartment : truck.getCompartments()) {
			MTMember old = null;
			for (MTMember member : compartment.getMemberList()) {
				if (member.getPlayerId().equals(player.getId())) {
					old = member;
				}
			}
			if (old != null) {
				compartment.getMemberList().remove(old);
			}
		}

		tarcompartment.getMemberList().add(MTMember.valueOf(player, MeterialTransportService.getInstance().getBattleData(MTMarchIndex.YACHE, player.getId())));

		notifyTruckUpdate(truck);

		player.responseSuccess(protocol.getType());
		LogUtil.logMaterialTransport(player, 3, truck);
		return true;
	}

	/** 联盟车刷新车厢奖励*/
	@ProtocolHandler(code = HP.code2.MT_GUILD_TRUCK_REFRESH_REQ_VALUE)
	private boolean onGuildMarchRefrerashReward(HawkProtocol protocol) {
		PBGuildTruckRefreshReq req = protocol.parseProtocol(PBGuildTruckRefreshReq.getDefaultInstance());
		int index = req.getIndex();
		MTTruck truck = MeterialTransportService.getInstance().getGuildTruckPre().get(player.getGuildId());
		if (truck == null || truck.getState() != MTTruckState.PRE || !Objects.equals(truck.getId(), req.getTruckId())) {
			return false;
		}

		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(MaterialTransportKVCfg.getInstance().getTrainRefreshCost()), false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return false;
		}
		consumeItems.consumeAndPush(player, Action.MT_TRAIN_REFRESH);

		MTTruckGroup compartment = truck.getCompartment(index);
		compartment.refreshAward(truck.getType());

		notifyTruckUpdate(truck);
		player.responseSuccess(protocol.getType());
		LogUtil.logMaterialTransport(player, 6, truck);
		return true;
	}

	/** 发个联盟车*/
	@ProtocolHandler(code = HP.code2.MT_GUILD_TRUCK_START_REQ_VALUE)
	private boolean onGuildMarchStart(HawkProtocol protocol) {
		// // PresetMarchInfo req = protocol.parseProtocol(PresetMarchInfo.getDefaultInstance());
		// // final int index = 1;// TODO
		// MTTruck truck = MeterialTransportService.getInstance().getGuildTruckPre().get(player.getGuildId());
		// if (truck == null || truck.getState() != MTTruckState.PRE) {
		// return false;
		// }
		//
		// long now = HawkTime.getMillisecond();
		// truck.setState(MTTruckState.MARCH);
		// truck.setStartTime(now);
		// truck.setEndTime(now + 3600000);
		//
		// MeterialTransportService.getInstance().addOrUpdateTruck(truck);
		// notifyTruckUpdate(truck);
		// player.responseSuccess(protocol.getType());

		return true;
	}

	/** 集结*/
	@ProtocolHandler(code = HP.code2.MT_GUILD_TRUCK_ROB_C_VALUE)
	private boolean onGuildTruckRob(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			return false;
		}
		PBChampionPlayer battleData = MeterialTransportService.getInstance().getBattleData(MTMarchIndex.LUEDUO, player.getId());
		if (battleData == null) {
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_PRE_MARCH_VALUE);
			return false;
		}

		PBGuildTruckMassRobReq req = protocol.parseProtocol(PBGuildTruckMassRobReq.getDefaultInstance());
		String truckId = req.getTruckId();

		MTTruck truck = MeterialTransportService.getInstance().getTruck(truckId);
		final String guildId = player.getGuildId();
		if (truck.getGuildId().equals(guildId) || truck.getState() == MTTruckState.REACHED) {
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_MARCHEND_VALUE);
			return false;
		}

		Collection<MTTruckRob> robList = MeterialTransportService.getInstance().getGuildMass(guildId);

		MaterialTransportKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(MaterialTransportKVCfg.class);
		if (robList.size() >= kvCfg.getAllianceRobMaxLimit()) {
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_ROB_LIMIT);
			return false;
		}

		MTTruckRob rob = new MTTruckRob();
		rob.setGuildId(guildId);
		rob.setTarTruckId(truckId);
		rob.setTarTruck(truck);
		long now = HawkTime.getMillisecond();
		rob.setStartTime(now);
		rob.setEndTime(now + MaterialTransportKVCfg.getInstance().getAllianceRobTime());
		MeterialTransportService.getInstance().recordGuildMass(rob);

		rob.getMass().add(MTMember.valueOf(player, battleData));

		player.sendProtocol(HawkProtocol.valueOf(HP.code2.MT_GUILD_TRUCK_ROB_S, rob.toPBObj().toBuilder()));
		player.responseSuccess(protocol.getType());
		return true;
	}

	/** 加入集结*/
	@ProtocolHandler(code = HP.code2.MT_GUILD_TRUCK_ROB_JOIN_C_VALUE)
	private boolean onGuildTruckRobJoin(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			return false;
		}
		PBChampionPlayer battleData = MeterialTransportService.getInstance().getBattleData(MTMarchIndex.LUEDUO, player.getId());
		if (battleData == null) {
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_PRE_MARCH_VALUE);
			return false;
		}

		PBGuildTruckMassRobReq req = protocol.parseProtocol(PBGuildTruckMassRobReq.getDefaultInstance());
		String robId = req.getRobId();

		Collection<MTTruckRob> robList = MeterialTransportService.getInstance().getGuildMass(player.getGuildId());

		MTTruckRob rob = robList.stream().filter(r -> r.getId().equals(robId)).findAny().orElse(null);
		MaterialTransportKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(MaterialTransportKVCfg.class);
		if (robList.size() >= kvCfg.getAllianceRobMaxLimit() && rob == null) {
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_MARCHEND_VALUE);
			return false;
		}

		if (rob == null) {
			return false;
		}
		boolean hasJoin = rob.getMass().stream().filter(r -> r.getPlayerId().equals(player.getId())).findAny().isPresent();
		if (!hasJoin && rob.getMass().size() >= 9) {
			sendError(protocol.getType(), Status.MTTError.MT_ROB_NOM_LIMIT_VALUE);
			return false;
		}

		if (!hasJoin) {
			rob.getMass().add(MTMember.valueOf(player, battleData));
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code2.MT_GUILD_TRUCK_ROB_S, rob.toPBObj().toBuilder()));
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**准备中的列车 可刷奖励/发出 */
	private MTTruck getPreTruck(int index) {
		MTTruck result = pretruck1;
		if (index == TRUCK2) {
			result = pretruck2;
		}
		return result;
	}

	/**取得当前准备中, 或行军中的列车*/
	private MTTruck getTruck(int index) {
		Collection<String> tlist = MeterialTransportService.getInstance().getPlayerTruck().get(player.getId());
		for (String id : tlist) {
			MTTruck truck = MeterialTransportService.getInstance().getTruck(id);
			if (truck != null
					&& truck.getType() == MTTruckType.SINGLE
					&& truck.getState() == MTTruckState.MARCH
					&& truck.getIndex() == index
					&& truck.getPlayerId().equals(player.getId())) {
				return truck;
			}
		}
		return getPreTruck(index);
	}

	/** 创建个人车*/
	@ProtocolHandler(code = HP.code2.MT_SELF_TRUCK_CREATE_C_VALUE)
	private boolean onSelfMarchCreate(HawkProtocol protocol) {
		Optional<PresetMarchInfo> preSetInfo = getPresetInfo(player.getId(), MTMarchIndex.YACHE);
		if (!preSetInfo.isPresent()) { // 没有预设行军, 不能出征
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_PRE_MARCH_VALUE);
			return false;
		}
		PBSelfTruckCreateReq req = protocol.parseProtocol(PBSelfTruckCreateReq.getDefaultInstance());
		final int index = req.getIndex();
		if (getPreTruck(index) != null) {
			return false;
		}

		MTTruck truck = new MTTruck();
		truck.setIndex(index);
		truck.setType(MTTruckType.SINGLE);
		truck.setPlayerId(player.getId());
		truck.setServerId(player.getServerId());
		if (player.hasGuild()) {
			truck.setGuildId(player.getGuildId());
			truck.setGuildName(player.getGuildName());
		}

		MTMember member = MTMember.valueOf(player, MeterialTransportService.getInstance().getBattleData(MTMarchIndex.YACHE, player.getId()));
		truck.setLeader(member);

		MTTruckGroup ment = new MTTruckGroup();
		ment.setIndex(1); // 个人车只有一个车厢. 奖励都给车头
		ment.refreshAward(truck.getType());
		ment.setRefreshCnt(0);
		truck.getCompartments().add(ment);

		if (index == 1) {
			pretruck1 = truck;
		} else {
			pretruck2 = truck;
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code2.MT_SELF_TRUCK_CREATE_S, truck.toPBObj().toBuilder()));

		player.responseSuccess(protocol.getType());
		
		LogUtil.logMaterialTransport(player, 1, truck);
		return true;
	}

	/** 刷新奖励*/
	@ProtocolHandler(code = HP.code2.MT_SELF_TRUCK_REFRESH_REQ_VALUE)
	private boolean onSelfRefresh(HawkProtocol protocol) {
		PBSelfTruckRefreshReq req = protocol.parseProtocol(PBSelfTruckRefreshReq.getDefaultInstance());
		final int index = req.getIndex();
		if (getPreTruck(index) == null) {
			return false;
		}

		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(MaterialTransportKVCfg.getInstance().getTruckRefreshCost()), false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return false;
		}
		consumeItems.consumeAndPush(player, Action.MT_TRUCK_REFRESH);

		MTTruck truck = getPreTruck(index);
		truck.getCompartment(1).refreshAward(MTTruckType.SINGLE);

		notifyTruckUpdate(truck);
		player.responseSuccess(protocol.getType());
		LogUtil.logMaterialTransport(player, 6, truck);
		return true;
	}

	/** 发个人车*/
	@ProtocolHandler(code = HP.code2.MT_SELF_TRUCK_START_REQ_VALUE)
	private boolean onSelfMarchStart(HawkProtocol protocol) {
		if (getDBEntity(player.getId()).getNumber(NumberType.truckNumberMap) >= MaterialTransportKVCfg.getInstance().getTruckNumber()) {
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_ROBOVER_VALUE);
			return false;
		}
		Optional<PresetMarchInfo> preSetInfo = getPresetInfo(player.getId(), MTMarchIndex.YACHE);
		if (!preSetInfo.isPresent()) { // 没有预设行军, 不能出征
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_PRE_MARCH_VALUE);
			return false;
		}
		PBSelfTruckStartReq req = protocol.parseProtocol(PBSelfTruckStartReq.getDefaultInstance());
		final int index = req.getIndex();

		MTTruck truck = getPreTruck(index);
		if (truck.getState() != MTTruckState.PRE) {
			return false;
		}

		if (index == 1) { // 已发, 清除预备车
			pretruck1 = null;
		}
		if (index == 2) {
			pretruck2 = null;
		}

		truck.setOrigionX(player.getPosXY()[0]);
		truck.setOrigionY(player.getPosXY()[1]);
		int[] tarpos = MeterialTransportService.getInstance().farestSuperWeapon(player.getPosXY());
		truck.setTerminalX(tarpos[0]);
		truck.setTerminalY(tarpos[1]);

		truck.setState(MTTruckState.MARCH);
		long now = HawkTime.getMillisecond();
		truck.setStartTime(now);
		truck.setEndTime(now + MeterialTransportService.getInstance().getNeedTime(truck));

		MeterialTransportService.getInstance().addOrUpdateTruck(truck);
		notifyTruckUpdate(truck);

		MarchEventSync.Builder addbuilder = MarchEventSync.newBuilder();
		addbuilder.setEventType(MarchEvent.MARCH_ADD_VALUE);
		WorldMarchRelation relation = truck.getRelation(player);
		MarchData.Builder dataBuilder = MarchData.newBuilder();
		dataBuilder.setMarchId(truck.getMarchId());
		dataBuilder.setMarchPB(truck.toBuilder(relation));
		addbuilder.addMarchData(dataBuilder);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_EVENT_SYNC_VALUE, addbuilder));

		player.responseSuccess(protocol.getType());
		getDBEntity(player.getId()).incNumber(NumberType.truckNumberMap);
		return true;
	}

	/** 抢夺单人车*/
	@ProtocolHandler(code = HP.code2.MT_SELF_TRUCK_ROB_C_VALUE)
	private boolean onAttackTruck(HawkProtocol protocol) {
		if (getDBEntity(player.getId()).getNumber(NumberType.truckRobNumberMap) >= MaterialTransportKVCfg.getInstance().getTruckRobNumber()) {
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_ROB_LIMIT_VALUE);
			return false;
		}
		PBSelfTruckRobReq req = protocol.parseProtocol(PBSelfTruckRobReq.getDefaultInstance());
		final String truckId = req.getTruckId();
		MTTruck truck = MeterialTransportService.getInstance().getTruck(truckId);
		if (truck == null) {
			return false;
		}
		if (truck.getState() != MTTruckState.MARCH || truck.getType() != MTTruckType.SINGLE) {
			sendError(protocol.getType(), Status.MTTError.MT_TRUCK_MARCHEND_VALUE);
			return false;
		}
		// if (truck.getRobCnt() >= MaterialTransportKVCfg.getInstance().getTruckRobbedNumber()) {
		// sendError(protocol.getType(), Status.MTTError.MT_TRUCK_ROBOVER_VALUE);
		// return false;
		// }

		MTTruckRob rob = new MTTruckRob();
		rob.setEndTime(HawkTime.getMillisecond());
		rob.setTarTruckId(truckId);
		rob.setTarTruck(truck);
		rob.getMass().add(MTMember.valueOf(player, MeterialTransportService.getInstance().getBattleData(MTMarchIndex.LUEDUO, player.getId())));
		MTTruckBattleRecord battleRecord = MeterialTransportService.getInstance().truckRob(rob);

		notifyTruckUpdate(truck);
		Builder resp = PBSelfTruckRobResp.newBuilder().setBattleRecord(battleRecord.toPBObj()).setTarget(truckId);
		sendProtocol(HawkProtocol.valueOf(HP.code2.MT_SELF_TRUCK_ROB_S, resp));

		if (battleRecord.isWin()) {
			sync();
		}

		player.responseSuccess(protocol.getType());
		return true;
	}

	/** 编辑预设队列*/
	@ProtocolHandler(code = HP.code2.MT_TRUCK_PREMARCH_SET_REQ_VALUE)
	private boolean onMarchPresetInfo(HawkProtocol protocol) {
		PresetMarchInfo req = protocol.parseProtocol(PresetMarchInfo.getDefaultInstance());
		if (req.getIdx() != MTMarchIndex.LUEDUO.getNumber() && req.getIdx() != MTMarchIndex.YACHE.getNumber()) {
			return false;
		}
		if (req.getSuperSoldierId() > 0) {
			if (player.getSuperSoldierByCfgId(req.getSuperSoldierId()) == null) {
				return false;
			}
		}
		if (req.getHeroIdsCount() > 2) { // 验证英雄不能超过2个
			return false;
		}
		if (req.getArmourSuit() == ArmourSuitType.ARMOUR_NONE) {
			req = req.toBuilder().setArmourSuit(ArmourSuitType.valueOf(player.getEntity().getArmourSuit())).build();
		}
		// 检测皮肤信息
		PlayerMarchModule marckModule = player.getModule(ModuleType.WORLD_MARCH_MODULE);
		if (!marckModule.checkMarchDressReq(req.getMarchDressList())) {
			return false;
		}
		for (PlayerHero hero : player.getHeroByCfgId(req.getHeroIdsList())) { // 两英雄互斥
			if (req.getHeroIdsList().contains(hero.getConfig().getProhibitedHero())) {
				return false;
			}
		}

		if (req.getSuperLab() != 0 && !player.isSuperLabActive(req.getSuperLab())) {
			return false;
		}
		final String key = MTPreMarch + player.getId();
		RedisProxy.getInstance().getRedisSession().hSetBytes(key, req.getIdx() + "", req.toByteArray());
		saveBattlePlayer(req, MTMarchIndex.valueOf(req.getIdx()));

		// 如果只预设其1 , 把另一条也设定好
		MTMarchIndex otherIndex = req.getIdx() == MTMarchIndex.LUEDUO.getNumber() ? MTMarchIndex.YACHE : MTMarchIndex.LUEDUO;
		Optional<PresetMarchInfo> other = getPresetInfo(player.getId(), otherIndex);
		if (!other.isPresent()) {
			RedisProxy.getInstance().getRedisSession().hSetBytes(key, otherIndex.getNumber() + "", req.toBuilder().setIdx(otherIndex.getNumber()).build().toByteArray());
			saveBattlePlayer(req.toBuilder().setIdx(otherIndex.getNumber()).build(), otherIndex);
		}

		sendPreMarch(player.getId());
		player.responseSuccess(protocol.getType());
		player.getData().getCommanderEntity().setMtpremarch(1);
		return true;
	}

	private Optional<PresetMarchInfo> getPresetInfo(String playerId, MTMarchIndex idx) {
		final String key = MTPreMarch + playerId;
		byte[] bytes = RedisProxy.getInstance().getRedisSession().hGetBytes(key, idx.getNumber() + "");
		if (bytes == null) {
			return Optional.empty();
		}
		PresetMarchInfo info = null;
		try {
			info = PresetMarchInfo.newBuilder().mergeFrom(bytes).build();
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Optional.ofNullable(info);
	}

	/**
	 * 查看采集行军队列
	 */
	@ProtocolHandler(code = HP.code2.MT_TRUCK_PREMARCH_REQ_VALUE)
	private void onLookPreMarch(HawkProtocol protocol) {
		// MTTruckPreMarchReq req = protocol.parseProtocol(MTTruckPreMarchReq.getDefaultInstance());
		sendPreMarch(player.getId());
	}

	/**
	 * 保存出战数据. 把请求接口放到PlayermarchModule里边. 就可以用原来行军的checkMarchReq方法了.
	 */
	private int saveBattlePlayer(PresetMarchInfo req, MTMarchIndex index) {

		double battlePoint = 0;
		List<Integer> heroIdList = req.getHeroIdsList();
		int superSoldierId = req.getSuperSoldierId();
		PBChampionPlayer.Builder data = PBChampionPlayer.newBuilder();
		data.setPlayerInfo(BuilderUtil.buildSnapshotData(player));
		int totalCnt = 0;
		for (ArmySoldierPB pbarmy : req.getArmyList()) {
			PBChampionSoldier.Builder army = PBChampionSoldier.newBuilder().setArmyId(pbarmy.getArmyId()).setCount(pbarmy.getCount())
					.setStar(player.getSoldierStar(pbarmy.getArmyId()))
					.setPlantStep(player.getSoldierStep(pbarmy.getArmyId()))
					.setPlantSkillLevel(player.getSoldierPlantSkillLevel(pbarmy.getArmyId()))
					.setPlantMilitaryLevel(player.getSoldierPlantMilitaryLevel(pbarmy.getArmyId()));
			data.addSoldiers(army);
			int armyId = pbarmy.getArmyId();
			int armyCnt = pbarmy.getCount();
			ArmyEntity armyEntity = player.getData().getArmyEntity(armyId);
			if (armyEntity == null || armyEntity.getTotal() < armyCnt) {
				return Status.Error.CHAMPIONSHIP_ARMY_CNT_NOT_ENOUGH_VALUE;
			}
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			battlePoint += 1d * armyCnt * cfg.getPower();
			totalCnt += armyCnt;
		}

		if (totalCnt <= 0) {
			return Status.Error.CHAMPIONSHIP_ARMY_EMPTY_VALUE;
		}

		for (int heroId : heroIdList) {
			Optional<PlayerHero> heroOp = player.getHeroByCfgId(heroId);
			if (heroOp.isPresent()) {
				// 检查英雄出征
				if (heroIdList.contains(heroOp.get().getConfig().getProhibitedHero())) {
					return Status.Error.PLAYER_HERO_MARCH_TYPE_ERROR_VALUE;
				}
				data.addHeros(heroOp.get().toPBobj());
				battlePoint += heroOp.get().power();
			}
		}
		Optional<SuperSoldier> ssoldierOp = player.getSuperSoldierByCfgId(superSoldierId);
		if (ssoldierOp.isPresent()) {
			data.setSuperSoldier(ssoldierOp.get().toPBobj());
			battlePoint += ssoldierOp.get().power();
		}

		ArmourBriefInfo armour = player.genArmourBriefInfo(req.getArmourSuit());
		data.setArmourBrief(armour);

		for (EffType eff : EffType.values()) {
			EffectParams effectParams = buildMarch(player, req).getMarchEntity().getEffectParams();
			PBChampionEff pbef = PBChampionEff.newBuilder().setEffectId(eff.getNumber())
					.setValue(player.getEffect().getEffVal(eff, effectParams)).build();
			data.addEffs(pbef);
		}

		for (int dressId : req.getMarchDressList()) {
			data.addDressId(dressId);
		}
		data.setManhattanFuncUnlock(player.checkManhattanFuncUnlock());
		data.setManhattanInfo(player.buildManhattanInfo(req.getManhattan()));
		data.setMechacoreFuncUnlock(player.checkMechacoreFuncUnlock());
		data.setMechacoreInfo(player.buildMechacoreInfo(req.getMechacoreSuit()));

		MeterialTransportService.getInstance().updateBattleData(player.getId(), index, data.build());

		return Status.SysError.SUCCESS_OK_VALUE;

	}

	private TemporaryMarch buildMarch(Player player, PresetMarchInfo info) {
		TemporaryMarch atkMarch = new TemporaryMarch();
		List<ArmyInfo> armys = new ArrayList<>();
		int max = player.getMaxMarchSoldierNum(EffectParams.getDefaultVal());
		for (ArmySoldierPB arpb : info.getArmyList()) {
			int count = arpb.getCount();
			if (info.getPercentArmy()) {
				count = (int) (1D * max / 1000000 * count) + 1;
			}
			armys.add(new ArmyInfo(arpb.getArmyId(), count));
		}

		WorldMarch march = atkMarch.getMarchEntity();
		march.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_VALUE);
		march.setTargetId("");
		march.setMarchId(player.getId());
		march.setPlayerId(player.getId());
		march.setPlayerName(player.getName());
		march.setMarchType(WorldMarchType.COLLECT_RES_TREASURE_VALUE);
		march.setArmourSuit(info.getArmourSuit().getNumber());
		march.setMechacoreSuit(info.getMechacoreSuit().getNumber());
		march.setHeroIdList(info.getHeroIdsList());
		march.setSuperSoldierId(info.getSuperSoldierId());
		march.setTalentType(info.getTalentType().getNumber());
		march.setSuperLab(info.getSuperLab());
		march.setManhattanAtkSwId(info.getManhattan().getManhattanAtkSwId());
		march.setManhattanDefSwId(info.getManhattan().getManhattanDefSwId());
		atkMarch.setArmys(armys);
		atkMarch.setPlayer(player);
		atkMarch.setHeros(player.getHeroByCfgId(info.getHeroIdsList()));
		march.setDressList(info.getMarchDressList());
		return atkMarch;
	}

	private MaterialTransportEntity getDBEntity(String playerId) {
		return MeterialTransportService.getInstance().getDBEntity(playerId);
	}

	public MarchSet getInviewMarchs() {
		return inviewMarchs;
	}

	public void setInviewMarchs(MarchSet inviewMarchs) {
		this.inviewMarchs = inviewMarchs;
	}

}

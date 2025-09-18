package com.hawk.game.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.task.HawkTaskManager;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.SimulateWarConstCfg;
import com.hawk.game.config.SimulateWarEncourageCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.msg.GuildJoinMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.GuildAuthority;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.SimulateWar.PBSimulateWarBattleData;
import com.hawk.game.protocol.SimulateWar.PBSimulateWarEff;
import com.hawk.game.protocol.SimulateWar.PBSimulateWarSoldier;
import com.hawk.game.protocol.SimulateWar.SimulateWarActivityState;
import com.hawk.game.protocol.SimulateWar.SimulateWarAdjustMarchReq;
import com.hawk.game.protocol.SimulateWar.SimulateWarAdjustWayReq;
import com.hawk.game.protocol.SimulateWar.SimulateWarBattleRecordReq;
import com.hawk.game.protocol.SimulateWar.SimulateWarDissolveReq;
import com.hawk.game.protocol.SimulateWar.SimulateWarOrderAdjustReq;
import com.hawk.game.protocol.SimulateWar.SimulateWarSignUpReq;
import com.hawk.game.protocol.SimulateWar.SimulateWarWayPlayersReq;
import com.hawk.game.protocol.SimulateWar.WayType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.simulatewar.SimulateWarService;
import com.hawk.game.service.simulatewar.data.SimulateWarActivityData;
import com.hawk.game.service.simulatewar.data.SimulateWarGuildData;
import com.hawk.game.service.simulatewar.data.SimulateWarPlayerExt;
import com.hawk.game.service.simulatewar.msg.SimulateWarAdjustWayMsg;
import com.hawk.game.service.simulatewar.msg.SimulateWarDissolveMsg;
import com.hawk.game.service.simulatewar.msg.SimulateWarEncourageMsg;
import com.hawk.game.service.simulatewar.msg.SimulateWarOrderAdjustMsg;
import com.hawk.game.service.simulatewar.msg.SimulateWarSaveMarchMsg;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.ModuleType;
import com.hawk.log.Action;

public class PlayerSimulateWarModule extends PlayerModule {

	public PlayerSimulateWarModule(Player player) {
		super(player);
	}
	
	@Override
	public boolean onPlayerLogin() {
		SimulateWarService.getInstance().syncPageInfo(player);
		//SimulateWarService.getInstance().synPlayerAllMarchInfo(player);
		
		return true;
	}
	@ProtocolHandler(code = HP.code.SIMULATE_WAR_PAGE_INFO_REQ_VALUE)
	private void onSimulateWarPageInfoReq(HawkProtocol hawkProtocol) {
		SimulateWarService.getInstance().syncPageInfo(player);
	}

	@ProtocolHandler(code = HP.code.SIMULATE_WAR_ENCOUREAGE_INFO_REQ_VALUE)
	private void onSimulateWarEncourageInfoReq(HawkProtocol protocol) {
		// 玩家没有公会
		if (!player.hasGuild()) {
			this.sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN_VALUE);

			return;
		}

		/*// 非管理期间不能助威.
		if (SimulateWarService.getInstance().getActivityInfo().getState() != SimulateWarActivityState.SW_MANAGE) {
			this.sendError(protocol.getType(), Status.Error.SIMULATE_WAR_STATE_NOT_ALLOW_OPERATION_VALUE);

			return;
		}*/

		SimulateWarService.getInstance().synEncourageInfo(player);
	}

	@ProtocolHandler(code = HP.code.SIMULATE_WAR_ENCOURAGE_REQ_VALUE)
	private void onSimulateWarEncourageReq(HawkProtocol protocol) {
		// 玩家没有公会
		if (!player.hasGuild()) {
			this.sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN_VALUE);

			return;
		}
		SimulateWarService warService = SimulateWarService.getInstance();
		// 非管理期间不能助威.
		if (warService.getActivityInfo().getState() != SimulateWarActivityState.SW_MANAGE) {
			this.sendError(protocol.getType(), Status.Error.SIMULATE_WAR_STATE_NOT_ALLOW_OPERATION_VALUE);

			return;
		}

		SimulateWarGuildData guildData = warService.getGuildDat(player.getGuildId());
		if (guildData == null) {
			this.sendError(protocol.getType(), Status.Error.SIMULATE_WAR_GUILD_NOT_JOIN_VALUE);

			return;
		}

		SimulateWarConstCfg constCfg = SimulateWarConstCfg.getInstance();
		if (guildData.getEncourageTimes() >= constCfg.getMaxEncourageTimes()) {
			this.sendError(protocol.getType(), Status.Error.SIMULATE_WAR_ENCOURAGE_MAX_VALUE);
			SimulateWarService.getInstance().synEncourageInfo(player);

			return;
		}

		SimulateWarPlayerExt playerExt = RedisProxy.getInstance()
				.getSimulateWarPlayerExt(warService.getActivityInfo().getTermId(), player.getId());
		SimulateWarEncourageCfg encourageCfg = HawkConfigManager.getInstance()
				.getConfigByKey(SimulateWarEncourageCfg.class, (playerExt.getEncourageTimes() + 1));
		if (encourageCfg == null) {
			this.sendError(protocol.getType(), Status.Error.SIMULATE_WAR_ENCOURAGE_MAX_VALUE);

			return;
		}

		ConsumeItems consumes = ConsumeItems.valueOf();
		consumes.addConsumeInfo(encourageCfg.getCostList());
		if (!consumes.checkConsume(player, protocol.getType())) {
			return;
		}
		consumes.consumeAndPush(player, Action.SIMULATE_WAR_ENCOURAGE);
		playerExt.setEncourageTimes(playerExt.getEncourageTimes() + 1);
		RedisProxy.getInstance().addOrUpdateSimulateWarPlayerExt(warService.getActivityInfo().getTermId(),
				player.getId(), playerExt);
		warService.synEncourgeInfo(player, playerExt.getEncourageTimes(), guildData.getEncourageTimes() + 1);
		HawkTaskManager.getInstance().postMsg(warService.getXid(), new SimulateWarEncourageMsg(player.getGuildId()));
		
		Player.logger.info("encourge playerId:{}, afterTimes:{}", player.getId(), playerExt.getEncourageTimes());
		
        LogUtil.logSimulateWarEncourage(player, warService.getTermId(), player.getGuildId());
        
		player.responseSuccess(protocol.getType());
	}

	@ProtocolHandler(code = HP.code.SIMULATE_WAR_SIGN_UP_REQ_VALUE)
	private void onSimulateWarSignUp(HawkProtocol protocol) {		
		SimulateWarSignUpReq req = protocol.parseProtocol(SimulateWarSignUpReq.getDefaultInstance());
		WorldMarchReq marchReq = req.getMarchInfo();
		WayType wayType = req.getWay();
		Player.logger.info("playerId:{}, sign up wayType:{}", player.getId(), wayType);
		PBSimulateWarBattleData.Builder wayBattleData = PBSimulateWarBattleData.newBuilder();
		Map<Integer, Integer> armyMap = new HashMap<>();
		int result = buildBattleData(wayBattleData, marchReq, armyMap);
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			wayBattleData.setWay(wayType);
			SimulateWarSaveMarchMsg msg = new SimulateWarSaveMarchMsg(player, wayBattleData, armyMap);
			HawkTaskManager.getInstance().postMsg(SimulateWarService.getInstance().getXid(), msg);
		} else {
			this.sendError(protocol.getType(), result);
		}
	}

	@ProtocolHandler(code = HP.code.SIMULATE_WAR_ADJUST_MARCH_REQ_VALUE)
	private void onSimulateWarAdjustMarchReq(HawkProtocol protocol) {		
		SimulateWarAdjustMarchReq req = protocol.parseProtocol(SimulateWarAdjustMarchReq.getDefaultInstance());
		WorldMarchReq marchReq = req.getMarchInfo();
		Player.logger.info("playerId:{}, adjust march:{}", player.getId(), req.getId());
		PBSimulateWarBattleData.Builder wayBattleData = PBSimulateWarBattleData.newBuilder();
		Map<Integer, Integer> armyMap = new HashMap<>();
		int result = buildBattleData(wayBattleData, marchReq, armyMap);
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			wayBattleData.setId(req.getId());
			SimulateWarSaveMarchMsg msg = new SimulateWarSaveMarchMsg(player, wayBattleData, armyMap);
			HawkTaskManager.getInstance().postMsg(SimulateWarService.getInstance().getXid(), msg);
		} else {
			this.sendError(protocol.getType(), result);
		}
	}

	private int buildBattleData(PBSimulateWarBattleData.Builder wayBattleData, WorldMarchReq marchReq,
			Map<Integer, Integer> armyMap) {
		int totalCnt = 0;
		double battlePoint = 0;
		SimulateWarActivityData activityInfo = SimulateWarService.getInstance().getActivityInfo();
		// 这里就检测一下兵的数量.
		if (activityInfo.getState() != SimulateWarActivityState.SW_SIGN_UP) {
			return Status.Error.SIMULATE_WAR_STATE_NOT_SIGN_VALUE;
		}

		// 大本等级限制.
		SimulateWarConstCfg constCfg = SimulateWarConstCfg.getInstance();
		if (player.getCityLv() < constCfg.getCityLvlLimit()) {
			return Status.Error.CITY_LEVEL_NOT_ENOUGH_VALUE;
		}
		for (ArmySoldierPB pbarmy : marchReq.getArmyInfoList()) {
			PBSimulateWarSoldier.Builder army = PBSimulateWarSoldier.newBuilder().setArmyId(pbarmy.getArmyId())
					.setCount(pbarmy.getCount()).setStar(player.getSoldierStar(pbarmy.getArmyId()))
					.setPlantStep(player.getSoldierStep(pbarmy.getArmyId()))
					.setPlantSkillLevel(player.getSoldierPlantSkillLevel(pbarmy.getArmyId()))
					.setPlantMilitaryLevel(player.getSoldierPlantMilitaryLevel(pbarmy.getArmyId()));
			
			wayBattleData.addSoldiers(army);
			int armyId = pbarmy.getArmyId();
			int armyCnt = pbarmy.getCount();
			ArmyEntity armyEntity = player.getData().getArmyEntity(armyId);
			if (armyEntity == null || armyEntity.getTotal() < armyCnt) {
				return Status.Error.SIMULATE_WAR_ARMY_CNT_NOT_ENOUGH_VALUE;
			}

			armyMap.put(armyId, armyEntity.getTotal());
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			battlePoint += 1l * armyCnt * cfg.getPower();
			totalCnt += armyCnt;
		}

		if (totalCnt <= 0) {
			return Status.Error.SIMULATE_WAR_ARMY_EMPTY_VALUE;
		}			

		for (int heroId : marchReq.getHeroIdList()) {
			Optional<PlayerHero> heroOp = player.getHeroByCfgId(heroId);
			if (heroOp.isPresent()) {
				// 检查英雄出征
				if (marchReq.getHeroIdList().contains(heroOp.get().getConfig().getProhibitedHero())) {
					return Status.Error.PLAYER_HERO_MARCH_TYPE_ERROR_VALUE;
				}
				wayBattleData.addHeros(heroOp.get().toPBobj());
				battlePoint += heroOp.get().power();
			} else {
				return Status.Error.SIMULATE_WAR_MARCH_HERO_NOT_EXIST_VALUE;
			}
		}
		if (marchReq.getSuperSoldierId() > 0) {
			Optional<SuperSoldier> ssoldierOp = player.getSuperSoldierByCfgId(marchReq.getSuperSoldierId());
			if (ssoldierOp.isPresent()) {
				wayBattleData.setSuperSoldier(ssoldierOp.get().toPBobj());
				battlePoint += ssoldierOp.get().power();
			} else {
				return Status.Error.SIMULATE_WAR_MARCH_SUPERSOLDIER_NOT_EXIST_VALUE;
			}
		}		
		
		if (totalCnt > player.getMaxMarchSoldierNum(new EffectParams(marchReq, new ArrayList<>()))) {
			return Status.Error.WORLD_MARCH_ARMY_TOTALCOUNT_VALUE;
		}

		ArmourBriefInfo armour = player.genArmourBriefInfo(marchReq.getArmourSuit());
		if (armour == null) {
			return Status.Error.SIMULATE_WAR_ARMOURBRIEF_NOT_EXITS_VALUE;
		}
		wayBattleData.setArmourBrief(armour);

		for (EffType eff : EffType.values()) {
			int value = player.getEffect().getEffVal(eff, new EffectParams(marchReq, new ArrayList<>()));
			if (value <= 0) {
				continue;
			}
			PBSimulateWarEff pbef = PBSimulateWarEff.newBuilder().setEffectId(eff.getNumber())
					.setValue(value).build();
			wayBattleData.addEffs(pbef);
		}

		PlayerMarchModule module = player.getModule(ModuleType.WORLD_MARCH_MODULE);
		if (module.checkMarchDressReq(marchReq.getMarchDressList())) {
			for (int dressId : marchReq.getMarchDressList()) {
				wayBattleData.addDressId(dressId);
			}
		}
		
		wayBattleData.setBattleValue((int) Math.ceil(battlePoint));
		wayBattleData.setManhattanFuncUnlock(player.checkManhattanFuncUnlock());
		wayBattleData.setManhattanInfo(player.buildManhattanInfo(marchReq.getManhattan()));
		wayBattleData.setMechacoreFuncUnlock(player.checkMechacoreFuncUnlock());
		wayBattleData.setMechacoreInfo(player.buildMechacoreInfo(marchReq.getMechacoreSuit()));
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 拉取玩家的三路出兵消息.
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.SIMULATE_WAR_ALL_MARCH_REQ_VALUE)
	private void onSimulateWarAllMarchReq(HawkProtocol protocol) {
		SimulateWarService.getInstance().synPlayerAllMarchInfo(player);
	}
	
	@ProtocolHandler(code = HP.code.SIMULATE_WAR_DISSOLVE_REQ_VALUE)
	private void onSimulateWarDissolveReq(HawkProtocol protocol) {
		SimulateWarDissolveReq req = protocol.parseProtocol(SimulateWarDissolveReq.getDefaultInstance());
		String marchId = req.getId();
		SimulateWarService warService = SimulateWarService.getInstance();
		if (warService.getActivityInfo().getState() != SimulateWarActivityState.SW_SIGN_UP) {
			this.sendError(protocol.getType(), Status.Error.SIMULATE_WAR_STATE_NOT_SIGN_VALUE);
			
			return;
		}
		
		Player.logger.info("playerId:{}, dissolve marchId:{}", player.getId(), marchId);
		
		SimulateWarDissolveMsg dissolveMsg = new SimulateWarDissolveMsg(player, marchId);
		HawkTaskManager.getInstance().postMsg(warService.getXid(), dissolveMsg);
	}
	
	@ProtocolHandler(code = HP.code.SIMULATE_WAR_ADJUST_WAY_REQ_VALUE)
	private void onSimulateWarWayAdjustReq(HawkProtocol protocol) {
		SimulateWarAdjustWayReq req = protocol.parseProtocol(SimulateWarAdjustWayReq.getDefaultInstance());
		SimulateWarService warService = SimulateWarService.getInstance(); 
		if (warService.getActivityInfo().getState() != SimulateWarActivityState.SW_SIGN_UP) {
			this.sendError(protocol.getType(), Status.Error.SIMULATE_WAR_STATE_NOT_SIGN_VALUE);
			
			return;
		}
		
		Player.logger.info("playerId:{}, adjust way marchId:{}, newWay:{}", player.getId(), req.getId(), req.getWay());
		
		SimulateWarAdjustWayMsg msg = new SimulateWarAdjustWayMsg(player, req.getWay(), req.getId());
		HawkTaskManager.getInstance().postMsg(warService.getXid(), msg);
	}
	
	@ProtocolHandler(code = HP.code.SIMULATE_WAR_ORDER_ADJUST_REQ_VALUE)
	private void onSimulateWarOrderAdjustReq(HawkProtocol protocol) {		
		SimulateWarOrderAdjustReq req = protocol.parseProtocol(SimulateWarOrderAdjustReq.getDefaultInstance());
		List<String> idList = req.getIdListList();
		WayType wayType = req.getWayType();
		String guildId = player.getGuildId();
		
		Player.logger.info("playerId:{}, order adjust way:{}", player.getId(), wayType);
		
		int authorityLv = GuildService.getInstance().getPlayerGuildAuthority(player.getId());
		//联盟权限不够.
		if (authorityLv <= GuildAuthority.L4_VALUE) {
			this.sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			
			return;
		}
		
		//列表不为空.
		if (CollectionUtils.isEmpty(idList)) {
			this.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			
			return;
		}
		
		Set<String> setSize = new HashSet<>(idList);
		if (setSize.size() != idList.size()) {
			this.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			
			return;
		}
		LogUtil.logSimulateWarAdjust(player, SimulateWarService.getInstance().getTermId(), guildId);
		SimulateWarOrderAdjustMsg msg = new SimulateWarOrderAdjustMsg(player, guildId, wayType, idList);
		HawkTaskManager.getInstance().postMsg(SimulateWarService.getInstance().getXid(), msg);
	}
	
	@ProtocolHandler(code = HP.code.SIMULATE_WAR_WAY_PLAYERS_REQ_VALUE)
	private void onSimulateWarWayMarchReq(HawkProtocol protocol) {
		SimulateWarWayPlayersReq req = protocol.parseProtocol(SimulateWarWayPlayersReq.getDefaultInstance());
		SimulateWarService warService = SimulateWarService.getInstance();
		warService.synSimulateWarWayPlayerReq(player, player.getGuildId(), req.getWay());
	}
	
	@ProtocolHandler(code = HP.code.SIMULATE_WAR_BATTLE_RECORD_REQ_VALUE)
	private void onSimulateWarBattleRecordReq(HawkProtocol hawkProtocol) {
		SimulateWarBattleRecordReq req = hawkProtocol.parseProtocol(SimulateWarBattleRecordReq.getDefaultInstance());
		if (!player.hasGuild()) {
			this.sendError(hawkProtocol.getType(), Status.Error.GUILD_NO_JOIN_VALUE);
			
			return;
		}
		
		SimulateWarService.getInstance().synBattleRecord(player, player.getGuildId(), req.getWay());
	}
	
	@MessageHandler
	private boolean onGuildJoinMsg(GuildJoinMsg msg) {
		SimulateWarService.getInstance().syncPageInfo(player);
		
		return true;
	}
	
}
 
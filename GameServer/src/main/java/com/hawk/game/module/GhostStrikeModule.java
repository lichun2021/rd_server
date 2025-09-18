package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.hawk.game.battle.BattleLogHelper;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst.BattleType;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.GhostStrikeCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.msg.WorldMoveCityMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.hero.NPCHero;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.protocol.Counterattack.PBGhostStrikeMarch;
import com.hawk.game.protocol.GhostStrike.GhostMarchReq;
import com.hawk.game.protocol.GuildAssistant.AssistanceCallbackNotifyPB;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.AttackMarchReportPB;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.StoryMissionService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventDefenceGhostStrike;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;

public class GhostStrikeModule extends PlayerModule {

	TemporaryMarch ghostMarch;
	int ghostStrikeCfgId;

	public GhostStrikeModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		if (Objects.nonNull(ghostMarch) && ghostMarch.getEndTime() < HawkTime.getMillisecond()) {
			try {
				this.gostMarchReach();
			} finally {
				ghostMarch = null;
				ghostStrikeCfgId = 0;
			}
		}
		return true;
	}

	/**
	 * 请求开始npc行军.
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GHOST_STRIKE_MARCH_START_VALUE)
	private void onGhostMarchStart(HawkProtocol protocol) {
		if (Objects.nonNull(ghostMarch)) {
			player.responseSuccess(protocol.getType());
			return;
		}

		GhostMarchReq req = protocol.parseProtocol(GhostMarchReq.getDefaultInstance());

		final int taskId = req.getTaskId();
		final Integer cfgId = StoryMissionService.getInstance().getMissionTaskTarget(player, taskId); // GhostStrikeCfg的id
		if(Objects.isNull(cfgId)){
			HawkLog.errPrintln("no GhostStrikeCfg get taskId ={}", taskId);
			return;
		}
																									
		GhostStrikeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GhostStrikeCfg.class, cfgId);
		if (Objects.isNull(cfg)) {
			return;
		}
		
		final int origionId = WorldPointService.getInstance().createGhostStrikePoint(player.getId(), cfg.getEnemyDistance()); // 行军起点

		boolean isTaskComplete = StoryMissionService.getInstance().checkTaskComplete(player, taskId);
		if (isTaskComplete) {
			return;
		}

		// 构建行军
		long now = HawkTime.getMillisecond();
		NpcPlayer ghostplayer = new NpcPlayer(HawkXID.nullXid());
		ghostplayer.setPlayerId(cfgId + "");
		ghostplayer.setName(cfgId + "");
		ghostplayer.setPlayerPos(origionId);
		NPCHero hero = NPCHeroFactory.getInstance().get(cfg.getEnemyHero());
		Set<String> viewerIds = new HashSet<>(1);
		viewerIds.add(player.getId());
		TemporaryMarch asmarch = new TemporaryMarch();
		asmarch.setOrigionId(ghostplayer.getPlayerPos());
		asmarch.setTerminalId(player.getPlayerPos());
		asmarch.setPlayer(ghostplayer);
		asmarch.setStartTime(now);
		asmarch.setEndTime(now + TimeUnit.SECONDS.toMillis(cfg.getEnemyTime()));
		asmarch.setMarchId(ghostplayer.getId());
		asmarch.setMarchType(WorldMarchType.GHOST_STRIKE); // *新的行军类型. 记得告诉做行军的
		asmarch.setViewerIds(viewerIds);
		asmarch.setArmys(cfg.getEnemyList());
		if (Objects.nonNull(hero)) {
			asmarch.setHeros(Arrays.asList(hero));
		}

		ghostMarch = asmarch;
		ghostStrikeCfgId = cfgId;

		player.responseSuccess(protocol.getType());
		syncMarch();
	}

	public void syncMarch() {
		PBGhostStrikeMarch.Builder resp = PBGhostStrikeMarch.newBuilder();
		if (Objects.nonNull(ghostMarch)) {
			// 同步客户端
			AttackMarchReportPB.Builder attReportBuilder = ghostMarch.assembleEnemyMarchInfo(player, Collections.emptySet());
			attReportBuilder.setMarchStartTime(ghostMarch.getStartTime());
			attReportBuilder.setArrivalTime(ghostMarch.getEndTime());
			attReportBuilder.setOriginalX(ghostMarch.getOrigionX());
			attReportBuilder.setOriginalY(ghostMarch.getOrigionY());
			resp.addXingjun(ghostMarch.toBuilder());
			resp.addLeida(attReportBuilder);

		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GHOST_STRIKE_MARCH_SYNC, resp));
	}

	private void gostMarchReach() {

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		defPlayers.add(player);

		// 防守方援军
		Set<IWorldMarch> helpMarchList = getDefMarch4War(player, defPlayers);
		// 防守方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();
		for (IWorldMarch iWorldMarch : helpMarchList) {
			defMarchs.add(iWorldMarch);
		}
		PveBattleIncome battleIncome = BattleService.getInstance().initGhostMarchBattleData(BattleType.GHOST_MARCH, player.getPlayerPos(), ghostMarch, defPlayers, defMarchs);
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		// 防守方剩余兵力
		Map<String, List<ArmyInfo>> defArmyLeftMap = battleOutcome.getAftArmyMapDef();

		// 防守战斗结算
		boolean defWin = !battleOutcome.isAtkWin();

		// 记录战斗日志
		BattleLogHelper battleLogHelper = new BattleLogHelper(battleIncome, battleOutcome, !defWin);
		battleLogHelper.logBattleFlow();
		
		List<ArmyInfo> armyList = battleOutcome.getBattleArmyMapDef().get(player.getId());
		ArmyService.getInstance().onArmyBack(player, armyList, Collections.emptyList(),0, null);
		
		// 发送邮件---战斗
		FightMailService.getInstance().sendPveFightMail(BattleType.GHOST_MARCH, battleIncome, battleOutcome, null);
		
		// 更新援助防御玩家行军的部队
		updateDefMarchAfterWar(new ArrayList<>(helpMarchList), defArmyLeftMap);
		
		if (defWin) {
			GhostStrikeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GhostStrikeCfg.class, ghostStrikeCfgId);
			MissionManager.getInstance().postMsg(player, new EventDefenceGhostStrike(defWin, cfg.getId()));
		}
		else{
			CityManager.getInstance().cityOnFire(null, player, Collections.emptyList());
		}
		
		WorldMarchService.getInstance().sendBattleResultInfo(ghostMarch, !defWin, ghostMarch.getArmys(), Collections.emptyList(), false);

		ghostMarch = null;
		ghostStrikeCfgId = 0;
	}
	
	private Set<IWorldMarch> getDefMarch4War(Player defPlayer, List<Player> defPlayers) {
		Set<IWorldMarch> helpMarchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(defPlayer.getId(), WorldMarchType.ASSISTANCE_VALUE,
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		for (IWorldMarch march : helpMarchList) {
			defPlayers.add(GlobalData.getInstance().makesurePlayer(march.getPlayerId()));
		}
		return helpMarchList;
	}
	
	private void updateDefMarchAfterWar(List<IWorldMarch> helpMarchList, Map<String, List<ArmyInfo>> defArmyMap) {
		// 更新援助防御玩家行军的部队
		if (helpMarchList != null && helpMarchList.size() > 0) {
			for (IWorldMarch tmpMarch : helpMarchList) {
				List<ArmyInfo> leftList = defArmyMap.get(tmpMarch.getPlayerId());
				if (WorldUtil.calcSoldierCnt(leftList) > 0) {
					WorldMarchService.getInstance().updateMarchArmy(tmpMarch, leftList);
					continue;
				}
				
				if (tmpMarch.getMarchType() == WorldMarchType.ASSISTANCE) {
					AssistanceCallbackNotifyPB.Builder callbackNotifyPB = AssistanceCallbackNotifyPB.newBuilder();
					callbackNotifyPB.setMarchId(tmpMarch.getMarchId());
					Player assistPlayer = GlobalData.getInstance().makesurePlayer(tmpMarch.getMarchEntity().getTargetId());
					assistPlayer.sendProtocol(HawkProtocol.valueOf(HP.code.ASSISTANCE_MARCH_CALLBACK, callbackNotifyPB));
				}
				
				// 死光了，行军立即送死兵回家
				WorldMarchService.getInstance().onMarchReturnImmediately(tmpMarch, leftList);
				
			}
		}
	}
	
	@MessageHandler
	private void onWorldMoveCityMsg(WorldMoveCityMsg msg) {
		ghostMarch = null;
		ghostStrikeCfgId = 0;
		syncMarch();
	}

	@Override
	protected boolean onPlayerLogin() {
		if (Objects.nonNull(ghostMarch)) {
			syncMarch();
		}

		return super.onPlayerLogin();
	}

}

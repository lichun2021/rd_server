package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.GhostTowerCfg;
import com.hawk.game.invoker.GhostTowerMonsterAttackInvoker;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 幽灵工厂打怪行军
 * @author che
 */
public class AttackGhostMarch extends PlayerMarch implements BasedMarch {

	private boolean isWin;
	
	private static int attackWin = 1;
	private static int attackFail = 2;
	private static int attackBack = 3;
	public AttackGhostMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.GHOST_TOWER_MARCH;
	}

	@Override
	public void onMarchReach(Player player) {
		// 行军
		WorldMarch march = getMarchEntity();
		// 目标点
		int terminalId = march.getTerminalId();
		// 目标野怪
		int ghostId = Integer.valueOf(march.getTargetId());
		// 点和怪信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(terminalId);

		// 点为空
		if (point == null || point.getPointType() != WorldPointType.GHOST_TOWER_MONSTER_VALUE) {
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.GHOST_TOWER_MONSTER_ATTACK_FAILED_CHANGED)
					.addContents(ghostId)
					.build());
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			WorldMarchService.logger.error("attack monster march reach error, point null, terminalId:{}", terminalId);
			return;
		}
		// 野怪配置
		GhostTowerCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(GhostTowerCfg.class, ghostId);
		if (monsterCfg == null) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			WorldMarchService.logger.error("attack ghost march reach error, GhostTowerCfg null, monsterId:{}", ghostId);
			return;
		}
		// 组织战斗数据
		List<Player> atkPlayers = new ArrayList<>();
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);
		atkPlayers.add(player);
		// 战斗
		PveBattleIncome battleIncome = BattleService.getInstance().initTowerGhostData(BattleConst.BattleType.ATTACK_GOHOST_TOWER, point, atkPlayers, atkMarchs);
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		// 战斗结果
		this.isWin = battleOutcome.isAtkWin();
		List<ArmyInfo> afterArmyList = WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk());
		// 发送战斗结果
		WorldMarchService.getInstance().sendBattleResultInfo(this, this.isWin, afterArmyList, new ArrayList<ArmyInfo>(), isWin);
		// 行军返回
		WorldMarchService.getInstance().onMarchReturn(this, WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk()), 0);
		// 结果处理
		doAtkMonsterResult(point, atkPlayers.get(0),battleIncome,battleOutcome,monsterCfg);
		// 刷新战力
//		refreshPowerAfterWar(atkPlayers, null);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_REACH_FIGHT_MONSTER, Params.valueOf("marchData", march),
				Params.valueOf("atkLeftArmyList", afterArmyList), Params.valueOf("isWin", isWin));
		// 日志
		WorldMarchService.logger.info("world attack monster isWin: {}, playerId: {}, atkLeftArmyList: {}", isWin, player.getId(), afterArmyList);
		
	
		
	}
	
	@Override
	public void onWorldMarchReturn(Player player) {
		
	}
	
	/**
	 * 攻打野怪战后数据处理
	 * @param point
	 * @param player
	 * @param battleOutcome
	 * @param heroId
	 * @param monsterCfg
	 */
	public void doAtkMonsterResult(WorldPoint point, Player player, PveBattleIncome battleIncome,BattleOutcome battleOutcome,GhostTowerCfg ghostTowerCfg) {
		boolean isWin = battleOutcome.isAtkWin();
		WorldMarch march = getMarchEntity();
		final int ghostId = Integer.valueOf(march.getTargetId()); 
		if (isWin){
			//删除世界点
			WorldPointService.getInstance().removeWorldPoint(point.getId(), false);
			player.dealMsg(MsgId.GHOST_TOWER_MONSTER_KILLED, new GhostTowerMonsterAttackInvoker(player,ghostId));
			LogUtil.logGhostTowerAttack(player, ghostId, attackWin);
		}else{
			LogUtil.logGhostTowerAttack(player, ghostId, attackFail);
		}
		
		//发放战报
		FightMailService.getInstance().sendPveFightMail(BattleConst.BattleType.ATTACK_GOHOST_TOWER, battleIncome, battleOutcome, null);
	}

	
	@Override
	public long getMarchNeedTime() {
		WorldMarch march = getMarchEntity();
		int ghostId = Integer.valueOf(march.getTargetId());
		GhostTowerCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(GhostTowerCfg.class, ghostId);
		if (monsterCfg != null && monsterCfg.getMarchTime() > 0) {
			return monsterCfg.getMarchTime() * 1000;
		}
		return super.getMarchNeedTime();
	}
	
	@Override
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		WorldMarch march = getMarchEntity();
		final int ghostId = Integer.valueOf(march.getTargetId());
		Player player = this.getPlayer();
		if(player != null){
			LogUtil.logGhostTowerAttack(player, ghostId, attackBack);
		}
	}
	
	
}

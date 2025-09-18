package com.hawk.game.invoker;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.IBattleIncome;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ScoreType;

/**
 * 战斗结束后, 兵的处理函数
 * @author zhenyu.shang
 * @since 2018年1月2日
 */
public class PlayerArmyBackMsgInvoker extends HawkMsgInvoker {
	
	private Player player;
	
	private BattleOutcome battleOutcome;
	
	private IBattleIncome battleIncome;
	
	private Map<String,long[]> grabResAry;
	
	private int pointType;
	
	private boolean needSendMail;
	
	private boolean isMassMarch;
	
	private WorldMarchType marchType;
	
	public PlayerArmyBackMsgInvoker(int pointType, Player player, BattleOutcome battleOutcome, IBattleIncome battleIncome, Map<String,long[]> grabResAry, boolean needSendMail, boolean isMassMarch, WorldMarchType marchType) {
		this.pointType = pointType;
		this.player = player;
		this.battleIncome = battleIncome;
		this.battleOutcome = battleOutcome;
		this.grabResAry = grabResAry;
		this.needSendMail = needSendMail;
		this.isMassMarch = isMassMarch;
		this.marchType = marchType;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		List<ArmyInfo> armyList = battleOutcome.getBattleArmyMapDef().get(player.getId());
		long powerBef = player.getPower();
		calDefCitySkill10303Wound(player, armyList);
		ArmyService.getInstance().onArmyBack(player, armyList, Collections.emptyList(),0, null);
		long powerAft = player.getPower();
		
		if (needSendMail) {
			// 发送邮件---战斗
			FightMailService.getInstance().sendFightMail(pointType, battleIncome, battleOutcome, grabResAry);
			BattleService.getInstance().dealWithPvpBattleEvent(battleIncome, battleOutcome, isMassMarch, marchType);
			
			// 玩家损失战斗力超过30%, 成就上报
			int loseRate = (int) Math.floor(GsConst.EFF_RATE * (powerBef - powerAft) / powerBef);
			if (loseRate >= ScoreType.DEFNDER_POWER_LOSE.paramVal()) {
				if (player.isActiveOnline()) {
					GameUtil.scoreBatch(player, ScoreType.DEFNDER_POWER_LOSE, 1);
				} else if (GameUtil.isScoreBatchEnable(player)) {
					String[] playerIds = { player.getId() };
					int[] keys = { ScoreType.DEFNDER_POWER_LOSE.intValue() };
					String[] vals = { "1" };
					LocalRedis.getInstance().addScoreBatchFlag(playerIds, keys, vals);
				}
			}
		}
		
		player.setBeAttacked(0L);
		
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public BattleOutcome getBattleOutcome() {
		return battleOutcome;
	}

	public IBattleIncome getBattleIncome() {
		return battleIncome;
	}

	public Map<String,long[]> getGrabResAry() {
		return grabResAry;
	}

	public int getPointType() {
		return pointType;
	}
	
	private void calDefCitySkill10303Wound(Player defPlayer, List<ArmyInfo> armyList) {
		if (defPlayer.getEffect().getEffVal(EffType.CITY_ENEMY_MARCH_SPD) <= 0) {
			return;
		}

		int totalDead = 0;

		for (ArmyInfo info : armyList) {
			totalDead += info.getDeadCount();
			totalDead += info.getWoundedCount();
		}
		defPlayer.skill10303DurningDead += totalDead;
		
	}
	
}

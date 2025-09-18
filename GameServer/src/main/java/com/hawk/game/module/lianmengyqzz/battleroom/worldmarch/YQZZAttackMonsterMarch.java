package com.hawk.game.module.lianmengyqzz.battleroom.worldmarch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.invoker.MonsterAtkAwardMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.march.AutoMonsterMarchParam;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZMonsterCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.entity.YQZZMarchEntity;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZMonsterHonor;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZMonster;
import com.hawk.game.msg.AutoSearchMonsterMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Mail.MonsterMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.MailService;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.YuriMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class YQZZAttackMonsterMarch extends IYQZZWorldMarch {
	private BattleOutcome outcome;
	private int vitBack;
	
	public YQZZAttackMonsterMarch(IYQZZPlayer parent) {
		super(parent);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.ATTACK_MONSTER;
	}
	
	public void setVitBack(int vitBack) {
		this.vitBack = vitBack;
		this.getMarchEntity().setVitCost(vitBack);
	}

	@Override
	public void heartBeats() {
		// 当前时间
		long currTime = HawkApp.getInstance().getCurrentTime();
		// 行军或者回程时间未结束
		if (getMarchEntity().getEndTime() > currTime) {
			return;
		}
		// 行军返回到达
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			onMarchBack();
			return;
		}

		// 行军到达
		onMarchReach(getParent());
	}

	@Override
	public void onMarchStart() {
		super.onMarchStart();
	}

	@Override
	public void onMarchReach(Player player) {
		YQZZMarchEntity march = getMarchEntity();
		IYQZZWorldPoint ttt = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalId()).orElse(null);
		if (ttt == null || !(ttt instanceof YQZZMonster)) {
			int[] xy = GameUtil.splitXAndY(getMarchEntity().getTerminalId());
			MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(getParent().getId()).setMailId(MailId.WORLD_NEW_MONSTER_POINT_CHANGED)
					.addContents(xy[0], xy[1])
					.addTips(march.getTargetId())
					.setDuntype(DungeonMailType.YQZZ);
			MailParames mparames = playerParamesBuilder.build();
			MailService.getInstance().sendMail(mparames);

			onMarchReturn(march.getTerminalId(), march.getOrigionId(), getArmys());
			return;
		}

		YQZZMonster monster = (YQZZMonster) ttt;
		// // 战斗结果
		BattleOutcome battleOutcome = doBattle();
		boolean isWin = battleOutcome.isAtkWin();
		// 发送战斗结果给前台播放动画
		this.sendBattleResultInfo(isWin, march.getArmys(), Collections.emptyList(), isWin);
		this.doAtkMonsterResult(monster, getParent(), battleOutcome, getMarchEntity().getHeroIdList(), monster.getWorldEnemyCfg(), false);
		// 泰伯利亚野怪击杀邮件 2021112401 四个参数，分别是1 坐标 2击杀个人积分 3击杀号令能量 4 联盟总号令能量
		// // 泰伯利亚野怪失败邮件 2021112402 1个参数，坐标
		// // 泰伯利亚野怪消失邮件 2021112403 1个参数，坐标
		// if (isWin) {
		// MailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.MONSTER_SUCC)
		// .addContents(monster.getX(), monster.getY(), monstercfg.getPlayerHonor())
		// .addTips(march.getTargetId()).setDuntype(DungeonMailType.YQZZ).build());
		//
		if (isWin) {
			getParent().getBase().changeNationTechValue(monster.getCfg().getOrderVal());
			monster.removeWorldPoint(); 
		} else if (getMarchEntity().getAutoMarchIdentify() > 0 || getParent().getKillMonster() >= getParent().getParent().getCfg().getMonsterLimit()) {
			// 自动打野失败了，关闭自动打野行军
			AutoMonsterMarchParam autoMarchParam = WorldMarchService.getInstance().getAutoMarchParam(player.getId());
			if (autoMarchParam != null) {
				autoMarchParam.removeAutoMarch(getMarchEntity().getAutoMarchIdentify());
				int remainCount = autoMarchParam.getAutoMarchCount();
				HawkLog.logPrintln("AtkMonsterAutoMarch broken, attack monster failed, playerId: {}, remainCount: {}", player.getId(), remainCount);
				if (remainCount == 0) {
					WorldMarchService.getInstance().breakAutoMarch(player, Status.Error.AUTO_ATK_MONSTER_PVE_BREAK_VALUE);
				}
			}
		}
		//Tlog
		LogUtil.logAttackMonster(player, monster.getX(), monster.getY(), monster.getWorldEnemyCfg().getType(), monster.getWorldEnemyCfg().getId(), monster.getWorldEnemyCfg().getLevel(), 1, 1, 0, 0, isWin, false, true);
		//
		// // if (getParent().getParent().worldMonsterCount() == 0) {
		// // ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.YQZZ_MONSTER_KILLALL).build();
		// // getParent().getParent().addWorldBroadcastMsg(parames);
		// // }
		// // LogUtil.logYQZZKillMonster(player, getParent().getParent().getId(), player.getGuildId(), player.getGuildName(), monster.getCfgId(), monstercfg.getGuildOrder());
		// } else {
		// MailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.MONSTER_FAILED)
		// .addContents(monster.getX(), monster.getY()).addTips(march.getTargetId()).setDuntype(DungeonMailType.YQZZ).build());
		// }

		// 行军返回
		onMarchReturn(march.getTerminalId(), march.getOrigionId(), battleOutcome.getAftArmyMapAtk().get(getParent().getId()));
		
	}

	public void doAtkMonsterResult(YQZZMonster monster, Player player, BattleOutcome battleOutcome, List<Integer> heroId, WorldEnemyCfg monsterCfg, boolean useSkill) {
		WorldPoint point = monster.getEntity();
		// 战斗是否胜利
		boolean isWin = battleOutcome.isAtkWin();

		if (getParent().getKillMonster() >= getParent().getParent().getCfg().getMonsterLimit()) {
			return;
		}
		
		if (!isWin) {
			MonsterMail.Builder mailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, heroId, monsterCfg.getId(), point, null, null, 0, 0, 0);
			YuriMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.MONSTER_FAILED)
					.addContents(mailBuilder).addTips(monsterCfg.getId()).setDuntype(DungeonMailType.YQZZ).build());
			return;
		}
		
		YQZZMonsterHonor mhonor = getParent().getMonsterHonorStat(monsterCfg.getId());
		mhonor.setKillCount(mhonor.getKillCount() + 1);
		mhonor.setPlayerHonor(mhonor.getPlayerHonor() + monster.getCfg().getPlayerScore());
		mhonor.setGuildHonor(mhonor.getGuildHonor() + monster.getCfg().getAllianceScore());
		mhonor.setNationHonor(mhonor.getNationHonor() + monster.getCfg().getNationScore());
		this.vitBack = 0;
		// 击杀奖励
		AwardItems killAward = AwardItems.valueOf();
		killAward.addAwards(monsterCfg.getKillAwards());

		// 击杀额外奖励 -- 376
		int extrAward376 = player.getEffect().getEffVal(EffType.MONSTER_EXTR_AWARD, getMarchEntity().getEffectParams()) / 10000;
		for (int i = 0; i < extrAward376; i++) {
			killAward.addAwards(monsterCfg.getKillAwards());
		}

		// 击杀额外随机奖励 -- 376
		int extrRandomAward376 = player.getEffect().getEffVal(EffType.MONSTER_EXTR_AWARD, getMarchEntity().getEffectParams()) % 10000;
		if (HawkRand.randInt(10000) < extrRandomAward376) {
			killAward.addAwards(monsterCfg.getKillAwards());
		}

		int extrRandomAward377 = player.getEffect().getEffVal(EffType.MONSTER_EXTR_AWARD_HERO, getMarchEntity().getEffectParams());
		if (HawkRand.randInt(10000) < extrRandomAward377) {
			int effect377LinkToAwardId = ConstProperty.getInstance().getEffect377LinkToAwardId();
			if (effect377LinkToAwardId != 0) {
				killAward.addAward(effect377LinkToAwardId);
			}
		}
		// 击杀额外随机奖励 -- 338
		int extrRandomAward338 = player.getEffect().getEffVal(EffType.EFF_338, getMarchEntity().getEffectParams()) % 10000;
		if (HawkRand.randInt(10000) < extrRandomAward338) {
			killAward.addAwards(monsterCfg.getKillAwards());
		}

		// 指挥官经验
		double exp500 = player.getEffect().getEffVal(EffType.PLAYER_EXP_PER, getMarchEntity().getEffectParams()) * GsConst.EFF_PER;
		double exp515 = player.getEffect().getEffVal(EffType.PLAYER_EXP_PER_MONSTER, getMarchEntity().getEffectParams()) * GsConst.EFF_PER;
		double exp337 = player.getEffect().getEffVal(EffType.EFF_337) * GsConst.EFF_PER;
		int addExp = (int) (monsterCfg.getCommanderExp() * (1 + exp500 + exp515 + exp337));
		killAward.appendAward(AwardItems.valueOf().addExp(addExp));

		// 首杀奖励
		AwardItems firstKillAward = AwardItems.valueOf();

		// eff:4022
		int buff = player.getEffect().getEffVal(EffType.KILL_MONSTER_AWARD_ADD, getMarchEntity().getEffectParams());
		if (useSkill) {
			buff = 0;
		}

		for (ItemInfo award : killAward.getAwardItems()) {
			int count = (int) (award.getCount() * (1 + buff * GsConst.EFF_PER));
			award.setCount(count);
		}

		// 能量探测器翻倍
		long buffEndTime = player.getData().getBuffEndTime(EffType.ATK_MONSTER_ENERGY_DETECTOR_VALUE);
		if (buffEndTime > HawkTime.getMillisecond()) {
			for (ItemInfo award : killAward.getAwardItems()) {
				boolean isEnergyDetectorTool = WorldMarchConstProperty.getInstance().isEnergyDetectorTool(award.getItemId());
				if (award.getItemType() != ItemType.TOOL || !isEnergyDetectorTool) {
					continue;
				}
				int energyDetectorMultipleEffect = WorldMarchConstProperty.getInstance().getEnergyDetectorMultipleEffect();
				award.setCount(award.getCount() * energyDetectorMultipleEffect);
			}
		}

		// 回归特权 eff:28102 奖励翻倍,多添加一次原始奖励和经验
//		int addVal = player.getEffect().getEffVal(EffType.BACK_PRIVILEGE_ATK_MONSTER_AWARD_DOUBLE_TIMES);
//		if (addVal > 0) {
//			int effTimes = RedisProxy.getInstance().effectTodayUsedTimes(
//					player.getId(), EffType.BACK_PRIVILEGE_ATK_MONSTER_AWARD_DOUBLE_TIMES);
//			if (effTimes < addVal) {
//				// 指挥官经验
//				killAward.appendAward(AwardItems.valueOf().addExp(monsterCfg.getCommanderExp()));
//				// 道具
//				killAward.addAwards(monsterCfg.getKillAwards());
//				RedisProxy.getInstance().effectTodayUseInc(player.getId(),
//						EffType.BACK_PRIVILEGE_ATK_MONSTER_AWARD_DOUBLE_TIMES);
//			}
//		}
		
		// 发邮件
		MonsterMail.Builder mailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, heroId, monsterCfg.getId(), point, killAward.getAwardItems(),
				firstKillAward.getAwardItems(), 0, 0, 0);
		YuriMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.MONSTER_SUCC)
				.addContents(mailBuilder).addTips(monsterCfg.getId()).setDuntype(DungeonMailType.YQZZ).build());

		// 投递发奖
		player.dealMsg(MsgId.ATTACK_MONSTER_AWARD, new MonsterAtkAwardMsgInvoker(player, monsterCfg.getId(), killAward, firstKillAward, AwardItems.valueOf(), 1));
	}

	private BattleOutcome doBattle() {
		if (Objects.nonNull(outcome)) {
			return outcome;
		}
		IYQZZWorldPoint ttt = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalId()).orElse(null);
		YQZZMonster monster = (YQZZMonster) ttt;
		YQZZMonsterCfg monstercfg = monster.getCfg();
		// 战斗
		List<Player> atkPlayers = new ArrayList<>();
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(this);
		atkPlayers.add(getParent());
		PveBattleIncome battleIncome = BattleService.getInstance().initMonsterBattleData(BattleConst.BattleType.ATTACK_MONSTER, monster.getPointId(), monstercfg.getMonsterId(),
				atkPlayers, atkMarchs);
		outcome = BattleService.getInstance().doBattle(battleIncome);
		outcome.setDuntype(DungeonMailType.YQZZ);
		return outcome;
	}

	@Override
	public void onMarchBack() {
		// 部队回城
		onArmyBack(getParent(), getMarchEntity().getArmys(), getMarchEntity().getHeroIdList(), getMarchEntity().getSuperSoldierId(), this);

		this.remove();
		
		if(this.vitBack > 0){
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItem(Const.ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, this.vitBack);
			awardItems.rewardTakeAffectAndPush(this.getPlayer(), Action.FIGHT_MONSTER);
		}
		
		// 发起自动打野行军消息
				checkAutoMarch();
	}
	
	/**
	 * 根据条件触发自动打野行军
	 */
	private void checkAutoMarch() {
		IYQZZPlayer player = getParent();
		if (player == null) {
			return;
		}

		AutoMonsterMarchParam autoMarchParam = WorldMarchService.getInstance().getAutoMarchParam(player.getId());
		if (autoMarchParam == null) {
			return;
		}

		int id = getMarchEntity().getAutoMarchIdentify();
		autoMarchParam.resetAutoMarchStatus(id);

		HawkTaskManager.getInstance().postMsg(player.getXid(), AutoSearchMonsterMsg.valueOf());
	}

}

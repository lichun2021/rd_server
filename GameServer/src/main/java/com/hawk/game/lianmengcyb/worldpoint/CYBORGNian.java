package com.hawk.game.lianmengcyb.worldpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst.BattleType;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.CYBORGNianCfg;
import com.hawk.game.config.WorldNianCfg;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.lianmengcyb.CYBORGGuildBaseInfo;
import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldmarch.ICYBORGWorldMarch;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.FightResult;
import com.hawk.game.protocol.Const.Result;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.FighteInfo;
import com.hawk.game.protocol.Mail.MonsterMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.HPBattleResultInfoSync;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 机甲
 * 
 * @author lwt
 * @date 2019年11月7日
 */
public class CYBORGNian implements ICYBORGWorldPoint {
	private final CYBORGBattleRoom parent;
	private int x;
	private int y;
	private int remainBlood;
	public static final int NIAN_ID = 4;

	public CYBORGNian(CYBORGBattleRoom parent) {
		this.parent = parent;
		setRemainBlood(getNianInitBlood(NIAN_ID));
	}

	@Override
	public void onMarchReach(ICYBORGWorldMarch leaderMarch) {
		// 进攻方玩家
		List<ICYBORGPlayer> atkPlayers = new ArrayList<>();
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		// 进攻方行军
		List<ICYBORGWorldMarch> atkMarchList = new ArrayList<>();
		atkMarchList.add(leaderMarch);
		atkMarchList.addAll(leaderMarch.getMassJoinMarchs(true));
		for (ICYBORGWorldMarch iWorldMarch : atkMarchList) {
			// 去程到达目标点，变成停留状态
			iWorldMarch.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE);
			iWorldMarch.getMarchEntity().setReachTime(leaderMarch.getMarchEntity().getEndTime());
			iWorldMarch.updateMarch();
			atkMarchs.add(iWorldMarch);
			atkPlayers.add(iWorldMarch.getParent());
		}

		// 防守方玩家
		WorldNianCfg nianCfg = HawkConfigManager.getInstance().getConfigByKey(WorldNianCfg.class, NIAN_ID);
		// // 高达血量
		// int beforeBlood = this.getRemainBlood();

		// 战斗数据输入
		PveBattleIncome battleIncome = BattleService.getInstance().initGundamBattleData(BattleType.ATTACK_GUNDAM_PVE, this.getPointId(), nianCfg.getId(), nianCfg.getArmyList(),
				atkMarchs);

		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setDuntype(DungeonMailType.CYBORG);
		/********************** 战斗数据组装及战斗 ***************************/

		// 战斗结果处理
		doBattleResult(leaderMarch, nianCfg, atkPlayers, battleOutcome);

		// 行军返回
		leaderMarch.onMarchReturn(this.getPointId(), leaderMarch.getParent().getPointId(), leaderMarch.getArmys());
		// 队员行军返回
		for (ICYBORGWorldMarch tmpMarch : leaderMarch.getMassJoinMarchs(true)) {
			tmpMarch.onMarchReturn(this.getPointId(), tmpMarch.getParent().getPointId(), tmpMarch.getArmys());
		}

		// 战斗胜利，移除点
		if (this.getRemainBlood() <= 0) {
			String guildTag = leaderMarch.getPlayer().getGuildTag();
			String guildName = leaderMarch.getPlayer().getGuildName();
			ChatParames parames = ChatParames.newBuilder().setChatType(Const.ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(Const.NoticeCfgId.CYBORG_NIAN_KILLED)
					.addParms(guildTag, guildName).build();
			getParent().addWorldBroadcastMsg(parames);

			this.removeWorldPoint();

		}

		if (this.getRemainBlood() > 0) {
			getParent().worldPointUpdate(this);
		}

	}

	/**
	 * 战斗结果处理(奖励、邮件处理)
	 */
	private void doBattleResult(ICYBORGWorldMarch leaderMarch, WorldNianCfg nianCfg, List<ICYBORGPlayer> atkPlayers, BattleOutcome battleOutcome) {
		// 结算前血量
		int beforeBlood = this.getRemainBlood();

		// 总击杀怪物数量(血量)
		int totalKillCount = getTotalKillCount(battleOutcome);

		// 计算后血量
		int afterBlood = calcAfterBlood(leaderMarch, totalKillCount);

		// 设置怪物剩余血量
		this.setRemainBlood(afterBlood > 0 ? afterBlood : 0);
		boolean deadlyStrike = false;
		// 战斗胜利 发击杀奖励
		if (afterBlood <= 0) {
			// 发邮件：击杀奖励邮件
			sendKillAwardMail(leaderMarch, nianCfg, atkPlayers, battleOutcome, afterBlood, totalKillCount);
			
			CYBORGGuildBaseInfo defCamp = getParent().getCampBase(leaderMarch.getParent().getCamp());
			LogUtil.logCYBORGNianKill(getParent().getId(), defCamp.campGuild, defCamp.campGuildName);
		} else {

			int totalBlood = getNianInitBlood(nianCfg.getId());
			int hpNumber = nianCfg.getHpNumber();
			int oneHpBlood = totalBlood / hpNumber;

			int beforeHpNumber = Math.min(((beforeBlood - 1) / oneHpBlood + 1), hpNumber);
			int afterHpNumber = Math.min(((afterBlood - 1) / oneHpBlood + 1), hpNumber);
			if (beforeHpNumber != afterHpNumber) {
				// if (leaderMarch.isMassMarch()) {
				// String guildTag = leaderMarch.getPlayer().getGuildTag();
				// Const.NoticeCfgId noticeId = Const.NoticeCfgId.WORLD_NIAN_GUILD_ONCE;
				// ChatParames parames = ChatParames.newBuilder().setChatType(Const.ChatType.SPECIAL_BROADCAST).setKey(noticeId)
				// .addParms(guildTag, leaderMarch.getPlayer().getName(), this.getX(),
				// this.getY())
				// .build();
				// getParent().addWorldBroadcastMsg(parames);
				// } else {
				// Const.NoticeCfgId noticeId = Const.NoticeCfgId.WORLD_NIAN_PLAYER_ONCE;
				// ChatParames parames = ChatParames.newBuilder().setChatType(Const.ChatType.SPECIAL_BROADCAST).setKey(noticeId)
				// .addParms(leaderMarch.getPlayer().getName(), this.getX(), this.getY()).build();
				// getParent().addWorldBroadcastMsg(parames);
				// }

				// 发邮件：致命一击奖励邮件
				sendOnceKillAwardMail(leaderMarch, nianCfg, atkPlayers, battleOutcome, afterBlood, totalKillCount);
				deadlyStrike = true;
			} else {
				// 发邮件：伤害奖励邮件
				sendAtkAward(leaderMarch, nianCfg, atkPlayers, battleOutcome, afterBlood, totalKillCount);

			}
		}
		// 发送战斗结果 集结野怪只有胜利
		this.sendBattleResultInfo(leaderMarch, true, WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk()), new ArrayList<ArmyInfo>(),
				this.getRemainBlood() <= 0, deadlyStrike);
	}

	/**
	 * 发邮件：战斗胜利邮件
	 */
	private void sendAtkAward(ICYBORGWorldMarch leaderMarch, WorldNianCfg nianCfg, List<ICYBORGPlayer> atkPlayers, BattleOutcome battleOutcome, int remainBlood,
			int totalKillCount) {
		// 获取怪物最大血量
		int totalEnemyBlood = getNianInitBlood(nianCfg.getId());

		// AwardItems atkAward = AwardItems.valueOf();
		// atkAward.addAwards(nianCfg.getAtkAwards());

		for (ICYBORGPlayer player : atkPlayers) {
			// 获取伤害比率
			int killCount = getKillCount(leaderMarch, battleOutcome, player, totalEnemyBlood, nianCfg.getId());
			// 加积分
			double playerHonor = CYBORGNian.getCfg().getPerHPPlayerHonor() * killCount;
			player.incrementPlayerHonor(playerHonor); // 伤害
			// 加伤害积分
			double guildHonor = CYBORGNian.getCfg().getPerHPGuildHonor() * killCount;

			CYBORGGuildBaseInfo campBase = getParent().getCampBase(leaderMarch.getParent().getCamp());
			campBase.campNianATKHonor += guildHonor;
			// // 获取受伤部队数量
			// int woundCount = getWoundCount(battleOutcome, player);
			// // 发邮件:伤害奖励
			// MonsterMail.Builder monsterMailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, Collections.emptyList(), nianCfg.getId(), new WorldPoint(),
			// atkAward.getAwardItems(), null, remainBlood, killCount, woundCount);
			// monsterMailBuilder.setX(getX());
			// monsterMailBuilder.setY(getY());
			// monsterMailBuilder.setIsAtkMax(killCount >= getKillCountLimit(leaderMarch, nianCfg.getId()));
			// monsterMailBuilder.setMaxBlood(getNianInitBlood(nianCfg.getId()));

			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.CYBORG_NIAN_ATK)
					.addContents(killCount)
					.addContents((int) guildHonor)
					.addContents((int) playerHonor)
					.setDuntype(DungeonMailType.CYBORG)
					.build());
		}
	}

	/**
	 * 发邮件：致命一击奖励邮件
	 */
	private void sendOnceKillAwardMail(ICYBORGWorldMarch leaderMarch, WorldNianCfg nianCfg, List<ICYBORGPlayer> atkPlayers, BattleOutcome battleOutcome, int remainBlood,
			int totalKillCount) {
		// // 致命一击奖励
		// AwardItems killAward = AwardItems.valueOf();
		// killAward.addAwards(nianCfg.getDeadlyAwards());
		//
		// // 伤害奖励
		// AwardItems atkAward = AwardItems.valueOf();
		// atkAward.addAwards(nianCfg.getAtkAwards());
		//
		// AwardItems sendAward = AwardItems.valueOf();
		// sendAward.addAwards(nianCfg.getDeadlyAwards());
		// sendAward.addAwards(nianCfg.getAtkAwards());
		//
		// AwardItems deadlyMassAward = AwardItems.valueOf();
		// deadlyMassAward.addAwards(nianCfg.getDeadlyMassAwards());

		// 获取怪物最大血量
		int totalEnemyBlood = getNianInitBlood(nianCfg.getId());
		// 加击杀积分
		int onceKillGuildHonor = CYBORGNian.getCfg().getOnceKillGuildHonor();
		// 加伤害积分
		double guildHonor = CYBORGNian.getCfg().getPerHPGuildHonor() * totalKillCount;
		CYBORGGuildBaseInfo campBase = getParent().getCampBase(leaderMarch.getParent().getCamp());
		campBase.campNianATKHonor += onceKillGuildHonor;
		campBase.campNianATKHonor += guildHonor;
		
		for (ICYBORGPlayer player : atkPlayers) {
			// 获取伤害比率
			int killCount = getKillCount(leaderMarch, battleOutcome, player, totalEnemyBlood, nianCfg.getId());
			// 加积分
			int onceKillPlayerHonor = CYBORGNian.getCfg().getOnceKillPlayerHonor();
			player.incrementPlayerHonor(onceKillPlayerHonor);// 击杀
			double honorAdd = CYBORGNian.getCfg().getPerHPPlayerHonor() * killCount;
			player.incrementPlayerHonor(honorAdd); // 伤害

			// 获取受伤部队数量
			// int woundCount = getWoundCount(battleOutcome, player);
			// 发邮件
			// MonsterMail.Builder mailBuilder = MailBuilderUtil.createMonsterMail(battleOutcome, Collections.emptyList(), nianCfg.getId(), new WorldPoint(), atkAward.getAwardItems(),
			// null, remainBlood, killCount, woundCount);
			// mailBuilder.setX(getX());
			// mailBuilder.setY(getY());
			// mailBuilder.setIsAtkMax(killCount >= getKillCountLimit(leaderMarch, nianCfg.getId()));
			// mailBuilder.setMaxBlood(getNianInitBlood(nianCfg.getId()));
			//
			// for (ItemInfo awardItem : killAward.getAwardItems()) {
			// mailBuilder.addKillReward(awardItem.toRewardItem());
			// }

			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.CYBORG_NIAN_ONCE_ATK)
					.addContents(killCount)
					.addContents((int) (guildHonor + onceKillGuildHonor))
					.addContents((int) (honorAdd + onceKillPlayerHonor))
					.addTips(nianCfg.getId())
					.setDuntype(DungeonMailType.CYBORG)
					.build());

			if (atkPlayers.size() > 1) {
				// SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				// .setPlayerId(player.getId())
				// .setMailId(MailId.NIAN_MASS_ONCE_KILL_AWARD)
				// .setRewards(deadlyMassAward.getAwardItems())
				// .setAwardStatus(MailRewardStatus.NOT_GET)
				// .setDuntype(DungeonMailType.CYBORG)
				// .build());
			}

		}
	}

	private void sendBattleResultInfo(ICYBORGWorldMarch march, boolean isWin, List<ArmyInfo> atkArmyList, List<ArmyInfo> defArmyList, boolean isMonsterDead, boolean deadlyStrike) {
		HPBattleResultInfoSync.Builder builder = HPBattleResultInfoSync.newBuilder();
		builder.setMarchId(march.getMarchId());
		for (ArmyInfo army : atkArmyList) {
			builder.addMyArmyId(army.getArmyId());
		}
		for (ArmyInfo army : defArmyList) {
			builder.addOppArmyId(army.getArmyId());
		}
		builder.setIsMonsterDead(isMonsterDead);

		if (isWin) {
			builder.setIsWin(Result.SUCCESS_VALUE);
		} else {
			builder.setIsWin(Result.FAIL_VALUE);
		}

		builder.setIsDeadlyStrike(deadlyStrike);

		List<ICYBORGPlayer> players = getParent().getPlayerList(CYBORGState.GAMEING);
		for (ICYBORGPlayer pla : players) {
			pla.sendProtocol(HawkProtocol.valueOf(HP.code.BATTLE_INFO_S_VALUE, builder));
		}
		if (getParent().hasAnchor()) {
			getParent().getAnchor().sendProtocol(HawkProtocol.valueOf(HP.code.BATTLE_INFO_S_VALUE, builder));
		}
	}

	/**
	 * 发邮件：击杀奖励邮件
	 */
	private void sendKillAwardMail(ICYBORGWorldMarch leaderMarch, WorldNianCfg nianCfg, List<ICYBORGPlayer> atkPlayers, BattleOutcome battleOutcome, int remainBlood,
			int totalKillCount) {

		// // 击杀奖励
		// AwardItems killAward = AwardItems.valueOf();
		// killAward.addAwards(nianCfg.getKillAwards());
		//
		// // 伤害奖励
		// AwardItems atkAward = AwardItems.valueOf();
		// atkAward.addAwards(nianCfg.getAtkAwards());
		//
		// AwardItems sendAward = AwardItems.valueOf();
		// sendAward.addAwards(nianCfg.getKillAwards());
		// sendAward.addAwards(nianCfg.getAtkAwards());
		//
		// AwardItems killMassAward = AwardItems.valueOf();
		// killMassAward.addAwards(nianCfg.getKillMassAwards());

		// 获取怪物最大血量
		int totalEnemyBlood = getNianInitBlood(nianCfg.getId());
		// 加击杀积分
		int killGuildHonor = CYBORGNian.getCfg().getKillGuildHonor();
		// 加伤害积分
		double guildHonor = CYBORGNian.getCfg().getPerHPGuildHonor() * totalKillCount;
		CYBORGGuildBaseInfo campBase = getParent().getCampBase(leaderMarch.getParent().getCamp());
		campBase.campNianKillCount++;
		campBase.campNianATKHonor += killGuildHonor;
		campBase.campNianATKHonor += guildHonor;

		for (ICYBORGPlayer player : atkPlayers) {

			// 获取伤害比率
			int killCount = getKillCount(leaderMarch, battleOutcome, player, totalEnemyBlood, nianCfg.getId());
			// 加积分
			int killPlayerHonor = CYBORGNian.getCfg().getKillPlayerHonor();
			player.incrementPlayerHonor(killPlayerHonor);// 击杀
			double honorAdd = CYBORGNian.getCfg().getPerHPPlayerHonor() * killCount;
			player.incrementPlayerHonor(honorAdd); // 伤害

			// // 获取受伤部队数量
			// int woundCount = getWoundCount(battleOutcome, player);
			// // 发邮件
			// MonsterMail.Builder mailBuilder = this.createMonsterMail(battleOutcome, Collections.emptyList(), nianCfg.getId(), atkAward.getAwardItems(), null,
			// remainBlood, killCount, woundCount);
			// mailBuilder.setIsAtkMax(killCount >= getKillCountLimit(leaderMarch, nianCfg.getId()));
			// mailBuilder.setMaxBlood(getNianInitBlood(nianCfg.getId()));
			//
			// for (ItemInfo awardItem : killAward.getAwardItems()) {
			// mailBuilder.addKillReward(awardItem.toRewardItem());
			// }

			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.CYBORG_NIAN_KILL)
					.addContents(killCount)
					.addContents((int) (guildHonor + killGuildHonor))
					.addContents((int) (honorAdd + killPlayerHonor))
					.addTips(nianCfg.getId())
					.setDuntype(DungeonMailType.CYBORG)
					.build());

			if (atkPlayers.size() > 1) {
				// SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				// .setPlayerId(player.getId())
				// .setMailId(MailId.NIAN_MASS_KILL_AWARD)
				// .setRewards(killMassAward.getAwardItems())
				// .setAwardStatus(MailRewardStatus.NOT_GET)
				// .setDuntype(DungeonMailType.CYBORG)
				// .build());
			}

		}
	}

	/**
	 * 创建野怪邮件
	 * 
	 * @param afterArmyList
	 *            进攻方部队列表
	 * @param isWin
	 *            是否胜利
	 * @param monsterId
	 *            怪物id
	 * @param point
	 *            世界点
	 * @param normalReward
	 *            普通奖励
	 * @param fistKillReward
	 *            首杀奖励
	 * @return
	 */
	private MonsterMail.Builder createMonsterMail(BattleOutcome battleOutcome, List<Integer> heroId, int monsterId, List<ItemInfo> normalRewards, List<ItemInfo> fistKillRewards,
			float remainBlood, float atkValue, int woundCount) {
		MonsterMail.Builder monsterBuilder = MonsterMail.newBuilder();
		monsterBuilder.setMonsterId(monsterId);
		monsterBuilder.setResult(battleOutcome.isAtkWin() ? FightResult.ATTACK_SUCC : FightResult.ATTACK_FAIL);
		if (fistKillRewards != null && !fistKillRewards.isEmpty()) {
			monsterBuilder.setFirstKill(true);
			fistKillRewards.forEach(fistKillReward -> monsterBuilder.addFirstKillReward(fistKillReward.toRewardItem()));
		}
		if (normalRewards != null) {
			normalRewards.forEach(normalReward -> monsterBuilder.addRewards(normalReward.toRewardItem()));
		}
		monsterBuilder.setX(this.getX());
		monsterBuilder.setY(this.getY());

		int totalCnt = 0;
		int hurtCnt = 0;
		int survivalCnt = 0;
		double disBattlePoint = 0;
		// 本次战斗部队信息
		Map<String, List<ArmyInfo>> battleArmyMapAtk = battleOutcome.getBattleArmyMapAtk();
		for (ArmyInfo info : WorldUtil.mergAllPlayerArmy(battleArmyMapAtk)) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, info.getArmyId());
			totalCnt += info.getTotalCount();
			survivalCnt += info.getFreeCnt();
			hurtCnt += info.getWoundedCount();
			disBattlePoint += info.getWoundedCount() * cfg.getPower();
		}
		FighteInfo.Builder figntInfo = FighteInfo.newBuilder();
		figntInfo.setTotalSoldier(totalCnt);
		figntInfo.setHurtSoldier(hurtCnt);
		figntInfo.setSurvivalSoldier(survivalCnt);
		figntInfo.setDisBattlePoint((int) Math.ceil(disBattlePoint));

		monsterBuilder.setAtkFight(figntInfo);

		monsterBuilder.setRemainBlood(remainBlood);
		monsterBuilder.setAtkValue(atkValue);
		monsterBuilder.setWoundCount(woundCount);
		return monsterBuilder;
	}

	/**
	 * 获取伤害比率
	 */
	private int getKillCount(ICYBORGWorldMarch leaderMarch, BattleOutcome battleOutcome, Player player, int totalCount, int nianId) {
		// 单人击杀玩家数量
		int playerKillCount = 0;
		List<ArmyInfo> playerArmyInfos = battleOutcome.getAftArmyMapAtk().get(player.getId());
		for (ArmyInfo playerArmyInfo : playerArmyInfos) {
			playerKillCount += playerArmyInfo.getKillCount();
		}

		playerKillCount = Math.min(playerKillCount, getKillCountLimit(leaderMarch, nianId));

		// 伤害比率
		return playerKillCount;
	}

	/**
	 * 获取受伤部队数量
	 */
	private int getWoundCount(BattleOutcome battleOutcome, Player player) {
		int woundCount = 0;
		List<ArmyInfo> playerArmyInfos = battleOutcome.getAftArmyMapAtk().get(player.getId());
		for (ArmyInfo playerArmyInfo : playerArmyInfos) {
			woundCount += playerArmyInfo.getWoundedCount();
		}
		return woundCount;
	}

	/**
	 * 获取击杀总数量
	 */
	private int getTotalKillCount(BattleOutcome battleOutcome) {
		int totalKillCount = 0;
		Map<String, List<ArmyInfo>> aftArmyMapAtk = battleOutcome.getAftArmyMapAtk();
		for (Entry<String, List<ArmyInfo>> entry : aftArmyMapAtk.entrySet()) {
			List<ArmyInfo> armyInfos = entry.getValue();
			int selfTotalCnt = 0;
			for (ArmyInfo armyInfo : armyInfos) {
				selfTotalCnt += armyInfo.getKillCount();
			}
			totalKillCount += selfTotalCnt;
		}

		return (int) (totalKillCount * GsConst.EFF_PER * getCfg().getHurtRate());
	}

	/**
	 * 计算怪物剩余血量(部队)
	 */
	private int calcAfterBlood(ICYBORGWorldMarch leaderMarch, int totalKillCount) {

		totalKillCount = Math.min(totalKillCount, getKillCountLimit(leaderMarch, NIAN_ID));

		// 攻打前怪物剩余血量
		int beforeBlood = this.getRemainBlood();
		// 攻击后怪物剩余血量
		int afterBlood = (beforeBlood >= totalKillCount) ? (beforeBlood - totalKillCount) : 0;
		return afterBlood;
	}

	private int getKillCountLimit(ICYBORGWorldMarch leaderMarch, int nianId) {
		// 伤害上限
		double killpercent = CYBORGNian.getCfg().getSignKillLimit();
		if (leaderMarch.isMassMarch()) {
			killpercent = CYBORGNian.getCfg().getMassKillLimit();
		}

		int maxBlood = getNianInitBlood(nianId);
		int killCountLimit = (int) Math.ceil(maxBlood * (killpercent / GsConst.RANDOM_MYRIABIT_BASE));
		return killCountLimit;
	}

	public static CYBORGNianCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(CYBORGNianCfg.class);
	}

	@Override
	public CYBORGBattleRoom getParent() {
		return parent;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getPointId() {
		return GameUtil.combineXAndY(x, y);
	}

	@Override
	public String getGuildId() {
		return "";
	}

	@Override
	public WorldPointPB.Builder toBuilder(ICYBORGPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setMonsterId(NIAN_ID);
		int maxBlood = getNianInitBlood(NIAN_ID);
		builder.setMonsterMaxBlood(maxBlood);
		builder.setRemainBlood(remainBlood);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(ICYBORGPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setMonsterId(NIAN_ID);
		int maxBlood = getNianInitBlood(NIAN_ID);
		builder.setMonsterMaxBlood(maxBlood);
		builder.setRemainBlood(remainBlood);
		return builder;
	}

	public int getNianInitBlood(int nianId) {
		WorldNianCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldNianCfg.class, nianId);
		if (cfg == null) {
			return 0;
		}

		long blood = 0;
		List<ArmyInfo> armyList = cfg.getArmyList();
		for (ArmyInfo army : armyList) {
			blood += army.getTotalCount();
		}

		int hpNumber = cfg.getHpNumber();

		// 返回血量自适应，可以整除血条数量
		return (int) ((((blood - 1) / hpNumber) + 1) * hpNumber);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.NIAN;
	}

	@Override
	public boolean onTick() {
		if (this.getRemainBlood() <= 0) {
			this.removeWorldPoint();
		}
		return false;
	}

	@Override
	public boolean needJoinGuild() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeWorldPoint() {
		getParent().removeViewPoint(this);
	}

	public int getRemainBlood() {
		return remainBlood;
	}

	public void setRemainBlood(int remainBlood) {
		this.remainBlood = remainBlood;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

}

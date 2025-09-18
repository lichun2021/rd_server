package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.battleIncome.impl.PveBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.FoggyFortressCfg;
import com.hawk.game.item.AwardItems;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZFoggyCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZFoggyHonor;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.MailRewards;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.FoggyInfo;

public class YQZZFoggyFortress implements IYQZZWorldPoint {
	private final YQZZBattleRoom parent;
	// 迷雾信息
	private String foggyInfo = "";
	private int cfgId;
	private int foggyFortressId;
	private FoggyInfo foggyInfoObj;
	private int x;
	private int y;
	private int aoiObjId = 0;

	@Override
	public final WorldPointType getPointType() {
		return WorldPointType.FOGGY_FORTRESS;
	}

	public YQZZFoggyFortress(YQZZBattleRoom parent) {
		this.parent = parent;
	}

	public static YQZZFoggyFortress create(YQZZBattleRoom parent, YQZZFoggyCfg foggycfg) {

		FoggyFortressCfg foggyFortressCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggycfg.getFoggyFortressId());
		FoggyInfo foggyInfo = new FoggyInfo();
		foggyInfo.setTrapInfo(foggyFortressCfg.getRandTrapInfo());
		foggyInfo.setSoliderInfo(foggyFortressCfg.getRandSoldierInfo());
		foggyInfo.setHeroIds(foggyFortressCfg.getRandHeroId(2));

		YQZZFoggyFortress result = new YQZZFoggyFortress(parent);
		result.cfgId = foggycfg.getId();
		result.foggyFortressId = foggycfg.getFoggyFortressId();
		result.initFoggyInfo(foggyInfo);
		return result;
	}

	public int getCfgId() {
		return cfgId;
	}

	public YQZZFoggyCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(YQZZFoggyCfg.class, cfgId);
	}

	public FoggyFortressCfg getFoggyFortressCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyFortressId);
	}

	public void onMarchReach(IYQZZWorldMarch leaderMarch) {
		Player leader = leaderMarch.getParent();
		Set<IYQZZWorldMarch> massJoinMarchs = leaderMarch.getMassJoinMarchs(true);
		// 目标点

		/**********************    战斗数据组装及战斗***************************/
		// 进攻方玩儿家
		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(leader);

		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(leaderMarch);

		// 填充参与集结信息
		for (IYQZZWorldMarch massJoinMarch : massJoinMarchs) {
			atkPlayers.add(massJoinMarch.getParent());
			atkMarchs.add(massJoinMarch);
		}

		// 战斗信息
		PveBattleIncome battleIncome = BattleService.getInstance().initFoggyBattleData(BattleConst.BattleType.ATTACK_FOGGY, this.getEntity(), atkPlayers, atkMarchs);
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		battleOutcome.setDuntype(DungeonMailType.YQZZ);
		/**********************战斗数据组装及战斗***************************/

		// 战斗结果处理
		MailRewards mailRewards = doFoggyBattleResult(battleOutcome, atkPlayers, this.foggyFortressId);
		// 攻击方玩家部队
		List<ArmyInfo> mergAllPlayerArmy = WorldUtil.mergAllPlayerArmy(battleOutcome.getAftArmyMapAtk());
		// 播放战斗动画
		leaderMarch.sendBattleResultInfo(battleOutcome.isAtkWin(), mergAllPlayerArmy, new ArrayList<ArmyInfo>());
		// 据点PVE战斗邮件发放
		FightMailService.getInstance().sendPveFightMail(BattleConst.BattleType.ATTACK_FOGGY, battleIncome, battleOutcome, mailRewards);

		// 日志
		FoggyFortressCfg foggyCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, this.foggyFortressId);
		for (Player player : atkPlayers) {
			//DungeonRedisLog.log(player.getId(), "foggyFortressId {} ", foggyFortressId);
			LogUtil.logAttackFoggy(player, this.getX(), this.getY(), foggyCfg.getId(), foggyCfg.getLevel(), player.getId().equals(leader.getId()),
					battleOutcome.isAtkWin(), atkPlayers.size());
		}

		// 行军返回
		leaderMarch.onMarchReturn(this.getPointId(), leaderMarch.getParent().getPointId(), battleOutcome.getAftArmyMapAtk().get(leaderMarch.getPlayerId()));
		// 队员行军返回
		for (IYQZZWorldMarch tmpMarch : leaderMarch.getMassJoinMarchs(true)) {
			tmpMarch.onMarchReturn(this.getPointId(), tmpMarch.getParent().getPointId(), battleOutcome.getAftArmyMapAtk().get(tmpMarch.getPlayerId()));
		}
		if (battleOutcome.isAtkWin()) {
			removeWorldPoint();
		}
	}

	@Override
	public WorldPointPB.Builder toBuilder(IYQZZPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(getX());
		builder.setPointY(getY());
		builder.setPointType(getPointType());

		builder.setMonsterId(foggyFortressId);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IYQZZPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(getX());
		builder.setPointY(getY());
		builder.setPointType(getPointType());

		builder.setMonsterId(foggyFortressId);
		builder.setPower(foggyInfoObj.getTotalPower());
		// FoggyFortressCfg foggyCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, getMonsterId());
		// long leftTime = (getLifeStartTime() + foggyCfg.getLifeTime() * 1000) - HawkTime.getMillisecond();
		// builder.setCommonEndTime(leftTime);
		return builder;
	}

	/**
	 * 战斗结果处理
	 * @param outCome
	 * @param atkPlayers
	 * @param foggyId
	 */
	public MailRewards doFoggyBattleResult(BattleOutcome outCome, List<Player> atkPlayers, int foggyId) {
		// 奖励展示。 只用于邮件里显示。
		MailRewards mailRewards = new MailRewards();

		if (outCome.isAtkWin() && atkPlayers.size() > 1) {
			FoggyFortressCfg foggyCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			for (Player player : atkPlayers) {
				IYQZZPlayer atkPlayer = (IYQZZPlayer) player;
				int attackFoggyWinTimes = atkPlayer.getKillFoggy();
				int joinAtkWinTimes = atkPlayer.getJoinKillFoggy();
				AwardItems awardItems = AwardItems.valueOf();
				
				boolean isLeader = atkPlayers.get(0).getId().equals(atkPlayer.getId());
				if (isLeader && attackFoggyWinTimes < getParent().getCfg().getFoggyStartAssembleLimit()) {
					awardItems.addAward(foggyCfg.getStartAssembleReward());
					YQZZFoggyHonor mhonor = atkPlayer.getFoggyHonorStat(foggyFortressId);
					mhonor.setKillCount(mhonor.getKillCount() + 1);
					mhonor.setPlayerHonor(mhonor.getPlayerHonor() + getCfg().getPlayerScore());
					mhonor.setGuildHonor(mhonor.getGuildHonor() + getCfg().getAllianceScore());
					MailParames.Builder mailParames = MailParames.newBuilder()
							.setMailId(MailId.MASS_FOGGY_REWARD)
							.setPlayerId(atkPlayer.getId())
							.setRewards(awardItems.toString())
							.setAwardStatus(MailRewardStatus.NOT_GET);
					SystemMailService.getInstance().sendMail(mailParames.build());
				}
				
				if (joinAtkWinTimes < getParent().getCfg().getFoggyAssembleLimit()) {
					YQZZFoggyHonor mhonor = atkPlayer.getFoggyHonorStat(foggyFortressId);
					mhonor.setJoinKillCount(mhonor.getJoinKillCount() + 1);
					mhonor.setPlayerHonor(mhonor.getPlayerHonor() + getCfg().getPlayerScore());
					//集结参与奖励
					AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, foggyCfg.getAssembleReward());
					if (awardCfg != null) {
						AwardItems joinAward = awardCfg.getRandomAward();
						awardItems.appendAward(joinAward);
						MailParames.Builder mailParames = MailParames.newBuilder()
								.setMailId(MailId.JOIN_FOGGY_REWARD)
								.setPlayerId(atkPlayer.getId())
								.setRewards(joinAward.toString())
								.setAwardStatus(MailRewardStatus.NOT_GET);
						SystemMailService.getInstance().sendMail(mailParames.build());
					}
				}
				
				if (!awardItems.getAwardItems().isEmpty()) {
					YQZZFoggyHonor mhonor = atkPlayer.getFoggyHonorStat(foggyFortressId);
					mhonor.setNationHonor(mhonor.getNationHonor() + getCfg().getNationScore());
					atkPlayer.getPush().syncPlayerInfo();
				}
				
				mailRewards.addSelfRewards(atkPlayer.getId(), awardItems.getAwardItems());
			}
		}

		return mailRewards;
	}

	/**
	 * 初始化迷雾要塞对象
	 * 
	 * @param foggyInfo
	 */
	public void initFoggyInfo(FoggyInfo foggyInfo) {
		this.foggyInfoObj = foggyInfo;
	}

	/**
	 * 获取迷雾要塞对象
	 * 
	 * @return
	 */
	public FoggyInfo getFoggyInfoObj() {
		return foggyInfoObj;
	}

	public String getFoggyInfo() {
		return foggyInfo;
	}

	public void setFoggyInfo(String foggyInfo) {
		this.foggyInfo = foggyInfo;
	}

	public void setFoggyInfoObj(FoggyInfo foggyInfoObj) {
		this.foggyInfoObj = foggyInfoObj;
	}

	@Override
	public YQZZBattleRoom getParent() {
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
	public int getAoiObjId() {
		return aoiObjId;
	}

	@Override
	public void setAoiObjId(int aoiObjId) {
		this.aoiObjId = aoiObjId;
	}

	@Override
	public int getGridCnt() {
		return 2;
	}

	@Override
	public boolean onTick() {
		// System.out.println("x "+x +" , "+y);
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean needJoinGuild() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeWorldPoint() {
		getParent().getWorldPointService().removeViewPoint(this);
	}

	public WorldPoint getEntity() {
		WorldPoint worldPoint = new WorldPoint();
		worldPoint.setX(x);
		worldPoint.setY(y);
		worldPoint.setId(getPointId());
		worldPoint.initFoggyInfo(foggyInfoObj);
		worldPoint.setMonsterId(foggyFortressId);
		return worldPoint;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getFoggyFortressId() {
		return foggyFortressId;
	}

	public void setFoggyFortressId(int foggyFortressId) {
		this.foggyFortressId = foggyFortressId;
	}

}

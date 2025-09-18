package com.hawk.game.module.lianmengtaiboliya.worldpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.config.WorldNianCfg;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengtaiboliya.ITBLYWorldPoint;
import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager.CAMP;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYNianCfg;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.ITBLYWorldMarch;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.Result;
import com.hawk.game.protocol.HP;
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
import com.hawk.game.world.march.IWorldMarch;

/**
 * 机甲
 * 
 * @author lwt
 * @date 2019年11月7日
 */
public class TBLYNian implements ITBLYWorldPoint {
	private final TBLYBattleRoom parent;
	private int x;
	private int y;
	private int remainBlood;
	private final int initBlood;
	public static final int NIAN_ID = 3;
	double KILL_RAND = 0.02;

	public TBLYNian(TBLYBattleRoom parent) {
		this.parent = parent;
		initBlood = getNianInitBlood(NIAN_ID);
		setRemainBlood(initBlood);
	}

	@Override
	public void onMarchReach(ITBLYWorldMarch leaderMarch) {
		// 进攻方玩家
		List<ITBLYPlayer> atkPlayers = new ArrayList<>();
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		// 进攻方行军
		List<ITBLYWorldMarch> atkMarchList = new ArrayList<>();
		atkMarchList.add(leaderMarch);
		atkMarchList.addAll(leaderMarch.getMassJoinMarchs(true));
		for (ITBLYWorldMarch iWorldMarch : atkMarchList) {
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

		/********************** 战斗数据组装及战斗 ***************************/

		// 战斗结果处理
		doBattleResult(leaderMarch, nianCfg, atkPlayers, atkMarchs);

		// 行军返回
		leaderMarch.onMarchReturn(this.getPointId(), leaderMarch.getParent().getPointId(), leaderMarch.getArmys());
		// 队员行军返回
		for (ITBLYWorldMarch tmpMarch : leaderMarch.getMassJoinMarchs(true)) {
			tmpMarch.onMarchReturn(this.getPointId(), tmpMarch.getParent().getPointId(), tmpMarch.getArmys());
		}

		// 战斗胜利，移除点
		if (this.getRemainBlood() <= 0) {
			String guildTag = leaderMarch.getPlayer().getGuildTag();
			String guildName = leaderMarch.getPlayer().getGuildName();
			ChatParames parames = ChatParames.newBuilder().setChatType(Const.ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(Const.NoticeCfgId.TBLY_NIAN_KILLED)
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
	private void doBattleResult(ITBLYWorldMarch leaderMarch, WorldNianCfg nianCfg, List<ITBLYPlayer> atkPlayers, List<IWorldMarch> atkMarchs) {
		// 结算前血量
		int beforeBlood = this.getRemainBlood();

		// 总击杀怪物数量(血量)
		int totalKillCount = getTotalKillCount(atkMarchs);

		// 计算后血量
		int afterBlood = calcAfterBlood(leaderMarch, totalKillCount);

		// 设置怪物剩余血量
		this.setRemainBlood(afterBlood > 0 ? afterBlood : 0);
		boolean deadlyStrike = false;
		// 战斗胜利 发击杀奖励
		if (afterBlood <= 0) {
			// 发邮件：击杀奖励邮件
			sendKillAwardMail(leaderMarch, nianCfg, atkPlayers, atkMarchs, afterBlood, totalKillCount);

			LogUtil.logTBLYNianKill(getParent().getExtParm(), getGuildId());

		} else {

			int hpNumber = nianCfg.getHpNumber();
			int oneHpBlood = initBlood / hpNumber;

			int beforeHpNumber = Math.min(((beforeBlood - 1) / oneHpBlood + 1), hpNumber);
			int afterHpNumber = Math.min(((afterBlood - 1) / oneHpBlood + 1), hpNumber);
			if (beforeHpNumber != afterHpNumber) {

				// 发邮件：致命一击奖励邮件
				sendOnceKillAwardMail(leaderMarch, nianCfg, atkPlayers, atkMarchs, afterBlood, totalKillCount);
				deadlyStrike = true;
			} else {
				// 发邮件：伤害奖励邮件
				sendAtkAward(leaderMarch, nianCfg, atkPlayers, atkMarchs, afterBlood, totalKillCount);

			}
		}
		// 发送战斗结果 集结野怪只有胜利
		this.sendBattleResultInfo(leaderMarch, true, atkMarchs, new ArrayList<ArmyInfo>(),
				this.getRemainBlood() <= 0, deadlyStrike);
	}

	/**
	 * 发邮件：战斗胜利邮件
	 */
	private void sendAtkAward(ITBLYWorldMarch leaderMarch, WorldNianCfg nianCfg, List<ITBLYPlayer> atkPlayers, List<IWorldMarch> atkMarchs, int remainBlood, int totalKillCount) {
		// 获取怪物最大血量

		// 加伤害积分
		for (ITBLYPlayer player : atkPlayers) {
			// 获取伤害比率
			int killCount = getKillCount(leaderMarch, atkMarchs, player, initBlood, nianCfg.getId());
			// 加积分
			double playerHonor = TBLYNian.getCfg().getPerHPPlayerHonor() * killCount;
			player.incrementPlayerHonor(playerHonor); // 伤害

			double guildHonor = TBLYNian.getCfg().getPerHPGuildHonor() * killCount;
			leaderMarch.getParent().incrementGuildHonor(guildHonor);
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.TBLY_NIAN_ATK)
					.addContents(killCount)
					.addContents((int) guildHonor)
					.addContents((int) playerHonor)
					.setDuntype(DungeonMailType.TBLY)
					.build());
		}
	}

	/**
	 * 发邮件：致命一击奖励邮件
	 */
	private void sendOnceKillAwardMail(ITBLYWorldMarch leaderMarch, WorldNianCfg nianCfg, List<ITBLYPlayer> atkPlayers, List<IWorldMarch> atkMarchs, int remainBlood,
			int totalKillCount) {

		// 加击杀积分
		double onceKillGuildHonor = TBLYNian.getCfg().getOnceKillGuildHonor() * 1D / atkPlayers.size();
		// 加伤害积分
		for (ITBLYPlayer player : atkPlayers) {
			// 获取伤害比率
			int killCount = getKillCount(leaderMarch, atkMarchs, player, initBlood, nianCfg.getId());
			// 加积分
			int onceKillPlayerHonor = TBLYNian.getCfg().getOnceKillPlayerHonor();
			player.incrementPlayerHonor(onceKillPlayerHonor);// 击杀
			double honorAdd = TBLYNian.getCfg().getPerHPPlayerHonor() * killCount;
			player.incrementPlayerHonor(honorAdd); // 伤害

			double guildHonor = TBLYNian.getCfg().getPerHPGuildHonor() * killCount;
			player.incrementGuildHonor(guildHonor);
			player.incrementGuildHonor(onceKillGuildHonor);

			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.TBLY_NIAN_ONCE_ATK)
					.addContents(killCount)
					.addContents((int) (guildHonor + onceKillGuildHonor))
					.addContents((int) (honorAdd + onceKillPlayerHonor))
					.addTips(nianCfg.getId())
					.setDuntype(DungeonMailType.TBLY)
					.build());

		}
	}

	private void sendBattleResultInfo(ITBLYWorldMarch march, boolean isWin, List<IWorldMarch> atkMarchs, List<ArmyInfo> defArmyList, boolean isMonsterDead, boolean deadlyStrike) {
		HPBattleResultInfoSync.Builder builder = HPBattleResultInfoSync.newBuilder();
		builder.setMarchId(march.getMarchId());
		for (ArmyInfo army : march.getArmys()) {
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

		List<ITBLYPlayer> players = getParent().getPlayerList(TBLYState.GAMEING);
		for (ITBLYPlayer pla : players) {
			pla.sendProtocol(HawkProtocol.valueOf(HP.code.BATTLE_INFO_S_VALUE, builder));
		}
		for (ITBLYPlayer anchor : getParent().getAnchors()) {
			anchor.sendProtocol(HawkProtocol.valueOf(HP.code.BATTLE_INFO_S_VALUE, builder));
		}
	}

	/**
	 * 发邮件：击杀奖励邮件
	 */
	private void sendKillAwardMail(ITBLYWorldMarch leaderMarch, WorldNianCfg nianCfg, List<ITBLYPlayer> atkPlayers, List<IWorldMarch> atkMarchs, int remainBlood,
			int totalKillCount) {

		// 加击杀积分
		double killGuildHonor = TBLYNian.getCfg().getKillGuildHonor() * 1D / atkPlayers.size();

		if (leaderMarch.getParent().getCamp() == CAMP.A) {
			getParent().campANianKillCount++;
		} else {
			getParent().campBNianKillCount++;
		}

		if (StringUtils.isEmpty(getParent().firstKillNian)) {
			getParent().firstKillNian = leaderMarch.getParent().getGuildId();
		}

		for (ITBLYPlayer player : atkPlayers) {

			// 获取伤害比率
			int killCount = getKillCount(leaderMarch, atkMarchs, player, initBlood, nianCfg.getId());
			// 加积分
			int killPlayerHonor = TBLYNian.getCfg().getKillPlayerHonor();
			player.incrementPlayerHonor(killPlayerHonor);// 击杀
			double honorAdd = TBLYNian.getCfg().getPerHPPlayerHonor() * killCount;
			player.incrementPlayerHonor(honorAdd); // 伤害
			// 加伤害积分
			double guildHonor = TBLYNian.getCfg().getPerHPGuildHonor() * killCount;
			player.incrementGuildHonor(guildHonor);
			player.incrementGuildHonor(killGuildHonor);
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.TBLY_NIAN_KILL)
					.addContents(killCount)
					.addContents((int) (guildHonor + killGuildHonor))
					.addContents((int) (honorAdd + killPlayerHonor))
					.addTips(nianCfg.getId())
					.setDuntype(DungeonMailType.TBLY)
					.build());

		}
	}

	/**
	 * 获取伤害比率
	 */
	private int getKillCount(ITBLYWorldMarch leaderMarch, List<IWorldMarch> atkMarchs, Player player, int totalCount, int nianId) {
		// 单人击杀玩家数量
		int playerKillCount = 0;
		for (IWorldMarch march : atkMarchs) {
			if (march.getPlayer() != player) {
				continue;
			}
			for (PlayerHero hero : march.getHeros()) {
				playerKillCount += hero.power() * 5;
			}
			Optional<SuperSoldier> spsoldier = player.getSuperSoldierByCfgId(march.getSuperSoldierId());
			if (spsoldier.isPresent()) {
				playerKillCount += spsoldier.get().power() * 5;
			}
			playerKillCount += march.getArmysPowers();
		}

		// TODO 系数
		double xishu = getCfg().getPerPowerHurt() * GsConst.EFF_PER;
		return (int) (playerKillCount * xishu * (1 + KILL_RAND - Math.random() * KILL_RAND));
	}

	/**
	 * 获取击杀总数量
	 */
	private int getTotalKillCount(List<IWorldMarch> atkMarchs) {
		int totalKillCount = 0;
		for (IWorldMarch march : atkMarchs) {
			for (PlayerHero hero : march.getHeros()) {
				totalKillCount += hero.power() * 5;
			}
			Optional<SuperSoldier> spsoldier = march.getPlayer().getSuperSoldierByCfgId(march.getSuperSoldierId());
			if (spsoldier.isPresent()) {
				totalKillCount += spsoldier.get().power() * 5;
			}
			totalKillCount += march.getArmysPowers();
		}

		// TODO 系数
		double xishu = getCfg().getPerPowerHurt() * GsConst.EFF_PER;
		return (int) (totalKillCount * xishu * (1 + KILL_RAND - Math.random() * KILL_RAND));
	}

	/**
	 * 计算怪物剩余血量(部队)
	 */
	private int calcAfterBlood(ITBLYWorldMarch leaderMarch, int totalKillCount) {

		totalKillCount = Math.min(totalKillCount, getKillCountLimit(leaderMarch, NIAN_ID));

		// 攻打前怪物剩余血量
		int beforeBlood = this.getRemainBlood();
		// 攻击后怪物剩余血量
		int afterBlood = (beforeBlood >= totalKillCount) ? (beforeBlood - totalKillCount) : 0;
		return afterBlood;
	}

	private int getKillCountLimit(ITBLYWorldMarch leaderMarch, int nianId) {
		// 伤害上限
		double killpercent = TBLYNian.getCfg().getSignKillLimit();
		if (leaderMarch.isMassMarch()) {
			killpercent = TBLYNian.getCfg().getMassKillLimit();
		}

		int killCountLimit = (int) Math.ceil(initBlood * (killpercent / GsConst.RANDOM_MYRIABIT_BASE));
		return killCountLimit;
	}

	public static TBLYNianCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(TBLYNianCfg.class);
	}

	@Override
	public TBLYBattleRoom getParent() {
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
	public WorldPointPB.Builder toBuilder(ITBLYPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setMonsterId(NIAN_ID);
		builder.setMonsterMaxBlood(initBlood);
		builder.setRemainBlood(remainBlood);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(ITBLYPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setMonsterId(NIAN_ID);
		builder.setMonsterMaxBlood(initBlood);
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
		double xishu = getParent().getTotalEnterPower() * 1d / getCfg().getHpMutiplePower();
		xishu = Math.max(1, xishu);// 最低保留10%血量
		xishu = Math.min(xishu, getCfg().getHpMutipleMax());// 不能超过int最大值
		blood = (long) (blood * xishu);

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

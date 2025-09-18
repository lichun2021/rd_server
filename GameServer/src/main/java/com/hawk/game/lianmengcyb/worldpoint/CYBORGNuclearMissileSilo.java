package com.hawk.game.lianmengcyb.worldpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.google.common.base.Joiner;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.CYBORGNuclearMissileSiloCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.lianmengcyb.CYBORGGuildBaseInfo;
import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.player.CYBORGPlayer;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldmarch.ICYBORGWorldMarch;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.CYBORG.PBCYBORGNuclearSync;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.MailArmyInfo;
import com.hawk.game.protocol.Mail.MailPlayerInfo;
import com.hawk.game.protocol.Mail.MailSoldierPB;
import com.hawk.game.protocol.Mail.PBCYBORGNuclearHitContent;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 核弹发射井
 *
 */
public class CYBORGNuclearMissileSilo extends ICYBORGBuilding {
	private long lastTick;
	private long MissileCoolDownTime = getCfg().getMissileCoolDownTime() * 1000;
	/** 联盟控制时间 , 发射后清0 */
	private long controlTime = 3600000;
	private PBCYBORGNuclearSync sendRecord;

	public CYBORGNuclearMissileSilo(CYBORGBattleRoom parent) {
		super(parent);
		if (getParent().IS_GO_MODEL) {
			MissileCoolDownTime = getCfg().getMissileCoolDownTime() * 1000;
		}
	}

	@Override
	public void onPlayerLogin(ICYBORGPlayer gamer) {
		if (Objects.nonNull(sendRecord)) {
			gamer.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_NUCLEAR_SEND_S, sendRecord.toBuilder()));
		}
	}

	@Override
	public void anchorJoin(ICYBORGPlayer gamer) {
		if (Objects.nonNull(sendRecord)) {
			gamer.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_NUCLEAR_SEND_S, sendRecord.toBuilder()));
		}
	}

	/** 核弹准备时间 */
	public long getNuclearControlTime(String guildId) {
		return controlTime;
	}

	@Override
	public boolean onTick() {
		super.onTick();
		long timePass = getParent().getCurTimeMil() - lastTick;
		if (timePass < 1000) {
			return true;
		}
		lastTick = getParent().getCurTimeMil();
		controlTime += timePass;

		nuclearShootCheck();
		return true;
	}
	
	public boolean isCoolDown(){
		if (getState() == CYBORGBuildState.ZHAN_LING) {
			long cool = MissileCoolDownTime - controlTime;
			if (cool < 0) {
				return true;
			}
		}
		return false;
	}
	
	public String getNuclearReadyGuild(){
		if(isCoolDown()){
			return getGuildId();
		}
		return "";
	}
	
	public String getNuclearReadyLeader(){
		if(isCoolDown()){
			return getPlayerId();
		}
		return "";
	}

	private void nuclearShootCheck() {
		if (Objects.isNull(sendRecord)) {
			return;
		}
		if (getParent().IS_GO_MODEL) {
			System.out.println(sendRecord.getSendOver() - getParent().getCurTimeMil());
		}
		if (sendRecord.getSendOver() > getParent().getCurTimeMil()) {
			return;
		}
		// 命中
		PBCYBORGNuclearSync shotNuclear = sendRecord.toBuilder().build();
		sendRecord = null;

		List<ICYBORGPlayer> mailReceiverList = new ArrayList<>();

		int pointId = GameUtil.combineXAndY(shotNuclear.getTarX(), shotNuclear.getTarY());
		ICYBORGWorldPoint tpoint = getParent().getWorldPoint(pointId).orElse(null);

		PBCYBORGNuclearHitContent.Builder content = PBCYBORGNuclearHitContent.newBuilder();
		content.setX(shotNuclear.getTarX());
		content.setY(shotNuclear.getTarY());
		final double killPct = getCfg().getKillPercentage() * 0.01;
		boolean hit = false;
		if (tpoint instanceof ICYBORGBuilding && !Objects.equals(tpoint.getGuildId(), shotNuclear.getGuildId())) {
			ICYBORGWorldMarch leaderMarch = ((ICYBORGBuilding) tpoint).getLeaderMarch();
			if (Objects.nonNull(leaderMarch)) {
				content.setGuildname(leaderMarch.getParent().getGuildName());
				content.setGuildTag(leaderMarch.getParent().getGuildTag());
				content.setCamp(leaderMarch.getParent().getCamp().intValue());
			}
			List<ICYBORGWorldMarch> tarMarches = getParent().getPointMarches(tpoint.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
			for (ICYBORGWorldMarch march : tarMarches) {
				hit = true;
				mailReceiverList.add(march.getParent());
				hitMarch(content, killPct, march);
			}
		} else if (tpoint instanceof ICYBORGPlayer && !Objects.equals(tpoint.getGuildId(), shotNuclear.getGuildId())) {
			hit = true;
			CYBORGPlayer defPlayer = (CYBORGPlayer) tpoint;
			content.setGuildname(defPlayer.getGuildName());
			content.setGuildTag(defPlayer.getGuildTag());
			content.setCamp(defPlayer.getCamp().intValue());

			MailPlayerInfo.Builder tarPlayer = MailPlayerInfo.newBuilder()
					.setPlayerId(defPlayer.getId())
					.setName(GameUtil.getPlayerNameWithGuildTag(defPlayer.getGuildId(), defPlayer.getName()))
					.setIcon(defPlayer.getIcon())
					.setX(defPlayer.getX())
					.setY(defPlayer.getY())
					.setPower(defPlayer.getPower())
					.setPfIcon(defPlayer.getPfIcon());
			content.setTarPlayer(tarPlayer);

			MailArmyInfo.Builder playerarmy = MailArmyInfo.newBuilder()
					.setPlayerName(defPlayer.getName())
					.setPlayerId(defPlayer.getId())
					.setGuildName(defPlayer.getGuildName())
					.setGuildTag(defPlayer.getGuildTag());
			for (ArmyEntity army : defPlayer.getData().getArmyEntities()) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
				int free = army.getFree();
				if (free == 0) {
					continue;
				}
				int dead = (int) (free * killPct);
				army.clearFree();
				army.addFree(free - dead);
				army.addWoundedCount(dead);

				content.setKillNum(content.getKillNum() + dead);
				content.setKillPow(content.getKillPow() + cfg.getPower() * dead);
				MailSoldierPB.Builder solBuild = MailSoldierPB.newBuilder()
						.setSoldierId(army.getArmyId())
						.setDefencedCount(army.getFree())
						.setWoundedCnt(dead)
						.setDeadCnt(0);
				playerarmy.addSoldier(solBuild);
			}
			content.addHitPlayers(playerarmy);

			mailReceiverList.add(defPlayer);
			List<ICYBORGWorldMarch> tarMarches = getParent().getPointMarches(tpoint.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST);
			for (ICYBORGWorldMarch march : tarMarches) {
				mailReceiverList.add(march.getParent());
				hitMarch(content, killPct, march);
			}

			// 更新玩家城点
			defPlayer.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT);
			defPlayer.refreshPowerElectric(PowerChangeReason.ARMY_BACK);
			if (content.getKillNum() > 0 && ArmyService.getInstance().getCureFinishCount(defPlayer) <= 0) {
				GameUtil.changeBuildingStatus(defPlayer, Const.BuildingType.HOSPITAL_STATION_VALUE, Const.BuildingStatus.SOLDIER_WOUNDED);
			}
			defPlayer.setOnFireEndTime(GameUtil.getOnFireEndTime(0));
			getParent().worldPointUpdate(defPlayer);
		}

		for (ICYBORGPlayer gamer : getParent().getPlayerList(CYBORGState.GAMEING)) {
			if (Objects.equals(gamer.getGuildId(), shotNuclear.getGuildId())) {
				mailReceiverList.add(gamer);
			}
		}

		if (hit) { // 命中
			for (ICYBORGPlayer gamer : mailReceiverList) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(gamer.getId())
						.setMailId(MailId.CYBORG_NUCLEAR_HIT)
						.addContents(content)
						.setDuntype(DungeonMailType.CYBORG)
						.build());

			}
			try { // log
				List<String> killstrList = content.getHitPlayersList().stream()
						.flatMap(marmyInfo -> marmyInfo.getSoldierList().stream())
						.map(solder -> solder.getSoldierId() + "_" + solder.getWoundedCnt())
						.collect(Collectors.toList());
				String killStr = Joiner.on(",").join(killstrList);
				CYBORGGuildBaseInfo atkCamp = getParent().getCampBase(shotNuclear.getGuildId());
				CYBORGGuildBaseInfo defCamp = getParent().getCampBase(tpoint.getGuildId());
				LogUtil.logCYBORGNuclearHit(getParent().getId(), tpoint.getPointType().name(), tpoint.getX(), tpoint.getY(), atkCamp.campGuild, atkCamp.campGuildName,
						defCamp.campGuild, defCamp.campGuildName, killStr);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		} else {
			for (ICYBORGPlayer gamer : mailReceiverList) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(gamer.getId())
						.setMailId(MailId.CYBORG_NUCLEAR_MIS)
						.addContents(shotNuclear.getTarX(), shotNuclear.getTarY())
						.setDuntype(DungeonMailType.CYBORG)
						.build());
			}
		}
	}

	private void hitMarch(PBCYBORGNuclearHitContent.Builder content, final double killPct, ICYBORGWorldMarch march) {
		ICYBORGPlayer leader = march.getParent();
		MailArmyInfo.Builder playerarmy = MailArmyInfo.newBuilder()
				.setPlayerName(leader.getName())
				.setPlayerId(leader.getId())
				.setGuildName(leader.getGuildName())
				.setGuildTag(leader.getGuildTag());
		List<ArmyInfo> armys = march.getArmys();
		for (ArmyInfo army : armys) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			int free = army.getFreeCnt();
			int dead = (int) (free * killPct);
			army.setWoundedCount(army.getWoundedCount() + dead);

			content.setKillNum(content.getKillNum() + dead);
			content.setKillPow(content.getKillPow() + cfg.getPower() * dead);
			MailSoldierPB.Builder solBuild = MailSoldierPB.newBuilder()
					.setSoldierId(army.getArmyId())
					.setDefencedCount(army.getFreeCnt())
					.setWoundedCnt(dead)
					.setDeadCnt(0);
			playerarmy.addSoldier(solBuild);
		}
		march.getMarchEntity().setArmys(armys);
		march.updateMarch();
		content.addHitPlayers(playerarmy);
	}

	public boolean onNuclearShoot(ICYBORGWorldPoint target, ICYBORGPlayer comdPlayer) {
		if (getState() != CYBORGBuildState.ZHAN_LING) {
			return false;
		}
		long cool = MissileCoolDownTime - controlTime;
		if (cool > 0) {
			return false;
		}
		// 不是本盟的没有权限操作
		if (!comdPlayer.getGuildId().equals(getGuildId())) {
			return false;
		}

		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(comdPlayer.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = comdPlayer.getId().equals(getLeaderMarch().getMarchEntity().getPlayerId());
		if (!guildAuthority && !isLeader) {
			return false;
		}

		// 发射
		PBCYBORGNuclearSync.Builder bul = PBCYBORGNuclearSync.newBuilder();
		bul.setGuildId(getGuildId());
		bul.setSendStart(getParent().getCurTimeMil());
		bul.setSendOver(getParent().getCurTimeMil() + 10000);
		bul.setTarX(target.getX());
		bul.setTarY(target.getY());
		bul.setFromX(getX());
		bul.setFromY(getY());

		this.sendRecord = bul.build();

		List<ICYBORGPlayer> plist = getParent().getPlayerList(CYBORGState.GAMEING);
		for (ICYBORGPlayer player : plist) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_NUCLEAR_SEND_S, bul));
		}

		if (getParent().hasAnchor()) {
			getParent().getAnchor().sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_NUCLEAR_SEND_S, bul));
		}

		controlTime = 0;
		getParent().getCampBase(comdPlayer.getCamp()).campNuclearSendCount++;

		if (target instanceof CYBORGPlayer) {
			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.CYBORG_NUCLEAR_SHOT_PLAYR)
					.addParms(comdPlayer.getGuildTag())
					.addParms(comdPlayer.getGuildName())
					.addParms(((CYBORGPlayer) target).getGuildTag())
					.addParms(((CYBORGPlayer) target).getName()).build();
			getParent().addWorldBroadcastMsg(parames);
		} else {
			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.CYBORG_NUCLEAR_SHOT_BUILD)
					.addParms(comdPlayer.getGuildTag())
					.addParms(comdPlayer.getGuildName())
					.addParms(target.getX())
					.addParms(target.getY()).build();
			getParent().addWorldBroadcastMsg(parames);
		}
		getParent().worldPointUpdate(this);
		if (getParent().IS_GO_MODEL) {
			System.out.println("发射核弹");
		}
		return true;
	}

	@Override
	public WorldPointPB.Builder toBuilder(ICYBORGPlayer viewer) {
		WorldPointPB.Builder result = super.toBuilder(viewer);
		long cool = MissileCoolDownTime - controlTime;
		result.setCyborgNuclearReadyTime(getParent().getCurTimeMil() + cool); // 核弹发射OK时间
		return result;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(ICYBORGPlayer viewer) {
		WorldPointDetailPB.Builder result = super.toDetailBuilder(viewer);
		long cool = MissileCoolDownTime - controlTime;
		result.setCyborgNuclearReadyTime(getParent().getCurTimeMil() + cool); // 核弹发射OK时间
		return result;
	}

	public static CYBORGNuclearMissileSiloCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(CYBORGNuclearMissileSiloCfg.class);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.CYBORG_NUCLEAR_MISSILE_SILO;
	}

	@Override
	public int getControlCountDown() {
		return getCfg().getControlCountDown();
	}

	@Override
	public double getGuildHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getCfg().getGuildHonor() * beiShu;
	}

	@Override
	public double getPlayerHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getCfg().getHonor() * beiShu;
	}

	@Override
	public double getFirstControlGuildHonor() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getCfg().getFirstControlGuildHonor() * beiShu;
	}

	@Override
	public double getFirstControlPlayerHonor() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		return getCfg().getFirstControlHonor() * beiShu;
	}

	@Override
	public int getProtectTime() {
		return getCfg().getProtectTime();
	}

	@Override
	public int getCollectArmyMin() {
		return getCfg().getCollectArmyMin();
	}

}

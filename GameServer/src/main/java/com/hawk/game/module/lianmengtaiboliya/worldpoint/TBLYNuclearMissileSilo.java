package com.hawk.game.module.lianmengtaiboliya.worldpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;

import com.google.common.util.concurrent.AtomicLongMap;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengtaiboliya.ITBLYWorldPoint;
import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager.CAMP;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYNuclearMissileSiloCfg;
import com.hawk.game.module.lianmengtaiboliya.order.TBLYOrderCollection;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.player.TBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.ITBLYWorldMarch;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Mail.MailArmyInfo;
import com.hawk.game.protocol.Mail.MailPlayerInfo;
import com.hawk.game.protocol.Mail.MailSoldierPB;
import com.hawk.game.protocol.Mail.PBTBLYNuclearHitContent;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.TBLY.PBTBLYNuclearSync;
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
public class TBLYNuclearMissileSilo extends ITBLYBuilding {
	private long lastTick;
	private long MissileCoolDownTime = getCfg().getMissileCoolDownTime() * 1000;
	/** 联盟控制时间 , 发射后清0 */
//	private long controlTime = 3600000;
	AtomicLongMap<String> controlTime = AtomicLongMap.create();
	private PBTBLYNuclearSync sendRecord;

	public TBLYNuclearMissileSilo(TBLYBattleRoom parent) {
		super(parent);
		if (getParent().IS_GO_MODEL) {
			MissileCoolDownTime = getCfg().getMissileCoolDownTime() * 1000;
		}
		controlTime.put(parent.getCampAGuild(), 3600000);
		controlTime.put(parent.getCampBGuild(), 3600000);
	}

	@Override
	public void onPlayerLogin(ITBLYPlayer gamer) {
		if (Objects.nonNull(sendRecord)) {
			gamer.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_NUCLEAR_SEND_S, sendRecord.toBuilder()));
		}
	}

	@Override
	public void anchorJoin(ITBLYPlayer gamer) {
		if (Objects.nonNull(sendRecord)) {
			gamer.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_NUCLEAR_SEND_S, sendRecord.toBuilder()));
		}
	}

	/** 核弹准备时间 */
	public long getNuclearControlTime(String guildId) {
		return controlTime.get(guildId);
	}

	@Override
	public boolean onTick() {
		super.onTick();
		long timePass = getParent().getCurTimeMil() - lastTick;
		if (timePass < 1000) {
			return true;
		}
		lastTick = getParent().getCurTimeMil();
		controlTime.addAndGet(getParent().getCampAGuild(), timePass);
		controlTime.addAndGet(getParent().getCampBGuild(), timePass);
		getParent().setNuclearReadyGuild("");
		getParent().setNuclearReadLeader("");
		if (getState() == TBLYBuildState.ZHAN_LING) {
			long cool = MissileCoolDownTime - controlTime.get(getGuildId());
			if (cool < 0) {
				getParent().setNuclearReadyGuild(getGuildId());
				getParent().setNuclearReadLeader(getPlayerId());
			}
		}

		nuclearShootCheck();
		return true;
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
		PBTBLYNuclearSync shotNuclear = sendRecord.toBuilder().build();
		sendRecord = null;

		List<ITBLYPlayer> mailReceiverList = new ArrayList<>();

		int pointId = GameUtil.combineXAndY(shotNuclear.getTarX(), shotNuclear.getTarY());
		ITBLYWorldPoint tpoint = getParent().getWorldPoint(pointId).orElse(null);

		PBTBLYNuclearHitContent.Builder content = PBTBLYNuclearHitContent.newBuilder();
		content.setX(shotNuclear.getTarX());
		content.setY(shotNuclear.getTarY());
		final double killPct = (getCfg().getKillPercentage() + getParent().getCurBuff530Val(EffType.TBLY530_656)) * 0.01;
		boolean hit = false;
		if (tpoint instanceof ITBLYBuilding && !Objects.equals(tpoint.getGuildId(), shotNuclear.getGuildId())) {
			ITBLYWorldMarch leaderMarch = ((ITBLYBuilding) tpoint).getLeaderMarch();
			if (Objects.nonNull(leaderMarch)) {
				content.setGuildname(leaderMarch.getParent().getGuildName());
				content.setGuildTag(leaderMarch.getParent().getGuildTag());
				content.setCamp(leaderMarch.getParent().getCamp().intValue());
			}
			List<ITBLYWorldMarch> tarMarches = getParent().getPointMarches(tpoint.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
			for (ITBLYWorldMarch march : tarMarches) {
				hit = true;
				mailReceiverList.add(march.getParent());
				hitMarch(content, killPct, march);
			}
		} else if (tpoint instanceof ITBLYPlayer && !Objects.equals(tpoint.getGuildId(), shotNuclear.getGuildId())) {
			hit = true;
			TBLYPlayer defPlayer = (TBLYPlayer) tpoint;
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
			List<ITBLYWorldMarch> tarMarches = getParent().getPointMarches(tpoint.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST);
			for (ITBLYWorldMarch march : tarMarches) {
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

		for (ITBLYPlayer gamer : getParent().getPlayerList(TBLYState.GAMEING)) {
			if (Objects.equals(gamer.getGuildId(), shotNuclear.getGuildId())) {
				mailReceiverList.add(gamer);
			}
		}

		if (hit) { // 命中
			for (ITBLYPlayer gamer : mailReceiverList) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(gamer.getId())
						.setMailId(MailId.TBLY_NUCLEAR_HIT)
						.addContents(content)
						.setDuntype(DungeonMailType.TBLY)
						.build());
			}
		} else {
			for (ITBLYPlayer gamer : mailReceiverList) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(gamer.getId())
						.setMailId(MailId.TBLY_NUCLEAR_MIS)
						.addContents(shotNuclear.getTarX(), shotNuclear.getTarY())
						.setDuntype(DungeonMailType.TBLY)
						.build());
			}
		}
	}

	private void hitMarch(PBTBLYNuclearHitContent.Builder content, final double killPct, ITBLYWorldMarch march) {
		ITBLYPlayer leader = march.getParent();
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

	public boolean onNuclearShoot(ITBLYWorldPoint target, ITBLYPlayer comdPlayer) {
		if (getState() != TBLYBuildState.ZHAN_LING) {
			return false;
		}
		long cool = MissileCoolDownTime - controlTime.get(comdPlayer.getGuildId());
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
			comdPlayer.sendError(HP.code.TBLY_NUCLEAR_SEND_C_VALUE, Status.Error.GUILD_LOW_AUTHORITY_VALUE, 0);
			return false;
		}

		// 发射
		PBTBLYNuclearSync.Builder bul = PBTBLYNuclearSync.newBuilder();
		bul.setGuildId(getGuildId());
		bul.setSendStart(getParent().getCurTimeMil());
		bul.setSendOver(getParent().getCurTimeMil() + 10000);
		bul.setTarX(target.getX());
		bul.setTarY(target.getY());
		bul.setFromX(getX());
		bul.setFromY(getY());

		this.sendRecord = bul.build();

		List<ITBLYPlayer> plist = getParent().getPlayerList(TBLYState.GAMEING);
		for (ITBLYPlayer player : plist) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_NUCLEAR_SEND_S, bul));
		}

		for (ITBLYPlayer anchor : getParent().getAnchors()) {
			anchor.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_NUCLEAR_SEND_S, bul));
		}

		controlTime.put(comdPlayer.getGuildId(), 0);
		if (comdPlayer.getCamp() == CAMP.A) {
			getParent().campANuclearSendCount++;
		} else {
			getParent().campBNuclearSendCount++;
		}

		if (target instanceof TBLYPlayer) {
			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.TBLY_NUCLEAR_SHOT_PLAYR)
					.addParms(comdPlayer.getGuildTag())
					.addParms(comdPlayer.getGuildName())
					.addParms(((TBLYPlayer) target).getGuildTag())
					.addParms(((TBLYPlayer) target).getName()).build();
			getParent().addWorldBroadcastMsg(parames);
		} else {
			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.TBLY_NUCLEAR_SHOT_BUILD)
					.addParms(comdPlayer.getGuildTag())
					.addParms(comdPlayer.getGuildName())
					.addParms(target.getX())
					.addParms(target.getY()).build();
			getParent().addWorldBroadcastMsg(parames);
		}

		if (getParent().IS_GO_MODEL) {
			System.out.println("发射核弹");
		}
		getParent().worldPointUpdate(this);
		LogUtil.logTBLYNuclearShot(getParent().getExtParm(), getGuildId());
		return true;
	}

	@Override
	public WorldPointPB.Builder toBuilder(ITBLYPlayer viewer) {
		WorldPointPB.Builder result = super.toBuilder(viewer);
		result.setTblyNuclearReadyTime(getReadyTime(viewer.getGuildId())); // 核弹发射OK时间
		return result;
	}
	
	public long getStartTime(){
		if (Objects.nonNull(sendRecord)) {
			return sendRecord.getSendStart();
		}
		return 0;
	}
	
	public long getReadyTime(String guildId){
		long cool = MissileCoolDownTime - controlTime.get(guildId);
		return  getParent().getCurTimeMil() + cool; // 核弹发射OK时间
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(ITBLYPlayer viewer) {
		WorldPointDetailPB.Builder result = super.toDetailBuilder(viewer);
		long cool = MissileCoolDownTime - controlTime.get(viewer.getGuildId());
		result.setTblyNuclearReadyTime(getParent().getCurTimeMil() + cool); // 核弹发射OK时间
		return result;
	}

	public static TBLYNuclearMissileSiloCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(TBLYNuclearMissileSiloCfg.class);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.TBLY_NUCLEAR_MISSILE_SILO;
	}

	@Override
	public int getControlCountDown() {
		return getCfg().getControlCountDown();
	}

	@Override
	public double getGuildHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		if (getShowOrder().containsKey(TBLYOrderCollection.shuangbeijifen)) {
			beiShu *= 2;
		}
		return getCfg().getGuildHonor() * beiShu;
	}

	@Override
	public double getPlayerHonorPerSecond() {
		int beiShu = getParent().isHotBloodModel() ? 2 : 1;
		if (getShowOrder().containsKey(TBLYOrderCollection.shuangbeijifen)) {
			beiShu *= 2;
		}
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

	@Override
	public int getPointTime() {
		return getCfg().getPointTime();
	}

	@Override
	public double getPointBase() {
		return getCfg().getPointBase();
	}

	@Override
	public double getPointSpeed() {
		return getCfg().getPointSpeed();
	}

	@Override
	public double getPointMax() {
		return getCfg().getPointMax();
	}
}

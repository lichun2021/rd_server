package com.hawk.game.module.lianmengyqzz.battleroom.order;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Mail.MailArmyInfo;
import com.hawk.game.protocol.Mail.MailSoldierPB;
import com.hawk.game.protocol.Mail.PBYQZZNuclearHitContent;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.YQZZ.PBYQZZNuclearSync;
import com.hawk.game.protocol.YQZZ.PBYQZZOrder;
import com.hawk.game.protocol.YQZZ.PBYQZZOrderUseReq;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;

public class YQZZOrder1001 extends YQZZOrder {

	private int tarX;// = 2; // 技能生效目标建筑. 对于快速援助
	private int tarY;// = 3;
	private PBYQZZNuclearSync sendRecord;

	public YQZZOrder1001(YQZZOrderCollection parent) {
		super(parent);
	}

	@Override
	public int canStartOrder(PBYQZZOrderUseReq req, IYQZZPlayer player) {
		IYQZZWorldPoint worldPoint = getParent().getParent().getParent().getWorldPoint(req.getTarX(), req.getTarY()).orElse(null);
		if (worldPoint == null) {
			return Status.SysError.DATA_ERROR_VALUE;
		}
		if (!(worldPoint instanceof IYQZZBuilding)) {
			return Status.SysError.DATA_ERROR_VALUE;
		}
		IYQZZBuilding build = (IYQZZBuilding) worldPoint;
		if (build.getGuildCamp() == player.getCamp()) {
			return Status.SysError.DATA_ERROR_VALUE;
		}

		return super.canStartOrder(req, player);
	}

	@Override
	public YQZZOrder startOrder(PBYQZZOrderUseReq req, IYQZZPlayer player) {
		super.startOrder(req, player);
		IYQZZWorldPoint worldPoint = getParent().getParent().getParent().getWorldPoint(req.getTarX(), req.getTarY()).orElse(null);
		if (worldPoint == null) {
			return this;
		}
		onNuclearShoot(worldPoint, player);
		return this;
	}

	@Override
	public void onTick() {
		try {
			nuclearShootCheck();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public PBYQZZOrder.Builder genPBYQZZOrderBuilder() {
		PBYQZZOrder.Builder result = super.genPBYQZZOrderBuilder();
		return result;
	}

	public boolean onNuclearShoot(IYQZZWorldPoint target, IYQZZPlayer comdPlayer) {

		YQZZBattleRoom battleroom = getParent().getParent().getParent();
		// 发射
		PBYQZZNuclearSync.Builder bul = PBYQZZNuclearSync.newBuilder();
		bul.setGuildId(comdPlayer.getGuildId());
		bul.setSendStart(battleroom.getCurTimeMil());
		bul.setSendOver(battleroom.getCurTimeMil() + 10000);
		bul.setTarX(target.getX());
		bul.setTarY(target.getY());
		bul.setFromX(getParent().getParent().getX());
		bul.setFromY(getParent().getParent().getY());

		this.sendRecord = bul.build();

		List<IYQZZPlayer> plist = battleroom.getPlayerList(YQZZState.GAMEING);
		for (IYQZZPlayer player : plist) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_NUCLEAR_SEND_S, bul));
		}

		ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.YQZZ_NUCLEAR_SHOT_BUILD)
				.addParms(GameUtil.getPresidentOfficerId(comdPlayer.getId()))
				.addParms(comdPlayer.getName())
				.addParms(target.getX())
				.addParms(target.getY()).build();
		battleroom.addWorldBroadcastMsg(parames);
		return true;
	}

	private void nuclearShootCheck() {
		if (Objects.isNull(sendRecord)) {
			return;
		}
		YQZZBattleRoom battleroom = getParent().getParent().getParent();
		if (battleroom.IS_GO_MODEL) {
			System.out.println(sendRecord.getSendOver() - battleroom.getCurTimeMil());
		}
		if (sendRecord.getSendOver() > battleroom.getCurTimeMil()) {
			return;
		}
		// 命中
		PBYQZZNuclearSync shotNuclear = sendRecord.toBuilder().build();
		sendRecord = null;

		List<IYQZZPlayer> mailReceiverList = new ArrayList<>();

		int pointId = GameUtil.combineXAndY(shotNuclear.getTarX(), shotNuclear.getTarY());
		IYQZZWorldPoint tpoint = battleroom.getWorldPoint(pointId).orElse(null);

		PBYQZZNuclearHitContent.Builder content = PBYQZZNuclearHitContent.newBuilder();
		content.setX(shotNuclear.getTarX());
		content.setY(shotNuclear.getTarY());
		final double killPct = getConfig().getP1() * 0.01;
		boolean hit = false;
		IYQZZWorldMarch leaderMarch = ((IYQZZBuilding) tpoint).getLeaderMarch();
		if (Objects.nonNull(leaderMarch) && leaderMarch.getParent().getCamp() != battleroom.getCampBase(shotNuclear.getGuildId()).camp) {
			content.setGuildname(leaderMarch.getParent().getGuildName());
			content.setGuildTag(leaderMarch.getParent().getGuildTag());
			content.setCamp(leaderMarch.getParent().getCamp().intValue());
			List<IYQZZWorldMarch> tarMarches = battleroom.getPointMarches(tpoint.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
			for (IYQZZWorldMarch march : tarMarches) {
				hit = true;
				mailReceiverList.add(march.getParent());
				hitMarch(content, killPct, march);
			}
		}

		for (IYQZZPlayer gamer : battleroom.getPlayerList(YQZZState.GAMEING)) {
			if (Objects.equals(gamer.getGuildId(), shotNuclear.getGuildId())) {
				mailReceiverList.add(gamer);
			}
		}

		if (hit) { // 命中
			for (IYQZZPlayer gamer : mailReceiverList) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(gamer.getId())
						.setMailId(MailId.YQZZ_NUCLEAR_HIT)
						.addContents(content)
						.setDuntype(DungeonMailType.YQZZ)
						.build());

			}
			try { // log
					// List<String> killstrList = content.getHitPlayersList().stream()
					// .flatMap(marmyInfo -> marmyInfo.getSoldierList().stream())
					// .map(solder -> solder.getSoldierId() + "_" + solder.getWoundedCnt())
					// .collect(Collectors.toList());
					// String killStr = Joiner.on(",").join(killstrList);
					// YQZZGuildBaseInfo atkCamp = getParent().getParent().getCampBase(shotNuclear.getGuildId());
					// YQZZGuildBaseInfo defCamp = getParent().getParent().getCampBase(tpoint.getGuildId());
					// LogUtil.logYQZZNuclearHit(getParent().getParent().getId(), tpoint.getPointType().name(), tpoint.getX(), tpoint.getY(), atkCamp.campGuild, atkCamp.campGuildName,
					// defCamp.campGuild, defCamp.campGuildName, killStr);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		} else {
			for (IYQZZPlayer gamer : mailReceiverList) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(gamer.getId())
						.setMailId(MailId.YQZZ_NUCLEAR_MIS)
						.addContents(shotNuclear.getTarX(), shotNuclear.getTarY())
						.setDuntype(DungeonMailType.YQZZ)
						.build());
			}
		}
	}

	private void hitMarch(PBYQZZNuclearHitContent.Builder content, final double killPct, IYQZZWorldMarch march) {
		IYQZZPlayer leader = march.getParent();
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

	public int getTarX() {
		return tarX;
	}

	public void setTarX(int tarX) {
		this.tarX = tarX;
	}

	public int getTarY() {
		return tarY;
	}

	public void setTarY(int tarY) {
		this.tarY = tarY;
	}

}

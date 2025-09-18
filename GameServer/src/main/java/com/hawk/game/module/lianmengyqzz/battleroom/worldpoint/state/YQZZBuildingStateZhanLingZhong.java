package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.state;

import java.util.List;
import java.util.Objects;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZGuildBaseInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZBuildStayTime;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildState;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.YQZZDeclareWar;
import com.hawk.game.protocol.World.YQZZHoldRec;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.LogUtil;

public class YQZZBuildingStateZhanLingZhong extends IYQZZBuildingState {
	private long zhanlingStart; // 占领开始
	private long zhanlingJieshu;
	private YQZZDeclareWar lastGuildid;
	private long lastTick;

	public YQZZBuildingStateZhanLingZhong(IYQZZBuilding build) {
		super(build);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() {
		this.zhanlingStart = getParent().getParent().getCurTimeMil();
		this.zhanlingJieshu = getParent().getControlCountDown() * 1000 + zhanlingStart;
		this.lastTick = this.zhanlingStart;
	}

	@Override
	public boolean onTick() {
		long timePass = getParent().getParent().getCurTimeMil() - lastTick;
		lastTick = getParent().getParent().getCurTimeMil();
		this.addStayTime(timePass);
		IYQZZWorldMarch leaderMarch = getParent().getLeaderMarch();
		if (Objects.isNull(leaderMarch)) {
			if (Objects.equals(YQZZConst.YURI_GUILD, getParent().getOnwerGuildId())) {
				getParent().setStateObj(new YQZZBuildingStateYuriZhanLing(getParent()));
			} else {
				getParent().setStateObj(new YQZZBuildingStateZhanLing(getParent()));
			}
			return true;
		}
		if (Objects.isNull(lastGuildid)) {
			lastGuildid = getParent().getDeclareWarRecord(leaderMarch.getParent().getGuildId()).get();
		}
		if (!Objects.equals(leaderMarch.getParent().getGuildId(), lastGuildid.getGuildId())) {
			getParent().setStateObj(new YQZZBuildingStateZhanLingZhong(getParent()));
			// 广播通知(主动占领未成功)
			ChatParames paramesAtk = ChatParames.newBuilder()
					.setChatType(ChatType.CHAT_FUBEN_TEAM_SPECIAL_BROADCAST)
					.setKey(NoticeCfgId.YQZZ_GUILD_CONTORY_BUILD_FAIL)
					.setGuildId(lastGuildid.getGuildId())
					.addParms(getParent().getX())
					.addParms(getParent().getY())
					.build();
			getParent().getParent().addWorldBroadcastMsg(paramesAtk);
			return true;
		}

		if (Objects.equals(leaderMarch.getParent().getGuildId(), getParent().getOnwerGuildId())) {
			getParent().setStateObj(new YQZZBuildingStateZhanLing(getParent()));
			// 广播通知(防守成功)

			ChatParames paramesAtk = ChatParames.newBuilder()
					.setChatType(ChatType.CHAT_FUBEN_TEAM_SPECIAL_BROADCAST)
					.setKey(NoticeCfgId.YQZZ_GUILD_DEFEND_BUILD_SUCESS)
					.setGuildId(getParent().getGuildId())
					.addParms(this.lastGuildid.getServerId())
					.addParms(this.lastGuildid.getGuildName())
					.addParms(getParent().getX())
					.addParms(getParent().getY())
					.build();
			getParent().getParent().addWorldBroadcastMsg(paramesAtk);
			return true;
		}

		if (getParent().getParent().getCurTimeMil() > zhanlingJieshu) {
			if (getParent().getBuildTypeCfg().getOccupyProTime() > 0) {
				getParent().setProtectedEndTime(zhanlingJieshu + getParent().getBuildTypeCfg().getOccupyProTime() * 1000);
				getParent().clearDeclareWarRecord();
			} else {
				getParent().clearDeclareWarRecord(lastGuildid.getGuildId());
			}

			String lastOwnerGuild = getParent().getOnwerGuildId();
			getParent().setOnwerGuild(lastGuildid);
			getParent().setStateObj(new YQZZBuildingStateZhanLing(getParent()));

			// 占领记录
			YQZZHoldRec.Builder hrec = YQZZHoldRec.newBuilder()
					.setGuildName(getParent().getGuildName())
					.setHoldTime(zhanlingJieshu)
					.setPlayerName(getParent().getPlayerName())
					.setGuildTag(getParent().getGuildTag())
					.setPtype(getParent().getPointType())
					.setX(getParent().getX())
					.setY(getParent().getY())
					.setGuildId(getParent().getGuildId())
					.setServerId(getParent().getGuildServerId());
			getParent().getHoldRecList().add(hrec);
			sendNotice376(lastOwnerGuild);
			return true;
		}

		return true;
	}

	private void sendNotice376(String lastOwnerGuild) {
		// 广播通知(主动占领成功)
		ChatParames paramesAtk = ChatParames.newBuilder()
				.setChatType(ChatType.CHAT_FUBEN_TEAM_SPECIAL_BROADCAST)
				.setKey(NoticeCfgId.YQZZ_GUILD_CONTORY_BUILD)
				.setGuildId(getParent().getOnwerGuildId())
				.addParms(getParent().getX())
				.addParms(getParent().getY())
				.build();
		getParent().getParent().addWorldBroadcastMsg(paramesAtk);
		// 广播通知(被动防守失败)
		if (!HawkOSOperator.isEmptyString(lastOwnerGuild)) {
			ChatParames paramesDef = ChatParames.newBuilder()
					.setChatType(ChatType.CHAT_FUBEN_TEAM_SPECIAL_BROADCAST)
					.setKey(NoticeCfgId.YQZZ_GUILD_DEFEND_BUILD_FAIL)
					.setGuildId(lastOwnerGuild)
					.addParms(lastGuildid.getServerId())
					.addParms(lastGuildid.getGuildName())
					.addParms(getParent().getX())
					.addParms(getParent().getY())
					.build();
			getParent().getParent().addWorldBroadcastMsg(paramesDef);
		}
		// 广播通知(控制权转换)
		if (getParent().getCfg().getBuildTypeId() >= 1) {
			if (!HawkOSOperator.isEmptyString(lastOwnerGuild)) {
				ChatParames paramesDef = ChatParames.newBuilder()
						.setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST)
						.setKey(NoticeCfgId.YQZZ_BUILD_CONTROL_CHANGE)
						.addParms(lastGuildid.getServerId())
						.addParms(lastGuildid.getGuildName())
						.addParms(getParent().getX())
						.addParms(getParent().getY())
						.build();
				getParent().getParent().addWorldBroadcastMsg(paramesDef);
			}
		}

		String guildControBef = "";
		String guildNameControBef = "";
		String guildServerBef = "";
		if (!HawkOSOperator.isEmptyString(lastOwnerGuild)) {
			YQZZGuildBaseInfo binfo = getParent().getParent().getCampBase(lastOwnerGuild);
			if (binfo != null) {
				guildControBef = binfo.campGuild;
				guildNameControBef = binfo.campGuildName;
				guildServerBef = binfo.campServerId;
			}
		}
		LogUtil.logYQZZBuildControlChange(guildControBef, guildNameControBef, guildServerBef,
				lastGuildid.getGuildId(), lastGuildid.getGuildName(), lastGuildid.getServerId(), this.getParent().getCfgId(), this.getParent().getParent().getId());

	}

	@Override
	public YQZZBuildState getState() {
		// TODO Auto-generated method stub
		return YQZZBuildState.ZHAN_LING_ZHONG;
	}

	@Override
	public void fillBuilder(WorldPointPB.Builder builder) {
		builder.setManorBuildTime(zhanlingStart); // 占领开始时间
		builder.setManorComTime(zhanlingJieshu); // 占领结束时间
	}

	@Override
	public void fillDetailBuilder(WorldPointDetailPB.Builder builder) {
		builder.setManorBuildTime(zhanlingStart); // 占领开始时间
		builder.setManorComTime(zhanlingJieshu); // 占领结束时间

	}

	/**
	 * 累计玩家驻防时间
	 * @param timePass
	 */
	private void addStayTime(long timePass) {
		if (!getParent().getParent().getBattleStageTime().getCurStage().getStageOpenBuildList().contains(getParent().getBuildTypeId())) {
			return;
		}
		List<IYQZZWorldMarch> stayMarches = getParent().getParent().getPointMarches(getParent().getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		for (IYQZZWorldMarch march : stayMarches) {
			YQZZBuildStayTime stayTime = march.getParent().getBuildControlTimeStat(getParent().getBuildType());
			stayTime.setTimes(stayTime.getTimes() + timePass);
		}
	}
}

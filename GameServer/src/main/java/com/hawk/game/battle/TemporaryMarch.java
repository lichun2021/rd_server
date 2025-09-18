package com.hawk.game.battle;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hawk.db.entifytype.EntityType;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo.Builder;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.World.AttackMarchReportPB;
import com.hawk.game.protocol.World.MarchReportPB;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRefreshPB;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldPointService;

public class TemporaryMarch implements IWorldMarch, IReportPushMarch {
	private Player player;
	private List<ArmyInfo> armys = Collections.emptyList();
	private List<PlayerHero> heros = Collections.emptyList();
	private Set<String> viewerIds = Collections.emptySet();
	private WorldMarch marchEntity;
	private long startTime;

	private long endTime;
	private String marchId = "";
	private String assistantStr = "";
	private int origionId;
	private int terminalId;
	private WorldMarchType marchType;

	public TemporaryMarch(){
		this.marchEntity = new WorldMarch();
		this.marchEntity.setPersistable(false);
		this.marchEntity.setEntityType(EntityType.TEMPORARY);
	}
	
	public void pushAttackReport(Player receiver) {
		final WorldPoint point = WorldPointService.getInstance().getWorldPoint(this.getTerminalX(), this.getTerminalY());
		AttackMarchReportPB.Builder attReportBuilder = this.assembleEnemyMarchInfo(receiver, Collections.emptySet());
		MarchReportPB.Builder marchReport = MarchReportPB.newBuilder();
		marchReport.setAttackReport(attReportBuilder);
		marchReport.setTargetType(getMarchTargetPointType(receiver, point).getNumber());
		receiver.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_REPORT_PUSH, marchReport));
	}

	public void removeAttackReport(Player receiver) {
		// 在线即推送给客户端
		if (receiver == null || !receiver.isActiveOnline()) {
			return;
		}
		WorldMarchRefreshPB.Builder builder = WorldMarchRefreshPB.newBuilder();
		builder.setMarchId(getMarchId());
		receiver.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_END_PUSH, builder));

	}

	// /** 加一条行军线 */
	// public void addMarchLine() {
	// MarchEventSync.Builder builder = MarchEventSync.newBuilder();
	// builder.setEventType(MarchEvent.MARCH_ADD_VALUE);
	// MarchData.Builder dataBuilder = MarchData.newBuilder();
	// dataBuilder.setMarchId(this.getMarchId());
	// dataBuilder.setMarchPB(toBuilder());
	//
	// builder.addMarchData(dataBuilder);
	// player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_EVENT_SYNC_VALUE,
	// builder));
	//
	// }
	//
	// /** 通知客户端删除行军线 */
	// public void removeMarchLine() {
	// MarchEventSync.Builder builder = MarchEventSync.newBuilder();
	// builder.setEventType(MarchEvent.MARCH_DELETE_VALUE);
	// MarchData.Builder dataBuilder = MarchData.newBuilder();
	// dataBuilder.setMarchId(this.getMarchId());
	// builder.addMarchData(dataBuilder);
	// player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_EVENT_SYNC_VALUE,
	// builder));
	// }

	@Override
	public Set<String> calcPointViewers(int viewRadiusX, int viewRadiusY) {
		return viewerIds;
	}

	public WorldMarchPB.Builder toBuilder() {
		WorldMarchPB.Builder builder = WorldMarchPB.newBuilder();
		builder.setPlayerId(player.getId());
		builder.setEndTime(endTime);

		if (!HawkOSOperator.isEmptyString(player.getName())) {
			builder.setPlayerName(player.getName());
		} else {
			builder.setPlayerName(GlobalData.getInstance().getPlayerNameById(player.getName()));
		}

		String guildTag = GuildService.getInstance().getPlayerGuildTag(player.getId());
		if (guildTag != null && !"".equals(guildTag)) {
			builder.setGuildTag(guildTag);
		}

		for (PlayerHero hero : heros) {
//			builder.addHeroId(hero.getCfgId());
//			builder.addHeroLvl(hero.getLevel());
			builder.addHeroList(hero.toPBobj());
		}

		for (int i = 0; i < armys.size(); i++) {
			ArmyInfo info = armys.get(i);
			if (info == null) {
				continue;
			}
			if (info.getFreeCnt() <= 0) {
				continue;
			}
			builder.addArmy(info.toArmySoldierPB(player).build());
		}

		builder.setMarchId(marchId);
		builder.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH);
		builder.setMarchType(WorldMarchType.GUILD_HOSPICE);

		builder.setOrigionX(getOrigionX());
		builder.setOrigionY(getOrigionY());

		builder.setStartTime(startTime);
		builder.setTargetId("");
		builder.setTerminalX(getTerminalX());
		builder.setTerminalY(getTerminalY());
		
		ArmourSuitType armourSuit = ArmourSuitType.valueOf(marchEntity.getArmourSuit());
		if (armourSuit != null) {
			builder.setArmourSuit(armourSuit);
		}
		
		return builder;
	}

	@Override
	public void pushAttackReport() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ArmyInfo> getArmys() {
		return armys;
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public List<PlayerHero> getHeros() {
		return heros;
	}

	@Override
	public int compareTo(IWorldMarch o) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void register() {
		throw new UnsupportedOperationException();

	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();

	}

	@Override
	public String getMarchId() {
		return marchId;
	}

	@Override
	public void heartBeats() {
		throw new UnsupportedOperationException();

	}

	@Override
	public int getSuperSoldierId() {
		return 0;
	}

	@Override
	public WorldMarchType getMarchType() {
		return marchType;
	}

	@Override
	public boolean isPassiveMarch() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPlayerId() {
		return player.getId();
	}

	@Override
	public WorldMarch getMarchEntity() {
		return marchEntity;
	}

	@Override
	public boolean isReturnBackMarch() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isMarchState() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isMassMarch() {
		return false;
	}

	@Override
	public boolean isMassJoinMarch() {
		return false;
	}

	@Override
	public boolean isAssistanceMarch() {
		return false;
	}

	@Override
	public boolean isPresidentMarch() {
		return false;
	}

	@Override
	public boolean isPresidentTowerMarch() {
		return false;
	}

	@Override
	public boolean isManorMarch() {
		return false;
	}

	@Override
	public int getMaxMassJoinSoldierNum(Player leader) {
		return 0;
	}
	
	@Override
	public int getMaxMassJoinSoldierNum(Player leader, Player perReachMarchPlayer) {
		return 0;
	}
	
	@Override
	public boolean isReachAndStopMarch() {
		return false;
	}

	@Override
	public boolean isManorMarchReachStatus() {
		return false;
	}

	@Override
	public boolean isNeedCalcTickMarch() {
		return false;
	}

	@Override
	public long getMarchNeedTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getMarchBaseSpeed() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onMarchStart() {
		throw new UnsupportedOperationException();

	}

	@Override
	public void onMarchReach(Player player) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void onMarchReturn() {
		throw new UnsupportedOperationException();

	}

	@Override
	public void onMarchCallback(long callbackTime, WorldPoint worldPoint) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void onMarchStop(int status, List<ArmyInfo> armys, WorldPoint targetPoint) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void updateMarch() {
		throw new UnsupportedOperationException();

	}

	@Override
	public boolean needShowInGuildWar() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Builder getGuildWarInitiativeInfo() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Builder getGuildWarPassivityInfo() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean checkGuildWarShow() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean doCollectRes(boolean changeSpeed) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void doQuitGuild(String guildId) {
		throw new UnsupportedOperationException();

	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setArmys(List<ArmyInfo> armys) {
		marchEntity.setArmys(armys);
		this.armys = armys;
	}

	public void setHeros(List<PlayerHero> heros) {
		this.heros = heros;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void setMarchId(String marchId) {
		this.marchId = marchId;
	}

	@Override
	public boolean isSuperWeaponMarch() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isGuildSpaceMarch() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isWarFlagMarch() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void targetMoveCityProcess(Player targetPlayer, long currentTime) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean beforeImmediatelyRemoveMarchProcess(Player player) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void moveCityProcess(long currentTime) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttackReport() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> attackReportRecipients() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMarchStatus() {
		return WorldMarchStatus.MARCH_STATUS_MARCH_VALUE;
	}

	@Override
	public int getAlarmPointId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getTerminalY() {
		return GameUtil.splitXAndY(terminalId)[1];
	}

	@Override
	public int getTerminalX() {
		return GameUtil.splitXAndY(terminalId)[0];
	}

	@Override
	public int getTerminalId() {
		return terminalId;
	}

	@Override
	public long getMassReadyTime() {
		return 0;
	}

	@Override
	public String getAssistantStr() {
		return assistantStr;
	}

	@Override
	public int getOrigionX() {
		return GameUtil.splitXAndY(origionId)[0];
	}

	@Override
	public int getOrigionY() {
		return GameUtil.splitXAndY(origionId)[1];
	}

	@Override
	public int getOrigionId() {
		return origionId;
	}

	public void setAssistantStr(String assistantStr) {
		this.assistantStr = assistantStr;
	}

	public void setOrigionId(int origionId) {
		this.origionId = origionId;
	}

	public void setTerminalId(int terminalId) {
		this.terminalId = terminalId;
	}

	public void setMarchType(WorldMarchType marchType) {
		this.marchType = marchType;
	}

	public Set<String> getViewerIds() {
		return viewerIds;
	}

	public void setViewerIds(Set<String> viewerIds) {
		this.viewerIds = viewerIds;
	}

	@Override
	public boolean isFortressMarch() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<IWorldMarch> getQuarterMarch() {
		return Collections.emptySet();
	}

	@Override
	public boolean needShowInNationWar() {
		// TODO Auto-generated method stub
		return false;
	}
}

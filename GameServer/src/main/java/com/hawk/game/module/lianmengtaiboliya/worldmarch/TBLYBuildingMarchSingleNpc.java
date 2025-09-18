package com.hawk.game.module.lianmengtaiboliya.worldmarch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengtaiboliya.ITBLYWorldPoint;
import com.hawk.game.module.lianmengtaiboliya.npc.TBLYNpcPlayer;
import com.hawk.game.module.lianmengtaiboliya.order.TBLYOrderCollection;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.submarch.ITBLYPassiveAlarmTriggerMarch;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.submarch.ITBLYReportPushMarch;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.ITBLYBuilding;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYBuildState;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYCommandPost;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.sub.TBLYAtkBuildBuff;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.GuildWar.GuildWarTeamInfo;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.PBTBLYBuildSkill;
import com.hawk.game.protocol.World.WorldMarchDeletePush;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;

/**
 * 单人铁幕装置
 */
public class TBLYBuildingMarchSingleNpc extends ITBLYWorldMarch implements ITBLYReportPushMarch, ITBLYPassiveAlarmTriggerMarch {
	private WorldMarchType marchType;
	private Set<ITBLYWorldMarch> joinMarchList = new HashSet<>();
	public TBLYBuildingMarchSingleNpc(ITBLYPlayer parent) {
		super(parent);
	}

	@Override
	public void onMarchStart() {
	}

	@Override
	public boolean needShowInGuildWar() {
		return true;
	}

	@Override
	public WorldMarchType getMarchType() {
		return marchType;
	}

	@Override
	public Set<ITBLYWorldMarch> getMassJoinMarchs(boolean b) {
		return joinMarchList;
	}

	@Override
	public long getMarchNeedTime() {
		return TBLYCommandPost.getCfg().getNpcMarchTime() * 1000;
	}

	@Override
	public Set<String> attackReportRecipients() {
		// 防守方援军
		List<ITBLYWorldMarch> helpMarchList = getParent().getParent().getPointMarches(getMarchEntity().getTerminalId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
		Set<String> result = new HashSet<>();
		result.add(getMarchEntity().getTargetId());
		for (ITBLYWorldMarch march : helpMarchList) {
			result.add(march.getPlayerId());
		}
		return result;
	}

	@Override
	public void heartBeats() {
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			return;
		}
		// 当前时间
		long currTime = HawkApp.getInstance().getCurrentTime();
		// 行军或者回程时间未结束
		if (getMarchEntity().getEndTime() > currTime) {
			return;
		}
		// 行军返回到达
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			this.remove();
			return;
		}

		// 行军到达
		onMarchReach(getParent());

	}

	@Override
	public void onMarchReach(Player player) {

		ITBLYBuilding point = (ITBLYBuilding) getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		if(point.getGuildCamp() == getParent().getCamp()){
			rePushPointReport();
			this.remove();
			return;	
		}
		
		point.onMarchReach(this);
		rePushPointReport();
		this.remove();

		if (point.getState() != TBLYBuildState.ZHONG_LI && point.getGuildCamp() != getParent().getCamp()) {
			TBLYNpcPlayer npc = (TBLYNpcPlayer) getParent();
			{
				TBLYAtkBuildBuff buff = new TBLYAtkBuildBuff();
				buff.setBuildAtkBuffMap(npc.getCfg().getBuildAtkBuffMap());
				buff.setNpcId(npc.getCfgId());
				buff.setStartTime(point.getParent().getCurTimeMil());
				buff.setEndTime(point.getParent().getCurTimeMil() + npc.getCfg().getAtkBuffTime() * 1000);
				point.setAtkBuff(buff);

			}
			{
				PBTBLYBuildSkill buff = PBTBLYBuildSkill.newBuilder()
						.setSkillId(TBLYOrderCollection.atkBuff)
						.setNpcCfgId(npc.getCfgId())
						.setCamp(npc.getCamp().intValue())
						.setX(point.getX())
						.setY(point.getY())
						.setStartTime(point.getParent().getCurTimeMil())
						.setEndTime(point.getParent().getCurTimeMil() + npc.getCfg().getAtkBuffTime() * 1000)
						.build();
				point.getShowOrder().put(TBLYOrderCollection.atkBuff, buff);
			}
			
		}
		getParent().getParent().worldPointUpdate(point);
	}

	@Override
	public void onMarchReturn() {
		// 删除行军报告
		this.removeAttackReport();
		this.remove();
	}

	public boolean onMarchReturn(int origionId, int terminalId, List<ArmyInfo> selfArmys) {
		// 删除行军报告
		this.removeAttackReport();
		this.remove();
		return true;
	}

	/** 获取被动方联盟战争界面信息 */
	@Override
	public GuildWarTeamInfo.Builder getGuildWarPassivityInfo() {
		ITBLYWorldPoint point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		ITBLYBuilding build = (ITBLYBuilding) point;

		return build.getGuildWarPassivityInfo();
	}

	@Override
	public void onMarchCallback() {
		super.onMarchCallback();
	}

	@Override
	public int getLeaderMaxMassJoinSoldierNum(Player leader) {
		return 0;
	}

	@Override
	public int getMaxMassJoinSoldierNum(Player leader, Player perReachMarchPlayer) {
		return 0;
	}

	private void rePushPointReport() {
		// 删除行军报告
		removeAttackReport();
		// this.pullAttackReport();
	}

	@Override
	public void pullAttackReport() {
		List<ITBLYWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (ITBLYWorldMarch march : marchList) {
			if (march instanceof ITBLYReportPushMarch && march != this) {
				((ITBLYReportPushMarch) march).pushAttackReport();
			}
		}
	}

	@Override
	public void pullAttackReport(String playerId) {
		List<ITBLYWorldMarch> marchList = getParent().getParent().getPointMarches(getAlarmPointId());
		for (ITBLYWorldMarch march : marchList) {
			if (march instanceof ITBLYReportPushMarch && march != this) {
				((ITBLYReportPushMarch) march).pushAttackReport(playerId);
			}
		}
	}

	@Override
	public void onMarchBack() {

		this.remove();

	}

	public void setMarchType(WorldMarchType marchType) {
		this.marchType = marchType;
	}
	
	@Override
	public void remove() {
		try {
			if (this instanceof ITBLYReportPushMarch) {
				((ITBLYReportPushMarch) this).removeAttackReport();
			}
			getMarchEntity().setMarchStatus(0);
			WorldMarchDeletePush.Builder builder = WorldMarchDeletePush.newBuilder();
			builder.setMarchId(getMarchId());
			builder.setRelation(WorldMarchRelation.SELF);
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_DELETE_PUSH_VALUE, builder);
			getParent().sendProtocol(protocol);

			getParent().getParent().removeMarch(this);
			notifyMarchEvent(MarchEvent.MARCH_DELETE);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}

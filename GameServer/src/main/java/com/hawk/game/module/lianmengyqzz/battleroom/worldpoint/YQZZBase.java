package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint;

import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZGuildBaseInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZ_CAMP;
import com.hawk.game.module.lianmengyqzz.battleroom.order.YQZZOrder;
import com.hawk.game.module.lianmengyqzz.battleroom.order.YQZZOrderCollection;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.tech.NationTechCenter;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.YQZZ.CrossNationTechSkillUse;

/**
 * 月球基地
 *
 */
public class YQZZBase extends IYQZZBuilding {
	private YQZZ_CAMP bornCamp;
	private String serverId = "";
	private int nationPlayerHonor;
	private int nationTechValue;
	private YQZZOrderCollection orderCollection;
	public Deque<ChatMsg> nationMsgCache = new ConcurrentLinkedDeque<>();
	public Set<YQZZBuildType> controlBuildTypes = new HashSet<>();
	public int pylonCnt;
	
	public YQZZBase(YQZZBattleRoom parent) {
		super(parent);
	}
	
	public void updataNationSkill(YQZZOrder order){
		CrossNationTechSkillUse.Builder builder = CrossNationTechSkillUse.newBuilder();
		builder.setTechId(order.getConfig().getTechId());
		builder.setEffectStartTime(order.getEffectStartTime());
		builder.setEfectEndTime(order.getEffectEndTime());
		builder.setCdTime(order.getCdTime());
		HawkProtocol protocol = HawkProtocol.valueOf(CHP.code.CROSS_NATATION_TECH_SKILL_USE, builder);
		CrossProxy.getInstance().sendNotify(protocol, serverId, "");
	}

	/**
	 * 获取国家科技等级
	 * @param techId
	 * @return
	 */
	public int getNationTechLevel(int techId) {
		NationTechCenter center = (NationTechCenter) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (GlobalData.getInstance().isLocalServer(serverId)) {
			return center.getNationTechLevel(techId);
		}
		return center.getCrossCache(serverId).getNationTech(techId);
	}

	/**
	 * 获取国家科技研究值
	 * @return
	 */
	public int getNationTechValue() {
		return nationTechValue;
	}

	public void setNationTechValue(int nationTechValue) {
		this.nationTechValue = nationTechValue;
	}

	/**
	 * 尝试增加建筑值
	 * @param changeValue(可能为负数)
	 */
	public boolean changeNationTechValue(int changeValue) {
		nationTechValue += changeValue;
		return true;
	}

	public YQZZ_CAMP getBornCamp() {
		return bornCamp;
	}

	@Override
	public boolean onTick() {
		orderCollection.onTick();
		return true;
	}

	public void setBornCamp(YQZZ_CAMP bornCamp) {
		this.bornCamp = bornCamp;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	@Override
	public boolean underGuildControl(String guildId) {
		YQZZGuildBaseInfo base = getParent().getCampBase(guildId);
		if (base == null) {
			return false;
		}
		return base.camp == bornCamp;
	}
	
	public boolean underNationControl(String guildId) {
		return bornCamp == getParent().getCampBase(guildId).camp;
	}

	@Override
	public WorldPointPB.Builder toSecondMapBuilder() {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(getX());
		builder.setPointY(getY());
		builder.setPointType(getPointType());
		builder.setManorState(getState().intValue()); // /**中立*/(0),/**占领中*(1),/**已占*/(2);
		builder.setFlagView(bornCamp.intValue()); // 1 红 ,2 蓝
		builder.setServerId(serverId);
		return builder;
	}

	@Override
	public WorldPointPB.Builder toBuilder(IYQZZPlayer viewer) {
		// TODO Auto-generated method stub
		WorldPointPB.Builder builder = super.toBuilder(viewer);
		builder.setFlagView(bornCamp.intValue()); // 1 红 ,2 蓝
		builder.setProtectedEndTime(Long.MAX_VALUE);
		builder.setServerId(serverId);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IYQZZPlayer viewer) {
		// TODO Auto-generated method stub
		WorldPointDetailPB.Builder builder = super.toDetailBuilder(viewer);
		builder.setFlagView(bornCamp.intValue()); // 1 红 ,2 蓝
		builder.setProtectedEndTime(Long.MAX_VALUE);
		builder.setServerId(serverId);
		return builder;
	}

	public void addNationMsgCache(ChatMsg msg) {
		try {
			nationMsgCache.addFirst(msg);
			if (nationMsgCache.size() > 50) {
				nationMsgCache.removeLast();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	public YQZZOrderCollection getOrderCollection() {
		return orderCollection;
	}

	public void setOrderCollection(YQZZOrderCollection orderCollection) {
		this.orderCollection = orderCollection;
	}

	public int getNationPlayerHonor() {
		return nationPlayerHonor;
	}

	public void setNationPlayerHonor(int nationPlayerHonor) {
		this.nationPlayerHonor = nationPlayerHonor;
	}

}

package com.hawk.game.module.lianmengXianquhx.worldpoint;

import java.util.Objects;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.lianmengXianquhx.IXQHXWorldPoint;
import com.hawk.game.module.lianmengXianquhx.XQHXBattleRoom;
import com.hawk.game.module.lianmengXianquhx.cfg.XQHXPylonCfg;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.module.lianmengXianquhx.worldmarch.XQHXPylonMarch;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldPoint;

public class XQHXPylon implements IXQHXWorldPoint {
	private int cfgId;
	private int pylonId;
	private int x;
	private int y;
	private final XQHXBattleRoom parent;

	private XQHXPylonMarch collectMarch;

	private XQHXPylon(XQHXBattleRoom parent) {
		this.parent = parent;
	}

	public static XQHXPylon create(XQHXBattleRoom parent, XQHXPylonCfg monstercfg) {
		XQHXPylon result = new XQHXPylon(parent);
		result.cfgId = monstercfg.getId();
		result.pylonId = monstercfg.getPylonId();
		return result;
	}

	@Override
	public boolean onTick() {
		if (collectMarch != null && collectMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE) {
			collectMarch = null;
			getParent().worldPointUpdate(this);
		}
		return true;
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.XQHX_PYLON;
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
	public WorldPointPB.Builder toBuilder(IXQHXPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setResourceId(pylonId);
		builder.setStrongpointId(pylonId);
		if (collectMarch != null) {
			IXQHXPlayer owner = collectMarch.getParent();
			String thisPlayerId = owner.getId();
			builder.setPlayerId(thisPlayerId);
			builder.setPlayerName(owner.getName());
			builder.setCityLevel(owner.getCityLv());
			builder.setMarchId(collectMarch.getMarchId());
			builder.setServerId(collectMarch.getParent().getMainServerId());
			builder.setGuildId(owner.getGuildId());
			builder.setGuildTag(owner.getGuildTag());
			builder.setGuildFlag(owner.getGuildFlag());
			builder.setManorState(XQHXBuildState.ZHAN_LING.intValue());
			builder.setFlagView(collectMarch.getParent().getCamp().intValue()); // 1 红 2 蓝
			BuilderUtil.buildMarchEmotion(builder, collectMarch.getMarchEntity());

			builder.setLastActiveTime(collectMarch.getMarchEntity().getResEndTime() - getParent().getCurTimeMil());
		}
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IXQHXPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setResourceId(pylonId);
		builder.setStrongpointId(pylonId);
		if (collectMarch != null) {
			IXQHXPlayer owner = collectMarch.getParent();
			String thisPlayerId = owner.getId();
			builder.setPlayerId(thisPlayerId);
			builder.setPlayerName(owner.getName());
			builder.setCityLevel(owner.getCityLv());
			builder.setMarchId(collectMarch.getMarchId());
			builder.setServerId(collectMarch.getParent().getMainServerId());
			builder.setGuildId(owner.getGuildId());
			builder.setGuildTag(owner.getGuildTag());
			builder.setGuildFlag(owner.getGuildFlag());
			builder.setManorState(XQHXBuildState.ZHAN_LING.intValue());
			builder.setFlagView(collectMarch.getParent().getCamp().intValue()); // 1 红 2 蓝
			BuilderUtil.buildMarchEmotion(builder, collectMarch.getMarchEntity());

			builder.setLastActiveTime(collectMarch.getMarchEntity().getResEndTime() - getParent().getCurTimeMil());
		}
		
		return builder;
	}

	public XQHXPylonCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(XQHXPylonCfg.class, cfgId);
	}

	@Override
	public boolean needJoinGuild() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public XQHXBattleRoom getParent() {
		return parent;
	}

	@Override
	public void removeWorldPoint() {

		getParent().removeViewPoint(this);
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	@Override
	public int getGridCnt() {
		return 2;
	}

	public WorldPoint getEntity() {
		WorldPoint worldPoint = new WorldPoint();
		worldPoint.setId(getPointId());
		worldPoint.setX(x);
		worldPoint.setY(y);
		worldPoint.setResourceId(cfgId);
		return worldPoint;
	}
	
	public boolean underGuildControl(String guildId) {
		return Objects.equals(getGuildId(), guildId);
	}


	public int getResourceId() {
		return pylonId;
	}

	public XQHXPylonMarch getCollectMarch() {
		return collectMarch;
	}

	public void setCollectMarch(XQHXPylonMarch collectMarch) {
		this.collectMarch = collectMarch;
	}


	@Override
	public String getGuildId() {
		if (collectMarch == null) {
			return "";
		}
		return collectMarch.getParent().getGuildId();
	}


	public String getPlayerId() {
		if (collectMarch == null) {
			return "";
		}
		return collectMarch.getParent().getId();
	}

	public String getPlayerName() {
		if (collectMarch == null) {
			return "";
		}
		return collectMarch.getParent().getName();
	}

}

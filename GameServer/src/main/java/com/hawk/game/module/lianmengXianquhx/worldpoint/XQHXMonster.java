package com.hawk.game.module.lianmengXianquhx.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.lianmengXianquhx.IXQHXWorldPoint;
import com.hawk.game.module.lianmengXianquhx.XQHXBattleRoom;
import com.hawk.game.module.lianmengXianquhx.cfg.XQHXMonsterCfg;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;

public class XQHXMonster implements IXQHXWorldPoint {
	private int cfgId;
	private int x;
	private int y;
	private int monsterId;
	private final XQHXBattleRoom parent;

	private XQHXMonster(XQHXBattleRoom parent, int cfgId) {
		this.parent = parent;
		this.cfgId = cfgId;
		XQHXMonsterCfg monsterCfg = getCfg();
		this.monsterId = monsterCfg.getMonsterId();
	}

	public static XQHXMonster create(XQHXBattleRoom parent, int cfgId) {
		XQHXMonster result = new XQHXMonster(parent, cfgId);
		return result;
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.MONSTER;
	}

	@Override
	public int getGridCnt() {
		return 1;
	}

	@Override
	public boolean onTick() {
		return true;
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
		builder.setMonsterId(monsterId);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(IXQHXPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setMonsterId(monsterId);
		return builder;
	}

	public XQHXMonsterCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(XQHXMonsterCfg.class, cfgId);
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
	public String getGuildId() {
		return null;
	}

}

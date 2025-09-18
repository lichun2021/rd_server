package com.hawk.game.lianmengcyb.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.CYBORGMonsterCfg;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;

public class CYBORGMonster implements ICYBORGWorldPoint {
	private int cfgId;
	private int x;
	private int y;
	private int monsterId;
	private final CYBORGBattleRoom parent;

	private CYBORGMonster(CYBORGBattleRoom parent, int cfgId) {
		this.parent = parent;
		this.cfgId = cfgId;
		CYBORGMonsterCfg monsterCfg = getCfg();
		this.monsterId = monsterCfg.getMonsterId();
	}

	public static CYBORGMonster create(CYBORGBattleRoom parent, int cfgId) {
		CYBORGMonster result = new CYBORGMonster(parent, cfgId);
		return result;
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.MONSTER;
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
	public WorldPointPB.Builder toBuilder(ICYBORGPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setMonsterId(monsterId);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(ICYBORGPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setMonsterId(monsterId);
		return builder;
	}

	public CYBORGMonsterCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(CYBORGMonsterCfg.class, cfgId);
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
	public CYBORGBattleRoom getParent() {
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

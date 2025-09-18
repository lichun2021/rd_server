package com.hawk.game.module.lianmengtaiboliya.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.lianmengtaiboliya.ITBLYWorldPoint;
import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYMonsterCfg;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;

public class TBLYMonster implements ITBLYWorldPoint {
	private int cfgId;
	private int x;
	private int y;
	private int monsterId;
	private final TBLYBattleRoom parent;

	private TBLYMonster(TBLYBattleRoom parent, int cfgId) {
		this.parent = parent;
		this.cfgId = cfgId;
		TBLYMonsterCfg monsterCfg = getCfg();
		this.monsterId = monsterCfg.getMonsterId();
	}

	public static TBLYMonster create(TBLYBattleRoom parent, int cfgId) {
		TBLYMonster result = new TBLYMonster(parent, cfgId);
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
	public WorldPointPB.Builder toBuilder(ITBLYPlayer viewer) {
		WorldPointPB.Builder builder = WorldPointPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setMonsterId(monsterId);
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(ITBLYPlayer viewer) {
		WorldPointDetailPB.Builder builder = WorldPointDetailPB.newBuilder();
		builder.setPointX(x);
		builder.setPointY(y);
		builder.setPointType(getPointType());
		builder.setMonsterId(monsterId);
		return builder;
	}

	public TBLYMonsterCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(TBLYMonsterCfg.class, cfgId);
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
	public TBLYBattleRoom getParent() {
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

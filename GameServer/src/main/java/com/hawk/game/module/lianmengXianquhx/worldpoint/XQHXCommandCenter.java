package com.hawk.game.module.lianmengXianquhx.worldpoint;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.lianmengXianquhx.XQHXBattleRoom;
import com.hawk.game.module.lianmengXianquhx.cfg.XQHXBuildCfg;
import com.hawk.game.protocol.World.WorldPointType;

/**
 * 指挥部
 *
 */
public class XQHXCommandCenter extends IXQHXBuilding {

	public XQHXCommandCenter(XQHXBattleRoom parent) {
		super(parent);
	}

	public XQHXBuildCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(XQHXBuildCfg.class);
	}

	@Override
	public WorldPointType getPointType() {
		return WorldPointType.XQHX_BUILDING;
	}

	@Override
	public int getGridCnt() {
		return getBuildTypeCfg().getGridCnt();
	}
}

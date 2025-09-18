package com.hawk.game.world.proxy;

import com.hawk.game.config.XZQConstCfg;
import com.hawk.game.crossactivity.resourcespree.ResourceSpreeBoxWorldPoint;
import com.hawk.game.lianmengxzq.worldpoint.XZQWorldPoint;
import com.hawk.game.module.spacemecha.worldpoint.MechaBoxWorldPoint;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.module.spacemecha.worldpoint.StrongHoldWorldPoint;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.world.WorldPoint;

/**
 * 
 * @author lwt
 * @date 2017年7月26日
 */
public class WorldPointFactory {
	private static transient WorldPointFactory INSTANCE;

	private WorldPointFactory() {
	}


	public static WorldPointFactory getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new WorldPointFactory();
			INSTANCE.init();
		}
		return INSTANCE;

	}
	/**
	 * 创建空技能 当技能未实现时返回null
	 * 
	 * @param type
	 * @return
	 */
	public WorldPoint createWorldPoint(int type) {
		WorldPointType worldPointType = WorldPointType.valueOf(type);
		if(worldPointType == null){
			return null;
		}
		if(worldPointType == WorldPointType.XIAO_ZHAN_QU ){
			if(XZQConstCfg.getInstance().isOpen()){
				return new XZQWorldPoint(worldPointType);
			}else{
				return null;
			}
		}else if(worldPointType == WorldPointType.RESOURCE_SPREE_BOX){
			return new ResourceSpreeBoxWorldPoint();
		} else if (worldPointType == WorldPointType.SPACE_MECHA_STRONG_HOLD) {
			return new StrongHoldWorldPoint();
		} else if (worldPointType == WorldPointType.SPACE_MECHA_MAIN || worldPointType == WorldPointType.SPACE_MECHA_SLAVE) {
			return new SpaceWorldPoint();
		} else if (worldPointType == WorldPointType.SPACE_MECHA_BOX) {
			return new MechaBoxWorldPoint();
		}
		return new WorldPoint();
		
	}


	private void init() {
		
	}

}

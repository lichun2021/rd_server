package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.GuildManor.GuildSuperMineType;

/**
 *
 * @author zhenyu.shang
 * @since 2017年7月12日
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_field.xml")
public class GuildManorMineCfg  extends HawkConfigBase {
	
	/** Id */
	@Id
	protected final int id; 
	
	protected final String defaultName; 
	
	protected final long resourceUpLimit; 
	
	protected final int collectSpeed;
	
	protected final int buildingUpLimit;
	
	protected final int resType;
	
	public GuildManorMineCfg() {
		id = 0;
		defaultName = "";
		resourceUpLimit = 0;
		collectSpeed = 0;
		buildingUpLimit = 0;
		resType = 0;
	}

	public int getId() {
		return id;
	}

	public String getDefaultName() {
		return defaultName;
	}

	public long getResourceUpLimit() {
		return resourceUpLimit;
	}

	public int getCollectSpeed() {
		return collectSpeed;
	}

	public int getBuildingUpLimit() {
		return buildingUpLimit;
	}

	public int getResType() {
		return resType;
	}
	
	@Override
	protected boolean checkValid() {
		switch (this.getId()) {
		case GuildSuperMineType.GOLDORE_MINE_VALUE:
			if(resType != PlayerAttr.GOLDORE_UNSAFE_VALUE){
				return false;
			}
			return true;
		case GuildSuperMineType.OIL_MINE_VALUE:
			if(resType != PlayerAttr.OIL_UNSAFE_VALUE){
				return false;
			}
			return true;
		case GuildSuperMineType.TOMBARTHITE_MINE_VALUE:
			if(resType != PlayerAttr.TOMBARTHITE_UNSAFE_VALUE){
				return false;
			}
			return true;
		case GuildSuperMineType.URANIUM_MINE_VALUE:
			if(resType != PlayerAttr.STEEL_UNSAFE_VALUE){
				return false;
			}
			return true;
		default:
			return false;
		}
	}
}

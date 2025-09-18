package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.game.protocol.Const.PlayerAttr;

/**
 * 资源田产出物品
 * @author zhenyu.shang
 * @since 2018年1月12日
 */
@HawkConfigManager.XmlResource(file = "xml/world_field.xml")
@HawkConfigBase.CombineId(fields = {"type", "citylevel"})
public class WorldFieldCfg extends HawkConfigBase {
	
	/** 矿类型 */
	protected final int type;
	/** 大本等级 */
	protected final int citylevel;
	/** 奖励 */
	protected final int award;
	/** 周期时间 */
	protected final int ticktime;
	
	public WorldFieldCfg() {
		type = 0;
		citylevel = 0;
		award = 0;
		ticktime = 0;
	}

	public int getType() {
		return type;
	}

	public int getCitylevel() {
		return citylevel;
	}

	public int getAward() {
		return award;
	}

	public int getTicktime() {
		return ticktime;
	}
	
	@Override
	protected boolean checkValid() {
		PlayerAttr attr = PlayerAttr.valueOf(type);
		if(attr == null){
			HawkLog.errPrintln("resource type is error , can not find type {} in playerAttr", type);
			return false;
		}
		return true;
	}
}

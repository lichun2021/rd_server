package com.hawk.game.config;

import com.hawk.game.util.GsConst;
import com.hawk.serialize.string.SerializeHelper;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;

@HawkConfigManager.XmlResource(file = "xml/gift_sys_pool.xml")
public class GiftSysPoolCfg extends HawkConfigBase {
	/**
	 *礼包池id
	 */
	@Id
	private final int id;

	/**
	 *礼包池类型
	 */
	private final int type;

	/**
	 *礼包池生效类型
	 */
	private final int activateType;

	/**
	 *取礼包组合数
	 */
	private final int cnt;

	/**
	 *礼包池内对应礼包组合id和权重
	 */
	private final String value;

	/**
	 *礼包池重置时间
	 */
	private final String resetTime;
	/**
	 * 是否出售
	 */
	private final int isSale;
	
	private Map<Integer, Integer> valueMap;
	/**
	 * 重置时间数组
	 */
	private  List<Integer> resetTimeList;

	public GiftSysPoolCfg() {
		this.id = 0;
		this.type = 0;
		this.activateType = 0;
		this.cnt = 0;
		this.value = "";
		this.resetTime = "";
		this.isSale = 0;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getActivateType() {
		return activateType;
	}

	public int getCnt() {
		return cnt;
	}

	public String getValue() {
		return value;
	}

	public String getResetTime() {
		return resetTime;
	}



	@Override
	public boolean assemble() {
		this.valueMap = SerializeHelper.cfgStr2Map(this.value);
		this.resetTimeList = SerializeHelper.cfgStr2List(resetTime);
		
		return true;
	}

	public Map<Integer, Integer> getValueMap() {
		return valueMap;
	}

	public void setValueMap(Map<Integer, Integer> valueMap) {
		this.valueMap = valueMap;
	}
	
	@Override
	public boolean checkValid() {
		for (Integer groupId : this.getValueMap().keySet()) {
			GiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, groupId);
			if (groupCfg == null) {
				throw new InvalidParameterException("GiftSysPoolCfg 配置的组ID不存在 sysid=>"+this.id+" groupId=>"+groupId);
			}
			
			if (groupCfg.getType() == GsConst.GiftConst.GROUP_TYPE_POST) {
				throw new InvalidParameterException("GiftSysPoolCfg 根礼包不能是后置礼包 poolId=>"+this.id+" groupId=>"+groupId);
			}
		}
		
		for(Integer time : resetTimeList) {
			if (time < 0 || time >= 24) {
				throw new InvalidParameterException("invliad time:"+time);
			}
		}
		
		if (this.activateType == GsConst.GiftConst.POOL_ACTIVATE_TYPE_NORMAL) {
			HawkAssert.isTrue(cnt > 0 , "超值礼包常规池子的数量不能小于0 pooId:"+id);
		}
				
		return true;
	}

	public int getIsSale() {
		return isSale;
	}

	public List<Integer> getResetTimeList() {
		return resetTimeList;
	}
}
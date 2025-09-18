package com.hawk.game.module.mechacore.cfg;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.GsApp;

/**
 * 机甲核心常量表
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "xml/mecha_core_const.xml")
public class MechaCoreConstCfg extends HawkConfigBase {
	/**
	 * 开放vip等级
	 */
	protected final int vipLimit;
	/**
	 * 开放基地等级
	 */
	protected final int baseLimit;
	/**
	 * 开服时间（秒）
	 */
	protected final int serverDelay;
	/**
	 * 机甲核心开放
	 */
	protected final int superSoldierCoreOpen;
	/**
	 * 模块抽奖开放时间戳（单位秒）
	 */
	protected final int superSoldierDrawOpen;
	/**
	 * 抽奖，装配槽界面开放时间戳（单位秒）
	 */
	protected final int moduleOpenTime;
	
	/**
	 * 保底抽奖次数（此字段已废弃）
	 */
	protected final int drawFloorTimes;
	/**
	 * 免费抽奖次数
	 */
	protected final int freeDrawTimes;
	/**
	 * 免费抽奖cd（分钟）
	 */
	protected final int freeDrawCD;
	
	/**
	 * 每日抽取次数上限
	 */
	protected final int drawTimesLimit;
	
	/**
	 * 抽奖赠送道具
	 */
	protected final String extReward;
	
	/**
	 * 提升核心等级道具
	 */
	protected final String coreTechnologyLevelUpItem;
	/**
	 * 破核心阶位的道具
	 */
	protected final String coreTechnologyRankUpItem;
	/**
	 * 升级核心槽道具
	 */
	protected final String moduleSlotUpItem;
	/**
	 * 传承消耗道具
	 */
	protected final String inheritConsumeItem;
	/**
	 * 抽取模块的道具
	 */
	protected final String moduleDrawItem;
	
	/**
	 * 批量抽取的次数上限
	 */
	protected final int batchDrawMax;
	/**
	 * 抽卡多少次可以领宝箱（此字段已废弃）
	 */
	protected final int gachaTimesBox;
	
	/**
	 * 模块背包中的存储上限
	 */
	protected final int moduleMaxCount;
	
	/**
	 * 核心装配子功能开启条件：需要核心科技品阶达到多少阶
	 */
	protected final int moduleLoadRankLimit;
	/**
	 * 核心模块抽取功能开启条件：需要核心科技品阶达到多少阶
	 */
	protected final int drawRankLimit;
	
	/**
	 * 全局静态对象
	 */
	private static MechaCoreConstCfg instance = null;

	/**
	 * 获取全局静态对象
	 *
	 * @return
	 */
	public static MechaCoreConstCfg getInstance() {
		return instance;
	}

	public MechaCoreConstCfg() {
		vipLimit = 0;
		baseLimit = 0;
		serverDelay = 0;
		superSoldierCoreOpen = 0;
		superSoldierDrawOpen = 0;
		moduleOpenTime = 0;
		drawFloorTimes = 0;
		freeDrawTimes = 0;
		freeDrawCD = 0;
		drawTimesLimit = 0;
		extReward = "";
		coreTechnologyLevelUpItem = "";
		coreTechnologyRankUpItem = "";
		moduleSlotUpItem = "";
		inheritConsumeItem = "";
		moduleDrawItem = "";
		batchDrawMax = 100;
		gachaTimesBox = 50;
		moduleMaxCount = 1000;
		moduleLoadRankLimit = 0;
		drawRankLimit = 0;
	}
	
	/**
	 * 数据组装
	 */
	@Override
	protected boolean assemble() {
		instance = this;
		return true;
	}
	
	public int getBatchDrawMax() {
		return batchDrawMax;
	}

	public int getVipLimit() {
		return vipLimit;
	}

	public int getBaseLimit() {
		return baseLimit;
	}

	public int getServerDelay() {
		return serverDelay;
	}

	public boolean isMechaCoreOpen() {
		return superSoldierCoreOpen > 0;
	}

	public boolean isMechaCoreDrawOpen() {
		return superSoldierDrawOpen * 1000L < HawkApp.getInstance().getCurrentTime();
	}
	
	public int getModuleOpenTime() {
		return moduleOpenTime;
	}
	
	public boolean isMechaModuleOpen() {
		return moduleOpenTime * 1000L < HawkApp.getInstance().getCurrentTime();
	}

	@Deprecated
	public int getDrawFloorTimes() {
		return drawFloorTimes;
	}

	public int getFreeDrawTimes() {
		return freeDrawTimes;
	}

	public int getFreeDrawCD() {
		return freeDrawCD;
	}

	public int getDrawTimesLimit() {
		return drawTimesLimit;
	}

	public String getExtReward() {
		return extReward;
	}
	
	public String getCoreTechnologyLevelUpItem() {
		return coreTechnologyLevelUpItem;
	}

	public String getCoreTechnologyRankUpItem() {
		return coreTechnologyRankUpItem;
	}

	public String getModuleSlotUpItem() {
		return moduleSlotUpItem;
	}

	public String getInheritConsumeItem() {
		return inheritConsumeItem;
	}

	public String getModuleDrawItem() {
		return moduleDrawItem;
	}
	
	@Deprecated
	public int getGachaTimesBox() {
		return gachaTimesBox;
	}
	
	public int getModuleMaxCount() {
		return moduleMaxCount;
	}
	
	public int getModuleLoadRankLimit() {
		return moduleLoadRankLimit;
	}
	
	public int getDrawRankLimit() {
		return drawRankLimit;
	}

	public boolean checkServerDelay() {
		return HawkApp.getInstance().getCurrentTime() - GsApp.getInstance().getServerOpenTime() > serverDelay * 1000L;
	}
	
}

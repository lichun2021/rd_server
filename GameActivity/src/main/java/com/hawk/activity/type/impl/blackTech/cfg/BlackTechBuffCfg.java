package com.hawk.activity.type.impl.blackTech.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRandObj;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 奖池配置配置
 * 
 * @author RickMei
 *
 */
@HawkConfigManager.XmlResource(file = "activity/black_tech/black_tech_buff.xml")
public class BlackTechBuffCfg extends HawkConfigBase implements HawkRandObj {

	/** */
	@Id
	private final int buffID;
	/** 权重 */
	private final int weight;
	/** 加持礼包 */
	private final String packageID;

	private final long buffTime;

	/**
	 * 大本等级限制
	 */
	private final int buildLimit;
	
	/**
	 * 章节任务完成限制
	 */
	private final int taskLimit;
	/**
	 * 1 解锁装备研究 
	 * 2 解锁机甲赋能
	 * 3 解锁星能探索
	 */
	private final int unlockEquipResearch;
	
	private List<Integer> packageIds;

	private long buffTimeMs = 0;
	
	public BlackTechBuffCfg() {
		buffID = 0;
		weight = 0;
		packageID = "";
		buffTime = 0;
		buildLimit = 0;
		taskLimit = 0;
		unlockEquipResearch = 0;
	}

	@Override
	protected boolean assemble() {
		try {
			packageIds = SerializeHelper.stringToList(Integer.class, packageID, ",");
			buffTimeMs = buffTime * 1000L;
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		for(Integer packageId : packageIds){
			if(null == HawkConfigManager.getInstance().getConfigByKey(BlackTechPackageCfg.class, packageId)){
				throw new RuntimeException(String.format("BlackTechBuffCfg black_tech_buff.xml buffId=%d cannot find padkage:%d in black_tech_package.xml", this.buffID,packageId));
			}
		}
		return false;
	}

	public int getId() {
		return buffID;
	}

	public int getWeight() {
		return weight;
	}
	public int getBuffId() {
		return buffID;
	}

	public String getPackageId() {
		return packageID;
	}

	public List<Integer> getPackageIds() {
		return packageIds;
	}

	public long getBuffTime() {
		return buffTimeMs;
	}

	public int getBuildLimit() {
		return buildLimit;
	}

	public int getTaskLimit() {
		return taskLimit;
	}

	public int getUnlockEquipResearch() {
		return unlockEquipResearch;
	}
	
}

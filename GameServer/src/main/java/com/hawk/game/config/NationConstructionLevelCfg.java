package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.nation.construction.comm.BuildCondtion;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 建设处配置表
 * @author zhenyu.shang
 * @since 2022年3月24日
 */
@HawkConfigManager.XmlResource(file = "xml/nation_construction_level.xml")
public class NationConstructionLevelCfg extends HawkConfigBase {
	
	@Id
	protected final int nationBuild;
	
	protected final int buildType;
	
	protected final int level;
	
	protected final int daylimit;
	
	protected final int totalBuildVal;
	
	protected final int levelUpTime;
	
	/**
	 * 国家任务每日恢复次数
	 */
	protected final int missionRecoveryTimes;
	
	/**
	 * 国家任务可完成次数
	 */
	protected final int missionFinishTimes;
	
	/** 前置条件 */
	protected final String buildCondition;
	
	/** 医院存储死兵作用号 */
	protected final String hospitalProp;
	/** 医院存储死兵上限 */
	protected final int hospitalLimit;
	/** 医院恢复死兵加速（万分比） */
	protected final int hospitalAccelerate;
	/** 仓库保护资源比例（万分比）  */
	protected final int safeResource;
	/** 飞船最多放置数量 */
	protected final int shipNum;
	
	/**
	 * 前置条件枚举
	 */
	private List<BuildCondtion> buildConds;
	
	private Map<Integer, Integer> hospitalPropVals = new HashMap<>();
	
	public NationConstructionLevelCfg() {
		this.nationBuild = 0;
		this.buildType = 0;
		this.level = 0;
		this.daylimit = 0;
		this.totalBuildVal = 0;
		this.levelUpTime = 0;
		this.buildCondition = "";
		this.missionRecoveryTimes = 0;
		this.missionFinishTimes = 0;
		hospitalProp = "";
		hospitalLimit = 0;
		hospitalAccelerate = 0;
		safeResource = 0;
		this.shipNum = 0;
	}

	public int getNationBuild() {
		return nationBuild;
	}

	public int getBuildType() {
		return buildType;
	}

	public int getLevel() {
		return level;
	}

	public int getDaylimit() {
		return daylimit;
	}

	public int getTotalBuildVal() {
		return totalBuildVal;
	}

	public int getLevelUpTime() {
		return levelUpTime;
	}

	public String getBuildCondition() {
		return buildCondition;
	}
	
	public List<BuildCondtion> getBuildConds() {
		return buildConds;
	}

	public int getHospitalPropVal(int effId) {
		return hospitalPropVals.getOrDefault(effId, 0);
	}

	public int getMissionRecoveryTimes() {
		return missionRecoveryTimes;
	}
	
	public int getMissionFinishTimes() {
		return missionFinishTimes;
	}

	public int getHospitalLimit() {
		return hospitalLimit;
	}

	public int getHospitalAccelerate() {
		return hospitalAccelerate;
	}

	public int getSafeResource() {
		return safeResource;
	}

	public int getShipNum() {
		return shipNum;
	}

	@Override
	protected boolean assemble() {
		buildConds = new ArrayList<BuildCondtion>();
		if (!HawkOSOperator.isEmptyString(buildCondition)) {
			String[] split1 = buildCondition.split(SerializeHelper.BETWEEN_ITEMS);
			for (int i = 0; i < split1.length; i++) {
				String[] split2 = split1[i].split(SerializeHelper.ATTRIBUTE_SPLIT);
				BuildCondtion cond = BuildCondtion.newBuildCondition(Integer.parseInt(split2[0]), split2[1]);
				if(cond == null){
					return false;
				}
				buildConds.add(cond);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(hospitalProp)) {
			String[] propValString = hospitalProp.split(",");
			for (String propVal : propValString) {
				String[] vals = propVal.split("_");
				hospitalPropVals.put(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]));
			}
		}
		return true;
	}
}

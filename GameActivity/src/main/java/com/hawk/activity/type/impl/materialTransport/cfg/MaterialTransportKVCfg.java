package com.hawk.activity.type.impl.materialTransport.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**勋章宝藏kv配置表
 * @author Winder
 *
 */
@HawkConfigManager.KVResource(file = "activity/material_transport/material_transport_cfg.xml")
public class MaterialTransportKVCfg extends HawkConfigBase {
	// # 服务器开服延时开启活动时间；单位：秒
	private final int serverDelay;// = 1728000

	// # 基地等级
	private final int baseLimit;// = 35

	// # 跨服服务器数量
	private final int serverNumber;// = 3

	// # 每日最多普通货车发车次数
	private final int truckNumber;// = 6

	// # 联盟每日最多普通列车发车次数
	private final int allianceCommonTrainNumber;// = 1

	// # 联盟每日最多豪华列车发车次数
	private final int allianceSpecialTrainNumber;// = 8

	// # 个人每日联盟列车参与次数
	private final int trainNumber;// = 4
	private final int specialTrainNumber;

	private final String buyRefreshCost;
	// # 普通货车刷新消耗（三段式）
	private final String truckRefreshCost;// = 10000_1001_10

	// # 联盟列车刷新消耗（三段式）
	private final String trainRefreshCost;// = 10000_1001_10

	// # 召唤豪华列车消耗
	private final String SpecialTrainCost;// = 10000_1000_10000

	// # 豪华列车支付ID（安卓）
	private final String androidPayId;// =

	// # 豪华列车支付ID（ios）
	private final String iosPayId;// =

	// # 每日普通货车抢夺次数
	private final int truckRobNumber;// = 6

	// # 每日联盟列车抢夺次数
	private final int trainRobNumber;// = 3

	// # 货车最多被抢次数
	private final int truckRobbedNumber;// = 2

	// # 列车最多被抢次数
	private final int trainRobbedNumber;// = 3

	// # 联盟普通车车头奖励
	private final String commonTrainReward;

	// # 联盟豪华车车头奖励
	private final String specialTrainReward;

	// # 联盟最大同时集结数量
	private final int allianceRobMaxLimit;
	// # 联盟列车车厢人数
	private final int trainCarryNumber;// = 2

	// # 联盟列车准备时间（秒）
	private final int trainPrepareTime;// = 300

	// # 集结时长（秒）
	private final int allianceRobTime;// = 300

	private final int truckRefreshLimit;
	private final int commonTrainRefresLimit;
	private final int specialTrainRefreshLimit;
	/**
	 * 单例
	 */
	private static MaterialTransportKVCfg instance;

	/**
	 * 获取单例
	 * @return
	 */
	public static MaterialTransportKVCfg getInstance() {
		return instance;
	}

	public MaterialTransportKVCfg() {
		buyRefreshCost = "";
		truckRefreshLimit = 1000;
		commonTrainRefresLimit = 1000;
		specialTrainRefreshLimit = 1000;
		commonTrainReward = "";
		specialTrainReward = "";
		serverDelay = 0;
		baseLimit = 35;
		serverNumber = 3;
		truckNumber = 6;
		allianceCommonTrainNumber = 1;
		allianceSpecialTrainNumber = 8;
		trainNumber = 4;
		specialTrainNumber = 10;
		truckRefreshCost = "";
		trainRefreshCost = "";
		SpecialTrainCost = "";
		androidPayId = "";
		iosPayId = "";
		truckRobNumber = 6;
		trainRobNumber = 3;
		truckRobbedNumber = 2;
		trainRobbedNumber = 3;
		allianceRobMaxLimit = 5;
		trainCarryNumber = 2;
		trainPrepareTime = 300;
		allianceRobTime = 300;
		instance = this;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getBaseLimit() {
		return baseLimit;
	}

	public int getServerNumber() {
		return serverNumber;
	}

	public int getTruckNumber() {
		return truckNumber;
	}

	public int getAllianceCommonTrainNumber() {
		return allianceCommonTrainNumber;
	}

	public int getAllianceSpecialTrainNumber() {
		return allianceSpecialTrainNumber;
	}

	public int getTrainNumber() {
		return trainNumber;
	}

	public String getTruckRefreshCost() {
		return truckRefreshCost;
	}

	public String getTrainRefreshCost() {
		return trainRefreshCost;
	}

	public String getSpecialTrainCost() {
		return SpecialTrainCost;
	}

	public String getAndroidPayId() {
		return androidPayId;
	}

	public String getIosPayId() {
		return iosPayId;
	}

	public int getTruckRobNumber() {
		return truckRobNumber;
	}

	public int getTrainRobNumber() {
		return trainRobNumber;
	}

	public int getTruckRobbedNumber() {
		return truckRobbedNumber;
	}

	public int getTrainRobbedNumber() {
		return trainRobbedNumber;
	}

	public int getAllianceRobMaxLimit() {
		return allianceRobMaxLimit;
	}

	public String getCommonTrainReward() {
		return commonTrainReward;
	}

	public String getSpecialTrainReward() {
		return specialTrainReward;
	}

	public int getTrainCarryNumber() {
		return trainCarryNumber;
	}

	public int getTrainPrepareTime() {
		return trainPrepareTime * 1000;
	}

	public static void setInstance(MaterialTransportKVCfg instance) {
	}

	public int getAllianceRobTime() {
		return allianceRobTime * 1000;
	}

	public String getBuyRefreshCost() {
		return buyRefreshCost;
	}

	public int getTruckRefreshLimit() {
		return truckRefreshLimit;
	}

	public int getCommonTrainRefresLimit() {
		return commonTrainRefresLimit;
	}

	public int getSpecialTrainRefreshLimit() {
		return specialTrainRefreshLimit;
	}

	public int getSpecialTrainNumber() {
		return specialTrainNumber;
	}

}
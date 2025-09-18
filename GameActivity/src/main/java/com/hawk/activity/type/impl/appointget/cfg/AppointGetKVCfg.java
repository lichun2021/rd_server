package com.hawk.activity.type.impl.appointget.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

import com.google.common.base.Splitter;

@HawkConfigManager.KVResource(file = "activity/appoint_get/appoint_get_kv_cfg.xml")
public class AppointGetKVCfg extends HawkConfigBase {

	private final long serverDelay;
	// # 随机获得属性
	// appointGet = 10_20_3000;10_20_3000;10_20_3000;0_0_1000
	private final String appointGet;

	// # 每日抽取次数限制
	private final int limitTimes;

	// # 单抽花费
	private final String oneCost;// = 30000_840172_1

	// # 十连花费
	private final String oneGoldCost;// = 30000_840172_10

	// # 每次抽取必定获得的固定奖励
	private final String fixReward;// = 30000_840172_1

	List<AppointGetRandObj> weigthList = new ArrayList<>();

	public AppointGetKVCfg() {
		appointGet = "";
		serverDelay = 0;
		limitTimes = 1000;
		oneCost = "";
		oneGoldCost = "";
		fixReward = "";
	}

	@Override
	protected boolean assemble() {
		List<String> attrs = Splitter.on(";").omitEmptyStrings().splitToList(appointGet);
		for (int i = 0; i < attrs.size(); i++) {
			String attr = attrs.get(i);
			weigthList.add(AppointGetRandObj.create(attr));
		}

		return super.assemble();
	}

	public AppointGetRandObj randObj() {
		return HawkRand.randomWeightObject(weigthList);
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public String getAppointGet() {
		return appointGet;
	}

	public List<AppointGetRandObj> getWeigthList() {
		return weigthList;
	}

	public void setWeigthList(List<AppointGetRandObj> weigthList) {
		this.weigthList = weigthList;
	}

	public int getLimitTimes() {
		return limitTimes;
	}

	public String getOneCost() {
		return oneCost;
	}

	public String getOneGoldCost() {
		return oneGoldCost;
	}

	public String getFixReward() {
		return fixReward;
	}

}

package com.hawk.game.module.toucai.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.module.toucai.cfg.MedalFactoryRewardCfg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.MedalFactory.HPMedalFactory;

/**
 * 生产线
 */
public class MedalCollect {
	private final MedalFactoryObj medalFactory;

	private int index;

	private long start;
	private long end;
	private int productCfgId;
	private int rewardCfgId;
	// 被偷的
	private List<MedalStealed> stealed = new ArrayList<>();

	public MedalCollect(MedalFactoryObj medalFactory) {
		this.medalFactory = medalFactory;
	}

	public void mergeFrom(String string) {
		JSONObject obj = JSONObject.parseObject(string);
		this.index = obj.getIntValue("index");
		this.start = obj.getLongValue("start");
		this.end = obj.getLongValue("end");
		this.productCfgId = obj.getIntValue("pf");
		this.rewardCfgId = obj.getIntValue("rf");

	}

	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("index", index);
		obj.put("start", start);
		obj.put("end", end);
		obj.put("pf", productCfgId);
		obj.put("rf", rewardCfgId);
		return obj.toJSONString();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<MedalStealed> getStealed() {
		return stealed;
	}

	public void setStealed(List<MedalStealed> stealed) {
		this.stealed = stealed;
	}

	public MedalFactoryObj getMedalFactory() {
		return medalFactory;
	}

	public HPMedalFactory toHP() {
		if (stealed.stream().filter(Objects::isNull).findAny().isPresent()) {
			stealed = stealed.stream().filter(s -> s != null).collect(Collectors.toCollection(ArrayList::new));
		}
		HPMedalFactory.Builder builder = HPMedalFactory.newBuilder();
		builder.setIndex(index);
		builder.setStartTime(start);
		builder.setEndTime(end);
		builder.setUnlock(isUnlock());
		builder.setProductCfgId(productCfgId);
		builder.setRewardCfgId(rewardCfgId);
		for (MedalStealed steal : stealed) {
			builder.addSteals(steal.toHP());
		}

		return builder.build();
	}
	
	public MedalFactoryRewardCfg getRewardCfg(){
		return HawkConfigManager.getInstance().getConfigByKey(MedalFactoryRewardCfg.class, rewardCfgId);
	}

	public boolean isUnlock() {
		if (rewardCfgId != 0) {
			return true;
		}
		if (index == 3 && medalFactory.getParent().getEffect().getEffVal(EffType.MEDAL_649) > 0) {
			return true;
		}
		
		int num = medalFactory.getLevelCfg().getProductionNum() - 1;
		return medalFactory.isUnlock() && num >= index;
	}


	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public int getProductCfgId() {
		return productCfgId;
	}

	public void setProductCfgId(int productCfgId) {
		this.productCfgId = productCfgId;
	}

	public int getRewardCfgId() {
		return rewardCfgId;
	}

	public void setRewardCfgId(int rewardCfgId) {
		this.rewardCfgId = rewardCfgId;
	}

	
}

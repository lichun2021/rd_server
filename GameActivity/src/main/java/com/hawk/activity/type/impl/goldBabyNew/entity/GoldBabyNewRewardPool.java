package com.hawk.activity.type.impl.goldBabyNew.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hibernate.loader.custom.Return;

import com.hawk.activity.type.impl.goldBaby.cfg.GoldBabyRewardCfg;
import com.hawk.activity.type.impl.goldBabyNew.cfg.GoldBabyNewRewardCfg;
import com.hawk.serialize.string.SerializeHelper;
import com.sun.jndi.url.iiopname.iiopnameURLContextFactory;
import com.sun.org.apache.bcel.internal.generic.RETURN;

public class GoldBabyNewRewardPool {

	//奖池id
	private int poolId;
	
	//奖池等级
	private int poolLevel;

	//奖池抽取次数
	private int times;
	
	//奖池重置次数
	private int resetTimes;
	
	//锁定最高档
	private int lockTopGrade;
	
	//不可抽标识
	private int findOver;
	
	//当前的奖池中奖品项的ID
	private List<Integer> rewardIds;
	
	public GoldBabyNewRewardPool() {
		rewardIds = new ArrayList<>();
	}
	
	public List<Integer> getRewardIds() {
		return rewardIds;
	}

	public void setRewardIds(List<Integer> rewardIds) {
		this.rewardIds = rewardIds;
	}

	public int getFindOver() {
		return findOver;
	}

	public void setFindOver(int findOver) {
		this.findOver = findOver;
	}

	public int getResetTimes() {
		return resetTimes;
	}

	public void setResetTimes(int resetTimes) {
		this.resetTimes = resetTimes;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public int getPoolId() {
		return poolId;
	}

	public void setPoolId(int poolId) {
		this.poolId = poolId;
	}

	public int getPoolLevel() {
		return poolLevel;
	}

	public void setPoolLevel(int poolLevel) {
		this.poolLevel = poolLevel;
	}
	
	
	public int getLockTopGrade() {
		return lockTopGrade;
	}

	public void setLockTopGrade(int lockTopGrade) {
		this.lockTopGrade = lockTopGrade;
	}

	public static GoldBabyNewRewardPool valueOf(String data) {
		
		//TODO 每次随机6个，这里要改
		String[] array = SerializeHelper.split(data, SerializeHelper.ATTRIBUTE_SPLIT);
		String[] fillArray = SerializeHelper.fillStringArray(array, 12, "0");
		int index = 0;
		GoldBabyNewRewardPool pool = new GoldBabyNewRewardPool();
		pool.setPoolId(SerializeHelper.getInt(fillArray, index++));
		pool.setPoolLevel(SerializeHelper.getInt(fillArray, index++));
		pool.setTimes(SerializeHelper.getInt(fillArray, index++));
		pool.setResetTimes(SerializeHelper.getInt(fillArray, index++));
		pool.setLockTopGrade(SerializeHelper.getInt(fillArray, index++));
		pool.setFindOver(SerializeHelper.getInt(fillArray, index++));
		
		//添加奖品ID
		while(index < 12){
			pool.addRewardId(SerializeHelper.getInt(fillArray, index++));
		}
		return pool;
	}
	
	@Override
	public String toString() {
		List<Integer> list = new ArrayList<>();
		list.add(poolId);
		list.add(poolLevel);
		list.add(times);
		list.add(resetTimes);
		list.add(lockTopGrade);
		list.add(findOver);
		list.addAll(rewardIds);
		return SerializeHelper.collectionToString(list, SerializeHelper.ATTRIBUTE_SPLIT);
	}
	

	public void addRewardId(int itemId) {
		rewardIds.add(itemId);
	}
	
	/**
	 * 根据当前奖池等级抽取 一定个数 的奖品
	 * 根据奖品的数量从大到小排序
	 * rewardIds 是排序后的奖品项id 
	 * 通过rewarIds.get(0) 就可得到最高档
	 */
	public void setRandomRewards() {
		rewardIds.clear();
		
		//TODO 每次随机6个，这里要改
		
		//获取该奖池现在等级的所有奖励配置
		List<GoldBabyNewRewardCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(GoldBabyNewRewardCfg.class).toList();
		cfgs = cfgs.stream().filter(i->i.getLevel()==this.poolLevel&&i.getPoolId()==this.poolId).collect(Collectors.toList());
		if (cfgs.isEmpty())return;
		//获取weight列表
		List<Integer> weights = new ArrayList<>();
		for (GoldBabyNewRewardCfg cfg: cfgs){
			weights.add(cfg.getWeight());
		}
		//抽6个
		cfgs = HawkRand.randomWeightObject(cfgs, weights, 6);
		//按数量逆排序，第0号就是最高档
		cfgs = cfgs.stream().sorted((e1,e2)->{
			return new Long(e1.getRewardList().get(0).getItemCount()-e2.getRewardList().get(0).getItemCount()).intValue();
		}).collect(Collectors.toList());
		//ID添加到rewardsId中
		for(GoldBabyNewRewardCfg cfg:cfgs){
			rewardIds.add(cfg.getId());
		}
	}
}

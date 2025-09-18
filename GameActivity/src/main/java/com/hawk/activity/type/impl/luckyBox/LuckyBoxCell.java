package com.hawk.activity.type.impl.luckyBox;

import java.util.ArrayList;
import java.util.List;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.luckyBox.cfg.LuckyBoxKVCfg;
import com.hawk.activity.type.impl.luckyBox.cfg.LuckyBoxRewardCfg;
import com.hawk.activity.type.impl.luckyBox.cfg.LuckyBoxTurntableRewardCfg;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;
import com.hawk.game.protocol.Activity.LuckBoxCell;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class LuckyBoxCell implements SplitEntity,HawkRandObj{
	private int cellId;
	
	private int rewardId;
	
	private int count;
	
	private int weight;
	
	private int canSelect;
	
	public LuckyBoxCell() {
		
	}

	public LuckyBoxCell(int cellId, int rewardId, int count, int weight,int canSelect) {
		super();
		this.cellId = cellId;
		this.rewardId = rewardId;
		this.count = count;
		this.weight = weight;
		this.canSelect = canSelect;
	}

	@Override
	public SplitEntity newInstance() {
		return new LuckyBoxCell();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(this.cellId);
		dataList.add(this.rewardId);
		dataList.add(this.count);
		dataList.add(this.weight);
		dataList.add(this.canSelect);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(5);
		this.cellId = dataArray.getInt();
		this.rewardId = dataArray.getInt();
		this.count = dataArray.getInt();
		this.weight = dataArray.getInt();
		this.canSelect = dataArray.getInt();
	}
	
	@Override
	public String toString() {
		return "[cellId=" + cellId + "rewardId=" + rewardId+ ", count=" + count + ", weight=" + weight + ", canSelect=" + canSelect + "]";
	}

	public LuckyBoxCell copy(){
		LuckyBoxCell copy = new LuckyBoxCell(this.cellId, this.rewardId, this.count, this.weight,this.canSelect);
		return copy;
	}

	public int getCellId() {
		return cellId;
	}

	public void setCellId(int cellId) {
		this.cellId = cellId;
	}

	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getCanSelect() {
		return canSelect;
	}

	public void setCanSelect(int canSelect) {
		this.canSelect = canSelect;
	}

	public List<RewardItem.Builder> getReward(){
		LuckyBoxRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(
				LuckyBoxRewardCfg.class, this.rewardId);

		List<RewardItem.Builder> rewardItemList = new ArrayList<>();
		rewardItemList.addAll(cfg.getRewardList());
		return rewardItemList;
	}

	public void consumeCount(){
		if(this.count > 0){
			this.count --;
		}
	}

	public LuckBoxCell.Builder getCellBuilder(){
		LuckBoxCell.Builder builder = LuckBoxCell.newBuilder();
		builder.setId(this.cellId);
		builder.setAwardId(this.rewardId);
		builder.setCount(this.count);
		builder.setWeight(this.weight);
		builder.setCanSelect(this.canSelect);
		return builder;
	}
}

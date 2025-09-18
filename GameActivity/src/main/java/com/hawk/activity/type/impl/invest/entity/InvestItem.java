package com.hawk.activity.type.impl.invest.entity;

import java.util.List;

import org.hawk.os.HawkTime;

import com.hawk.game.protocol.Activity.InvestInfoPB;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 投资理财产品信息
 * 
 * @author lating
 *
 */
public class InvestItem implements SplitEntity {
	/**
	 * 产品ID
	 */
	private int productId;
	/**
	 * 购买时间
	 */
	private long purchaseTime;
	/**
	 * 是否购买了加成道具：1已购买，0未购买
	 */
	private int addCustomer;
	/**
	 * 投资额度
	 */
	private int investAmount;
	/**
	 * 收益返还标识：1已返还，0未返还
	 */
	private int profitBack;
	
	public InvestItem() {
		
	}
	
	public InvestItem(int productId) {
		this.productId = productId;
		this.purchaseTime = HawkTime.getMillisecond();
	}
	
	public long getPurchaseTime() {
		return purchaseTime;
	}

	public void setPurchaseTime(long purchaseTime) {
		this.purchaseTime = purchaseTime;
	}
	
	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public int getProfitBack() {
		return profitBack;
	}

	public void setProfitBack(int profitBack) {
		this.profitBack = profitBack;
	}

	public int getInvestAmount() {
		return investAmount;
	}

	public void setInvestAmount(int investAmount) {
		this.investAmount = investAmount;
	}

	public int getAddCustomer() {
		return addCustomer;
	}

	public void setAddCustomer(int addCustomer) {
		this.addCustomer = addCustomer;
	}


	@Override
	public SplitEntity newInstance() {
		return new InvestItem();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(productId);
		dataList.add(purchaseTime);
		dataList.add(addCustomer);
		dataList.add(investAmount);
		dataList.add(profitBack);
	}

	@Override
	public void fullData(DataArray dataArray) {
		productId = dataArray.getInt();
		purchaseTime = dataArray.getLong();
		addCustomer = dataArray.getInt();
		investAmount = dataArray.getInt();
		profitBack = dataArray.getInt();
	}
	
	public InvestInfoPB.Builder toBuilder() {
		InvestInfoPB.Builder builder = InvestInfoPB.newBuilder();
		builder.setProductId(productId);
		builder.setInvestTime(purchaseTime);
		builder.setAmount(investAmount);
		builder.setBuyCustomer(addCustomer == 1);
		builder.setReceived(profitBack == 1);
		return builder;
	}

}

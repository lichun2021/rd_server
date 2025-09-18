package com.hawk.game.module.dayazhizhan.battleroom.order;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZOrderCfg;
import com.hawk.game.protocol.DYZZ.PBDYZZOrder;

public class DYZZOrder {
	private final DYZZOrderCollection parent;
	/** 号令ID */
	private int orderId;
	/** 生效开始时间 */
	private long effectStartTime;
	/** 生效结束时间 */
	private long effectEndTime;
	/** 冷却时间 */
	private long cdTime;
	/** 使用次数*/
	private int buyCnt;
	
	/**
	 * 初始化
	 * 
	 * @param config
	 */
	public void init(DYZZOrderCfg config) {
		this.orderId = config.getId();
	}
	
	public DYZZOrder(DYZZOrderCollection parent){
		this.parent = parent;
	}
	
	/**
	 * 开启号令
	 * @param id
	 * @return
	 */
	public DYZZOrder startOrder() {
		long curTime = getParent().getParent().getCurTimeMil();
		long endTime = curTime + getConfig().getEffectTime() * 1000;
		long cdTime = endTime + getConfig().getCoolingTime() * 1000;
		setEffectStartTime(curTime);
		setEffectEndTime(endTime);
		setCdTime(cdTime);
		setBuyCnt(getBuyCnt() + 1);
		return this;
	}

	/**
	 * 是否生效中
	 * 
	 * @return
	 */
	public boolean inEffect() {
		long curTime = getParent().getParent().getCurTimeMil();
		if (curTime < this.effectEndTime) {
			return true;
		}
		return false;
	}

	/**
	 * 是否在冷却中
	 * 
	 * @return
	 */
	public boolean inCD() {
		long curTime = getParent().getParent().getCurTimeMil();
		if (curTime < this.cdTime) {
			return true;
		}
		return false;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public long getEffectStartTime() {
		return effectStartTime;
	}

	public void setEffectStartTime(long effectStartTime) {
		this.effectStartTime = effectStartTime;
	}

	public long getEffectEndTime() {
		return effectEndTime;
	}

	public void setEffectEndTime(long effectEndTime) {
		this.effectEndTime = effectEndTime;
	}

	public long getCdTime() {
		return cdTime;
	}

	public void setCdTime(long cdTime) {
		this.cdTime = cdTime;
	}

	/**
	 * 获取配置
	 * 
	 * @return
	 */
	public DYZZOrderCfg getConfig() {
		return HawkConfigManager.getInstance().getConfigByKey(DYZZOrderCfg.class, this.orderId);
	}

	public PBDYZZOrder.Builder genPBDYZZOrderBuilder() {
		PBDYZZOrder.Builder builder = PBDYZZOrder.newBuilder();
		builder.setOrderId(this.orderId);
		builder.setEffectStartTime(this.effectStartTime);
		builder.setEffectEndTime(this.effectEndTime);
		builder.setCoolTime(this.cdTime);
		builder.setHasBuy(buyCnt);
		builder.setCost(getPowerCost());
		return builder;
	}

	public int getPowerCost() {
		return getConfig().getPowerCost(buyCnt);
	}

	public int getBuyCnt() {
		return buyCnt;
	}

	public void setBuyCnt(int buyCnt) {
		this.buyCnt = buyCnt;
	}

	public DYZZOrderCollection getParent() {
		return parent;
	}

}

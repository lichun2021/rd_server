package com.hawk.game.module.lianmengtaiboliya.order;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager.CAMP;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYOrderCfg;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.TBLY.PBTBLYOrder;
import com.hawk.game.protocol.TBLY.PBTBLYOrderUseReq;

public class TBLYOrder {
	private TBLYOrderCollection parent;

	/** 号令ID */
	private int orderId;
	/** 生效开始时间 */
	private long effectStartTime;
	/** 生效结束时间 */
	private long effectEndTime;
	/** 冷却时间 */
	private long cdTime;

	/**
	 * 初始化
	 * 
	 * @param config
	 */
	public void init(TBLYOrderCfg config) {
		this.orderId = config.getId();
	}

	public void onTick() {
		// TODO Auto-generated method stub

	}

	public int canStartOrder(PBTBLYOrderUseReq req) {
		if (this.inEffect()) {
			return Status.Error.TBLY_ORDER_IN_EFFECT_VALUE;
		}
		if (this.inCD()) {
			return Status.Error.TBLY_ORDER_IN_COOL_VALUE;
		}
		if (getParent().startExclusion(this)) {
			return Status.Error.TBLY_ORDER_EFF_EXCLUSION_VALUE;
		}
		int cost = this.getConfig().getPowerCost();
		if (getParent().getCamp() == CAMP.A && getParent().getParent().campAOrder < cost) {
			return Status.Error.TBLY_ORDER_POWER_NOT_ENOUGH_VALUE;
		}
		if (getParent().getCamp() == CAMP.B && getParent().getParent().campBOrder < cost) {
			return Status.Error.TBLY_ORDER_POWER_NOT_ENOUGH_VALUE;
		}
		return 0;
	}

	public void startOrder(PBTBLYOrderUseReq req) {
		long curTime = HawkTime.getMillisecond();
		long endTime = curTime + this.getConfig().getEffectTime() * 1000;
		long cdTime = endTime + this.getConfig().getCoolingTime() * 1000;
		this.setEffectStartTime(curTime);
		this.setEffectEndTime(endTime);
		this.setCdTime(cdTime);
		getParent().notifyChange();
	}

	/**
	 * 是否生效中
	 * 
	 * @return
	 */
	public boolean inEffect() {
		long curTime = HawkTime.getMillisecond();
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
		long curTime = HawkTime.getMillisecond();
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
	public TBLYOrderCfg getConfig() {
		return HawkConfigManager.getInstance().getConfigByKey(TBLYOrderCfg.class, this.orderId);
	}

	public PBTBLYOrder.Builder genPBTBLYOrderBuilder() {
		PBTBLYOrder.Builder builder = PBTBLYOrder.newBuilder();
		builder.setOrderId(this.orderId);
		builder.setEffectStartTime(this.effectStartTime);
		builder.setEffectEndTime(this.effectEndTime);
		builder.setCoolTime(this.cdTime);
		return builder;
	}

	public TBLYOrderCollection getParent() {
		return parent;
	}

	public void setParent(TBLYOrderCollection parent) {
		this.parent = parent;
	}

}

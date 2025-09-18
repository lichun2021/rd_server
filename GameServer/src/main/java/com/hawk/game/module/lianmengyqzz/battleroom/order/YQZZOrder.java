package com.hawk.game.module.lianmengyqzz.battleroom.order;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZOrderCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.YQZZ.PBYQZZOrder;
import com.hawk.game.protocol.YQZZ.PBYQZZOrderUseReq;
import com.hawk.game.util.EffectParams;

public class YQZZOrder {
	private final YQZZOrderCollection parent;
	/** 号令ID */
	private int orderId;
	/** 生效开始时间 */
	private long effectStartTime;
	/** 生效结束时间 */
	private long effectEndTime;
	/** 冷却时间 */
	private long cdTime;
	private boolean active; // 所表示的建筑是否被占领. 如未占领, 冷却也不能用

	/**
	 * 初始化
	 * 
	 * @param config
	 */
	public void init(YQZZOrderCfg config) {
		this.orderId = config.getId();
	}

	public int canStartOrder(PBYQZZOrderUseReq req, IYQZZPlayer player) {
		if (inEffect()) {
			return Status.YQZZError.YQZZ_ORDER_IN_EFFECT_VALUE;
		}
		if (inCD()) {
			return Status.YQZZError.YQZZ_ORDER_IN_COOL_VALUE;
		}
		if (!isActive()) {
			return Status.YQZZError.YQZZ_ORDER_NOT_ACTIVE_VALUE;
		}

		return 0;
	}

	public YQZZOrder(YQZZOrderCollection parent) {
		this.parent = parent;
	}

	public void onTick() {

	}

	public int getEffect(EffType effType, EffectParams effParams) {
		return 0;
	}

	/**
	 * 开启号令
	 * @param id
	 * @return
	 */
	public YQZZOrder startOrder(PBYQZZOrderUseReq req, IYQZZPlayer player) {
		long curTime = getParent().getParent().getParent().getCurTimeMil();
		long endTime = curTime + getConfig().getEffectTime() * 1000;
		long cdTime = endTime + getConfig().getCoolingTime() * 1000;
		setEffectStartTime(curTime);
		setEffectEndTime(endTime);
		setCdTime(cdTime);
		return this;
	}

	/**
	 * 是否生效中
	 * 
	 * @return
	 */
	public boolean inEffect() {
		if (!active) {
			return false;
		}
		long curTime = getParent().getParent().getParent().getCurTimeMil();
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
		long curTime = getParent().getParent().getParent().getCurTimeMil();
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
	public YQZZOrderCfg getConfig() {
		return HawkConfigManager.getInstance().getConfigByKey(YQZZOrderCfg.class, this.orderId);
	}

	public PBYQZZOrder.Builder genPBYQZZOrderBuilder() {
		PBYQZZOrder.Builder builder = PBYQZZOrder.newBuilder();
		builder.setOrderId(this.orderId);
		builder.setEffectStartTime(this.effectStartTime);
		builder.setEffectEndTime(this.effectEndTime);
		builder.setActive(active);
		builder.setCoolTime(this.cdTime);
		return builder;
	}

	public YQZZOrderCollection getParent() {
		return parent;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}

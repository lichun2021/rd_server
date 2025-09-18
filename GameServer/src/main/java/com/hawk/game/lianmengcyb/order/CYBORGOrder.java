package com.hawk.game.lianmengcyb.order;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.CYBORGOrderCfg;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.CYBORG.PBCYBORGOrder;
import com.hawk.game.protocol.CYBORG.PBCYBORGOrderUseReq;
import com.hawk.game.util.GameUtil;

public class CYBORGOrder {
	private final CYBORGOrderCollection parent;
	/** 号令ID */
	private int orderId;
	/** 生效开始时间 */
	private long effectStartTime;
	/** 生效结束时间 */
	private long effectEndTime;
	/** 冷却时间 */
	private long cdTime;
	private int buildX;
	private int buildY;
	private boolean active; // 所表示的建筑是否被占领. 如未占领, 冷却也不能用
	/**
	 * 初始化
	 * 
	 * @param config
	 */
	public void init(CYBORGOrderCfg config) {
		this.orderId = config.getId();
		int[] pos = GameUtil.splitXAndY(config.getBuildPointId());
		this.buildX = pos[0];
		this.buildY = pos[1];
		
	}
	
	public int canStartOrder(PBCYBORGOrderUseReq req) {
		if (inEffect()) {
			return Status.CYBORGError.CYBORG_ORDER_IN_EFFECT_VALUE;
		}
		if (inCD()) {
			return Status.CYBORGError.CYBORG_ORDER_IN_COOL_VALUE;
		}
		if(!isActive()){
			return Status.CYBORGError.CYBORG_ORDER_NOT_ACTIVE_VALUE;
		}

		return 0;
	}

	public CYBORGOrder(CYBORGOrderCollection parent) {
		this.parent = parent;
	}

	/**
	 * 开启号令
	 * @param id
	 * @return
	 */
	public CYBORGOrder startOrder(PBCYBORGOrderUseReq req,ICYBORGPlayer player) {
		long curTime = getParent().getParent().getCurTimeMil();
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
	public CYBORGOrderCfg getConfig() {
		return HawkConfigManager.getInstance().getConfigByKey(CYBORGOrderCfg.class, this.orderId);
	}

	public PBCYBORGOrder.Builder genPBCYBORGOrderBuilder() {
		PBCYBORGOrder.Builder builder = PBCYBORGOrder.newBuilder();
		builder.setOrderId(this.orderId);
		builder.setEffectStartTime(this.effectStartTime);
		builder.setEffectEndTime(this.effectEndTime);
		builder.setCoolTime(this.cdTime);
		builder.setBuildX(buildX);
		builder.setBuildY(buildY);
		builder.setActive(active);
		return builder;
	}

	public CYBORGOrderCollection getParent() {
		return parent;
	}

	public int getBuildX() {
		return buildX;
	}

	public void setBuildX(int buildX) {
		this.buildX = buildX;
	}

	public int getBuildY() {
		return buildY;
	}

	public void setBuildY(int buildY) {
		this.buildY = buildY;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}

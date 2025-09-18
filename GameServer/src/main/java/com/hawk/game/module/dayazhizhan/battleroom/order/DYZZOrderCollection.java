package com.hawk.game.module.dayazhizhan.battleroom.order;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigManager;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZOrderCfg;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.DYZZ.PBDYZZOrderSyncResp;
import com.hawk.game.protocol.Status;

public class DYZZOrderCollection {

	/** 房间 */
	private final DYZZBattleRoom parent;
	/** 阵营 */
	private final DYZZCAMP camp;
	/** 号令列表 */
	private Map<Integer, DYZZOrder> orders = new HashMap<>();
	/** 作用号列表 */
	private ImmutableMap<EffType, Integer> orderEffVal;
	/** 上次tick时间 */
	private long lastTickTime;

	public DYZZOrderCollection(DYZZBattleRoom parent, DYZZCAMP camp) {
		this.parent = parent;
		this.camp = camp;
		this.init();
	}

	/**
	 * 初始化
	 */
	public void init() {
		this.orderEffVal = ImmutableMap.of();
		List<DYZZOrderCfg> cfgList = HawkConfigManager.getInstance().getConfigIterator(DYZZOrderCfg.class).toList();
		for (DYZZOrderCfg cfg : cfgList) {
			DYZZOrder order = null;
			switch (cfg.getId()) {
			case 1001:
				order = new DYZZOrder1001(this);
				break;
			case 1002:
				order = new DYZZOrder1002(this);
				break;
			case 1003:
				order = new DYZZOrder1003(this);
				break;
			case 1004:
				order = new DYZZOrder1004(this);
				break;
			case 1005:
				order = new DYZZOrder1005(this);
				break;
			case 1006:
				order = new DYZZOrder1006(this);
				break;
			case 1007:
				order = new DYZZOrder1007(this);
				break;
			case 1008:
				order = new DYZZOrder1008(this);
				break;

			default:
				order = new DYZZOrder(this);
				break;
			}
			order.init(cfg);
			this.orders.put(order.getOrderId(), order);
		}

	}

	/**
	 * 是否可以激活号令
	 * @param id
	 * @return
	 */
	public int canStartOrder(int id) {
		DYZZOrder order = this.orders.get(id);
		if (order == null) {
			return Status.SysError.DATA_ERROR_VALUE;
		}
		if (order.inEffect()) {
			return Status.DYZZError.DYZZ_ORDER_IN_EFFECT_VALUE;
		}
		if (order.inCD()) {
			return Status.DYZZError.DYZZ_ORDER_IN_COOL_VALUE;
		}
		if (this.startExclusion(order)) {
			return Status.DYZZError.DYZZ_ORDER_EFF_EXCLUSION_VALUE;
		}
		int cost = order.getPowerCost();
		if (this.camp == DYZZCAMP.A && this.parent.campAOrder < cost) {
			return Status.DYZZError.DYZZ_ORDER_POWER_NOT_ENOUGH_VALUE;
		}
		if (this.camp == DYZZCAMP.B && this.parent.campBOrder < cost) {
			return Status.DYZZError.DYZZ_ORDER_POWER_NOT_ENOUGH_VALUE;
		}
		if (order.getBuyCnt() >= order.getConfig().getCampCount()) {
			return Status.DYZZError.DYZZ_ORDER_BUT_COUNT_VALUE;
		}
		return 0;
	}

	/**
	 * 激活是否存在互斥
	 * @param order
	 * @return
	 */
	public boolean startExclusion(DYZZOrder order) {
		for (DYZZOrder DYZZOrder : this.orders.values()) {
			if (DYZZOrder.getOrderId() == order.getOrderId()) {
				continue;
			}
			if (!DYZZOrder.inEffect()) {
				continue;
			}
			if (order.getConfig().getExclusionList().contains(DYZZOrder.getOrderId())) {
				return true;
			}
		}
		return false;
	}

	public DYZZOrder getOrder(int id) {
		return this.orders.get(id);
	}

	/**
	 * 时钟
	 */
	public void onTick() {
		long curTime = getParent().getCurTimeMil();
		if (curTime < lastTickTime + 2000) {
			return;
		}
		this.lastTickTime = curTime;
		Map<EffType, Integer> effVal = calEffVal();
		boolean change = this.isChange(effVal);
		if (change) {
			this.notifyChange();
		}
	}

	/**
	 * 是否有作用号变化
	 * 
	 * @param effVal
	 * @return
	 */
	public boolean isChange(Map<EffType, Integer> effVal) {
		if (effVal.size() != this.orderEffVal.size()) {
			return true;
		}
		for (EffType type : effVal.keySet()) {
			int curVal = effVal.get(type);
			int lastVal = this.orderEffVal.getOrDefault(type, 0);
			if (curVal != lastVal) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取作用号值
	 * @param type
	 * @return
	 */
	public int getEffectVal(EffType type) {
		return this.orderEffVal.getOrDefault(type, 0);
	}

	/**
	 * 计算做用号
	 */
	public void loadEffval() {
		Map<EffType, Integer> effVal = calEffVal();
		this.orderEffVal = ImmutableMap.copyOf(effVal);
	}

	private Map<EffType, Integer> calEffVal() {
		Map<EffType, Integer> effVal = new HashMap<>();
		for (DYZZOrder order : this.orders.values()) {
			if (!order.inEffect()) {
				continue;
			}
			this.mergeEffval(effVal, order.getConfig().getEffectList());
		}
		return effVal;
	}

	/**
	 * 变化通知
	 */
	public void notifyChange() {
		Set<EffType> allEff = new HashSet<>();
		allEff.addAll(this.orderEffVal.keySet());
		this.loadEffval();
		allEff.addAll(this.orderEffVal.keySet());
		EffType[] arr = allEff.toArray(new EffType[allEff.size()]);
		List<IDYZZPlayer> plist = this.parent.getCampPlayers(this.camp);
		for (IDYZZPlayer player : plist) {
			player.getPlayerPush().syncPlayerEffect(arr);
		}
	}

	/**
	 * 合并计算作用号
	 * 
	 * @param effVal
	 * @param effValList
	 */
	private void mergeEffval(Map<EffType, Integer> effVal, List<EffectObject> effValList) {
		for (EffectObject effobj : effValList) {
			EffType type = EffType.valueOf(effobj.getEffectType());
			if (type == null) {
				continue;
			}
			effVal.merge(type, effobj.getEffectValue(), (v1, v2) -> v1 + v2);
		}
	}

	/**
	 * 生成PB
	 * 
	 * @return
	 */
	public PBDYZZOrderSyncResp.Builder genPBDYZZOrderSyncRespBuilder() {
		PBDYZZOrderSyncResp.Builder builder = PBDYZZOrderSyncResp.newBuilder();
		if (this.camp == DYZZCAMP.A) {
			builder.setOrder(parent.campAOrder);
		}
		if (this.camp == DYZZCAMP.B) {
			builder.setOrder(parent.campBOrder);
		}

		for (DYZZOrder order : this.orders.values()) {
			builder.addOrders(order.genPBDYZZOrderBuilder());
		}
		return builder;
	}

	public DYZZCAMP getCamp() {
		return camp;
	}

	public DYZZBattleRoom getParent() {
		return parent;
	}

}

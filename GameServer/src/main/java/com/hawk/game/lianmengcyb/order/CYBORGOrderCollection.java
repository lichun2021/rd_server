package com.hawk.game.lianmengcyb.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.CYBORGOrderCfg;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.lianmengcyb.CYBORGRoomManager.CYBORG_CAMP;
import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGBuildState;
import com.hawk.game.lianmengcyb.worldpoint.ICYBORGBuilding;
import com.hawk.game.protocol.CYBORG.PBCYBORGOrderSyncResp;
import com.hawk.game.protocol.CYBORG.PBCYBORGOrderUseReq;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

public class CYBORGOrderCollection {

	/** 房间 */
	private final CYBORGBattleRoom parent;
	/** 阵营 */
	private final CYBORG_CAMP camp;
	/** 号令列表 */
	private Map<Integer, CYBORGOrder> orders = new HashMap<>();
	/** 作用号列表 */
	private ImmutableMap<EffType, Integer> orderEffVal;
	/** 上次tick时间 */
	private long lastTickTime;

	public CYBORGOrderCollection(CYBORGBattleRoom parent, CYBORG_CAMP camp) {
		this.parent = parent;
		this.camp = camp;
		this.init();
	}

	/**
	 * 初始化
	 */
	public void init() {
		this.orderEffVal = ImmutableMap.of();
		List<CYBORGOrderCfg> cfgList = HawkConfigManager.getInstance().getConfigIterator(CYBORGOrderCfg.class).toList();
		for (CYBORGOrderCfg cfg : cfgList) {
			CYBORGOrder order = null;
			switch (cfg.getOrderType()) {
			case CYBORG_MARCH_SPEED:
				order = new CYBORGOrder1001(this);
				break;
			case CYBORG_BUILD_CONTRAL:
				order = new CYBORGOrder1002(this);
				break;
			case CYBORG_BUFF:
				order = new CYBORGOrder1003(this);
				break;

			default:
				order = new CYBORGOrder(this);
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
	public int canStartOrder(PBCYBORGOrderUseReq req) {
		int orderId = req.getOrderId();
		CYBORGOrder order = this.orders.get(orderId);
		if (order == null) {
			return Status.SysError.DATA_ERROR_VALUE;
		}
		return order.canStartOrder(req);
	}

	/**
	 * 激活是否存在互斥
	 * @param order
	 * @return
	 */
	public boolean startExclusion(CYBORGOrder order) {
		for (CYBORGOrder CYBORGOrder : this.orders.values()) {
			if (CYBORGOrder.getOrderId() == order.getOrderId()) {
				continue;
			}
			if (!CYBORGOrder.inEffect()) {
				continue;
			}
		}
		return false;
	}

	public CYBORGOrder getOrder(int id) {
		return this.orders.get(id);
	}

	public List<CYBORGOrder> getOrders() {
		return new ArrayList<>(orders.values());
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
		boolean activeChange = false;
		for (CYBORGOrder order : orders.values()) {
			boolean active = order.isActive();
			ICYBORGWorldPoint point = getParent().getWorldPoint(order.getBuildX(), order.getBuildY()).orElse(null);
			if (Objects.nonNull(point) && point instanceof ICYBORGBuilding) {
				ICYBORGBuilding build = (ICYBORGBuilding) point;
				if (build.getState() == CYBORGBuildState.ZHAN_LING && build.getCamp() == camp) {
					order.setActive(true);
				}else{
					order.setActive(false);
				}
			} else {
				order.setActive(false);
			}
			if(active != order.isActive()){
				activeChange = true;
			}
		}

		Map<EffType, Integer> effVal = calEffVal();
		boolean change = this.isChange(effVal);
		if (change || activeChange) {
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
		for (CYBORGOrder order : this.orders.values()) {
			if (!order.inEffect()) {
				continue;
			}
			if(!order.isActive()){
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
		List<ICYBORGPlayer> plist = this.parent.getPlayerList(CYBORGState.GAMEING);
		for (ICYBORGPlayer player : plist) {
			if(player.getCamp() == camp){
				player.getPlayerPush().syncPlayerEffect(arr);
			}
			syncOrder(player);
		}
	}
	
	public void syncOrder(ICYBORGPlayer player) {
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CYBORG_ORDER_SYNC_S_VALUE, genPBCYBORGOrderSyncRespBuilder()));
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
//			effVal.merge(type, effobj.getEffectValue(), (v1, v2) -> v1 + v2);
			effVal.put(type, effobj.getEffectValue());
		}
	}

	/**
	 * 生成PB
	 * 
	 * @return
	 */
	public PBCYBORGOrderSyncResp.Builder genPBCYBORGOrderSyncRespBuilder() {
		PBCYBORGOrderSyncResp.Builder builder = PBCYBORGOrderSyncResp.newBuilder();
		builder.setCamp(camp.intValue());
		for (CYBORGOrder order : this.orders.values()) {
			builder.addOrders(order.genPBCYBORGOrderBuilder());
		}
		return builder;
	}

	public CYBORG_CAMP getCamp() {
		return camp;
	}

	public CYBORGBattleRoom getParent() {
		return parent;
	}

}

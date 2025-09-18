package com.hawk.game.module.lianmengtaiboliya.order;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager.CAMP;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYOrderCfg;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.TBLY.PBTBLYOrderSyncResp;

public class TBLYOrderCollection {
	public static final int Order10001 = 10001;
	public static final int Order10002 = 10002;
	public static final int Order10003 = 10003;
	public static final int Order10004 = 10004;
	public static final int Order10005 = 10005;

	public static final int shuangbeijifen = 104;
	/** 攻击方受攻击加成,  npc表配置*/
	public static final int atkBuff = 198;
	/** 房间 */
	private final TBLYBattleRoom parent;
	/** 阵营 */
	private final CAMP camp;
	/** 号令列表 */
	private Map<Integer, TBLYOrder> orders = new HashMap<>();
	/** 作用号列表 */
	private ImmutableMap<EffType, Integer> orderEffVal;
	/** 上次tick时间 */
	private long lastTickTime;

	public TBLYOrderCollection(TBLYBattleRoom parent, CAMP camp) {
		this.parent = parent;
		this.camp = camp;
		this.init();
	}

	/**
	 * 初始化
	 */
	public void init() {
		this.orderEffVal = ImmutableMap.of();
		List<TBLYOrderCfg> cfgList = HawkConfigManager.getInstance().getConfigIterator(TBLYOrderCfg.class).toList();
		for (TBLYOrderCfg cfg : cfgList) {
			TBLYOrder order = null;
			switch (cfg.getId()) {
			case Order10001:
				order = new TBLYOrder10001();
				break;
			case Order10002:
				order = new TBLYOrder10002();
				break;
			case Order10003:
				order = new TBLYOrder10003();
				break;
			case Order10004:
				order = new TBLYOrder10004();
				break;
			case Order10005:
				order = new TBLYOrder10005();
				break;

			default:
				order = new TBLYOrder();
				break;
			}
			order.setParent(this);
			order.init(cfg);
			this.orders.put(order.getOrderId(), order);
		}

	}

	public <T extends TBLYOrder> T getOrderById(int id) {
		TBLYOrder order = this.orders.get(id);
		return (T) order;
	}

	/**
	 * 激活是否存在互斥
	 * @param order
	 * @return
	 */
	public boolean startExclusion(TBLYOrder order) {
		for (TBLYOrder tblyOrder : this.orders.values()) {
			if (tblyOrder.getOrderId() == order.getOrderId()) {
				continue;
			}
			if (!tblyOrder.inEffect()) {
				continue;
			}
			if (order.getConfig().getExclusionList().contains(tblyOrder.getOrderId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 时钟
	 */
	public void onTick() {
		long curTime = HawkTime.getMillisecond();
		if (curTime < lastTickTime + 1000) {
			return;
		}
		this.lastTickTime = curTime;
		Map<EffType, Integer> effVal = calEffVal();
		boolean change = this.isChange(effVal);
		for (TBLYOrder order : this.orders.values()) {
			order.onTick();
		}
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
		for (TBLYOrder order : this.orders.values()) {
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
		List<ITBLYPlayer> plist = this.getParent().getCampPlayers(this.getCamp());
		for (ITBLYPlayer player : plist) {
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
	public PBTBLYOrderSyncResp.Builder genPBTBLYOrderSyncRespBuilder() {
		PBTBLYOrderSyncResp.Builder builder = PBTBLYOrderSyncResp.newBuilder();
		for (TBLYOrder order : this.orders.values()) {
			builder.addOrders(order.genPBTBLYOrderBuilder());
		}
		return builder;
	}

	public CAMP getCamp() {
		return camp;
	}
	
	public String getGuildId(){
		return getParent().getCampGuild(camp);
	}

	public TBLYBattleRoom getParent() {
		return parent;
	}

}

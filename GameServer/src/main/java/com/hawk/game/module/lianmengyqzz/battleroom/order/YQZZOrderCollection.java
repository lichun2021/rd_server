package com.hawk.game.module.lianmengyqzz.battleroom.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.lianmengcyb.order.CYBORGOrder;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZ_CAMP;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZOrderCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBase;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.YQZZ.PBYQZZOrderSyncResp;
import com.hawk.game.protocol.YQZZ.PBYQZZOrderType;
import com.hawk.game.protocol.YQZZ.PBYQZZOrderUseReq;
import com.hawk.game.util.EffectParams;

public class YQZZOrderCollection {

	/** 房间 */
	private final YQZZBase parent;
	/** 阵营 */
	private final YQZZ_CAMP camp;
	/** 号令列表 */
	private Map<Integer, YQZZOrder> orders = new HashMap<>();
	/** 上次tick时间 */
	private long lastTickTime;

	public YQZZOrderCollection(YQZZBase parent, YQZZ_CAMP camp) {
		this.parent = parent;
		this.camp = camp;
		this.init();
	}

	/**
	 * 初始化
	 */
	public void init() {
		List<YQZZOrderCfg> cfgList = HawkConfigManager.getInstance().getConfigIterator(YQZZOrderCfg.class).toList();
		for (YQZZOrderCfg cfg : cfgList) {
			YQZZOrder order = null;
			PBYQZZOrderType type = PBYQZZOrderType.valueOf(cfg.getId());
			switch (type) {
			case YQZZ_DAODAN:
				order = new YQZZOrder1001(this);
				break;
			case YQZZ_JIJIE:
				order = new YQZZOrder2001(this);
				break;
			case YQZZ_YZYD:
				order = new YQZZOrder3001(this);
				break;
			case YQZZ_LJHF:
				order = new YQZZOrder4001(this);
				break;

			default:
				order = new YQZZOrder(this);
				break;
			}
			order.setActive(parent.getNationTechLevel(cfg.getTechId()) > 0 || parent.getParent().IS_GO_MODEL);
			order.init(cfg);
			this.orders.put(order.getOrderId(), order);
		}

	}

	/**
	 * 是否可以激活号令
	 * @param id
	 * @return
	 */
	public int canStartOrder(PBYQZZOrderUseReq req, IYQZZPlayer player) {
		int orderId = req.getOrderId();
		YQZZOrder order = this.orders.get(orderId);
		if (order == null) {
			return Status.SysError.DATA_ERROR_VALUE;
		}
		return order.canStartOrder(req, player);
	}

	// /**
	// * 激活是否存在互斥
	// * @param order
	// * @return
	// */
	// public boolean startExclusion(YQZZOrder order) {
	// for (YQZZOrder YQZZOrder : this.orders.values()) {
	// if (YQZZOrder.getOrderId() == order.getOrderId()) {
	// continue;
	// }
	// if (!YQZZOrder.inEffect()) {
	// continue;
	// }
	// }
	// return false;
	// }

	public YQZZOrder getOrder(int id) {
		return this.orders.get(id);
	}

	public List<YQZZOrder> getOrders() {
		return new ArrayList<>(orders.values());
	}

	/**
	 * 时钟
	 */
	public void onTick() {
		for (YQZZOrder order : orders.values()) {
			try {
				order.onTick();

			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	/**
	 * 获取作用号值
	 * @param type
	 * @return
	 */
	public int getEffectVal(EffType effType, EffectParams effParams) {
		int result = 0;
		for (YQZZOrder order : orders.values()) {
			result += order.getEffect(effType, effParams);
		}
		return result;
	}

	/**
	 * 变化通知
	 */
	public void notifyChange() {
		List<IYQZZPlayer> plist = this.parent.getParent().getPlayerList(YQZZState.GAMEING);
		for (IYQZZPlayer player : plist) {
			if (player.getCamp() == camp) {
				syncOrder(player);
			}
		}
	}

	public void syncOrder(IYQZZPlayer player) {
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_ORDER_SYNC_S_VALUE, genPBYQZZOrderSyncRespBuilder()));
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
			// effVal.merge(type, effobj.getEffectValue(), (v1, v2) -> v1 + v2);
			effVal.put(type, effobj.getEffectValue());
		}
	}

	/**
	 * 生成PB
	 * 
	 * @return
	 */
	public PBYQZZOrderSyncResp.Builder genPBYQZZOrderSyncRespBuilder() {
		PBYQZZOrderSyncResp.Builder builder = PBYQZZOrderSyncResp.newBuilder();
		builder.setCamp(camp.intValue());
		for (YQZZOrder order : this.orders.values()) {
			builder.addOrders(order.genPBYQZZOrderBuilder());
		}
		return builder;
	}

	public YQZZ_CAMP getCamp() {
		return camp;
	}

	public YQZZBase getParent() {
		return parent;
	}

}

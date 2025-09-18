package com.hawk.game.player.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.util.HawkClassScaner;
import org.hawk.xid.HawkXID;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.ShopCfg;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ToolType;

/**
 * 商品道具操作类
 * @author lating
 *
 */
public class ItemService extends HawkAppObj {
	/**
	 * 具体的item策略对象
	 */
	private Map<Integer, AbstractItemUseEffect> itemUseObjMap = new HashMap<>();
	/**
	 * 全局实例对象
	 */
	private static ItemService instance = null;

	public static ItemService getInstance() {
		return instance;
	}

	/**
	 * 构造
	 * @param xid
	 */
	public ItemService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		return scanItemUseEffect();
	}
	
	/**
	 * 扫描具体的类
	 */
	private boolean scanItemUseEffect() {
		List<Class<?>> allClasses = HawkClassScaner.getAllClasses("com.hawk.game.player.item.impl");
		for (Class<?> clazz : allClasses) {
			if (clazz.isAssignableFrom(AbstractItemUseEffect.class)) {
				continue;
			}
			
			try {
				AbstractItemUseEffect obj = (AbstractItemUseEffect) clazz.newInstance();
				if (itemUseObjMap.containsKey(obj.itemType())) {
					throw new RuntimeException("ItemUseEffect impl repeated: " + obj.itemType());
				}
				itemUseObjMap.put(obj.itemType(), obj);
			} catch (InstantiationException | IllegalAccessException e) {
				HawkException.catchException(e);
				return false;
			}
		}
		
		return true;
	}

	/**
	 * 通过ItemId获取商品配置
	 * @param itemId
	 * @return
	 */
	public ShopCfg getShopCfgByItemId(int itemId){
		return ShopCfg.getShopCfgByItemId(itemId);
	}
	
	/**
	 * 使用道具判断
	 * @param player
	 * @param itemCfg
	 * @param itemCount
	 * @param protocol
	 * @param targetId
	 * @return
	 */
	public boolean itemUseCheck(Player player, ItemCfg itemCfg, int itemCount, int protocol, String targetId) {
		// 不可使用
		if (itemCfg.getItemType() != Const.ToolType.TALENT_SWITCH_VALUE && itemCfg.getUse() == 0) {
			HawkLog.errPrintln("use item, item can not use, playerId: {}, itemId: {}, protocol: {}", player.getId(), itemCfg.getId(), protocol);
			player.sendError(protocol, Status.Error.ITEM_CAN_NOT_USE_IN_BAG_VALUE, 0);
			return false;
		}
		
		if (itemCfg.getItemType() != ToolType.TRAIN_QUANTITY_ADD_ONCE_VALUE && itemCfg.getItemType() != ToolType.ADVANCE_QUANTITY_ADD_ONCE_VALUE &&  itemCfg.getEffect() == EffType.TRAIN_QUANTITY_ADD_NUM_VALUE) {
			StatusDataEntity entity = player.getData().getStatusById(EffType.TRAIN_QUANTITY_ADD_NUM_VALUE, "");
			if (entity != null && entity.getEndTime() > HawkTime.getMillisecond()) {
				player.sendError(protocol, Status.Error.TRAIN_QUANTITY_ADD_FAILD, 0); 
				HawkLog.errPrintln("use item, trainQuantityAdd buff already exist, playerId: {}, itemId: {}, value: {}, protocol: {}", 
						player.getId(), itemCfg.getId(), entity.getVal(), protocol);
				return false;
			}
		}
		
		AbstractItemUseEffect obj = itemUseObjMap.get(itemCfg.getItemType());
		if (obj == null) {
			HawkLog.errPrintln("onItemUse check break, playerId: {}, itemType: {}", player.getId(), itemCfg.getItemType());
			return true;
		}
		
		return obj.useItemCheck(player, itemCfg, itemCfg.getId(), itemCount, protocol, targetId);
	}
	
	/**
	 * 道具使用
	 * @param player
	 * @param itemCfg
	 * @param itemCount
	 * @param targetId
	 * @return
	 */
	public boolean onItemUse(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		AbstractItemUseEffect obj = itemUseObjMap.get(itemCfg.getItemType());
		if (obj == null) {
			HawkLog.errPrintln("onItemUse break, playerId: {}, itemType: {}", player.getId(), itemCfg.getItemType());
			return false;
		}
		return obj.useEffect(player, itemCfg, itemCount, targetId);
	}
	
}

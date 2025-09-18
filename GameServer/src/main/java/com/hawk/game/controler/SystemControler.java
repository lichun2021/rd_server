package com.hawk.game.controler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigReloadListener;
import org.hawk.config.HawkReloadable;
import org.hawk.log.HawkLog;

import com.hawk.game.config.SysControlProperty;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.util.GsConst.ControlerModule;

/**
 * 系统控制
 * 
 * @author lating
 *
 */
public class SystemControler {
	/**
	 * 协议控制
	 */
	public static final int PROTOCOL = 1;
	/**
	 * 消息id控制
	 */
	public static final int MSG = 2;
	/**
	 * 模块控制
	 */
	public static final int MODULE = 3;

	/**
	 * 整个系统被关闭标识，true表示整个系统被关闭
	 */
	private volatile boolean closeAll;

	/**
	 * 被关闭的消息
	 */
	private Map<Integer, Boolean> closedMsg;

	/**
	 * 被关闭的协议id
	 */
	private Map<Integer, Boolean> closedProtocol;

	/**
	 * 模块功能控制
	 */
	private Map<Integer, Boolean> closedModule;
	
	/**
	 * 控制某个系统下的某一项，比如：控制商城中的某项商品不能购买
	 */
	private Map<Integer, Set<Integer>> closedSystemItems;

	/**
	 * 单例
	 */
	private static SystemControler instance = new SystemControler();

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static SystemControler getInstance() {
		return instance;
	}

	/**
	 * 私有化构造
	 */
	private SystemControler() {
		closeAll = false;
		closedMsg = new ConcurrentHashMap<Integer, Boolean>();
		closedProtocol = new ConcurrentHashMap<Integer, Boolean>();
		closedModule = new ConcurrentHashMap<Integer, Boolean>();
		closedSystemItems = new ConcurrentHashMap<Integer, Set<Integer>>();
	}

	/**
	 * 初始化
	 */
	public void init() {
		// 模块关闭检测
		checkModuleClose();

		// 注册模块控制配置监听
		registerCloseModuleListener();
		
		initClosedSystemTypeItems();
	}

	/**
	 * 关闭消息
	 * 
	 * @param msg名称或id
	 */
	public void closeMsg(Integer... msgIds) {
		for (Integer msgId : msgIds) {
			if (closedMsg.putIfAbsent(msgId, true) == null) {
				HawkLog.logPrintln("do close msg: {}", msgId);
			}
		}
	}

	/**
	 * 关闭协议
	 * 
	 * @param msgId
	 */
	public void closeProtocol(Integer... protocolIds) {
		for (Integer protocolId : protocolIds) {
			if (closedProtocol.putIfAbsent(protocolId, true) == null) {
				HawkLog.logPrintln("do close protocol: {}", protocolId);
			}
		}
	}

	/**
	 * 关闭模块
	 */
	public void closeModule(Integer... moduleIds) {
		for (Integer moduleId : moduleIds) {
			if (closedModule.putIfAbsent(moduleId, true) == null) {
				HawkLog.logPrintln("do close module: {}", moduleId);
			}
		}
	}

	/**
	 * 关闭整个系统
	 */
	public void closeAllSystem() {
		closeAll = true;
	}

	/**
	 * 打开已被关闭的消息
	 * 
	 * @param msg
	 *            名称或id
	 */
	public void openMsg(Integer... msgIds) {
		for (Integer msgId : msgIds) {
			if (closedMsg.remove(msgId)) {
				HawkLog.logPrintln("undo close msg: {}", msgId);
			}
		}
	}

	/**
	 * 打开已被关闭的协议
	 * 
	 * @param msgId
	 */
	public void openProtocol(Integer... protocolIds) {
		for (Integer protocolId : protocolIds) {
			Boolean result = closedProtocol.remove(protocolId);
			if (Objects.nonNull(result) && result.booleanValue()) {
				HawkLog.logPrintln("undo close protocol: {}", protocolId);
			}
		}
	}

	/**
	 * 打开已被关闭的协议
	 * 
	 * @param msgId
	 */
	public void openModule(String... moduleIds) {
		for (String moduleId : moduleIds) {
			if (closedModule.remove(Integer.parseInt(moduleId)) != null) {
				HawkLog.logPrintln("undo close module: {}", moduleId);
			}
		}
	}

	/**
	 * 打开已被关闭的整个系统
	 */
	public void openSystem() {
		closeAll = false;
	}

	/**
	 * 判断消息是否被关闭
	 * 
	 * @param msg
	 *            名称或id
	 * @return
	 */
	public boolean isMsgClosed(int msgId) {
		return closedMsg.containsKey(msgId);
	}

	/**
	 * 判断协议是否被关闭
	 * 
	 * @param protocolId
	 * @return
	 */
	public boolean isProtocolClosed(int protocolId) {
		return closedProtocol.containsKey(protocolId);
	}

	/**
	 * 判断模块是否关闭
	 * 
	 * @param moduleId
	 * @return
	 */
	public boolean isModuleClosed(ControlerModule module) {
		return closedModule.containsKey(module.value());
	}

	/**
	 * 判断是否整个系统都被关闭
	 * 
	 * @return
	 */
	public boolean isAllSystemClosed() {
		return closeAll;
	}

	/**
	 * 获取被关闭的消息
	 * 
	 * @return
	 */
	public Collection<Integer> getClosedMsgs() {
		return Collections.unmodifiableCollection(closedMsg.keySet());
	}

	/**
	 * 获取被关闭的协议
	 * 
	 * @return
	 */
	public Collection<Integer> getClosedProtocols() {
		return Collections.unmodifiableCollection(closedProtocol.keySet());
	}

	/**
	 * 注册模块控制配置监听
	 */
	private void registerCloseModuleListener() {
		HawkConfigManager.getInstance().registerReloadListener(SysControlProperty.class, new HawkConfigReloadListener() {
			@Override
			public void beforReload(Class<? extends HawkReloadable> clazz) {
			}

			@Override
			public void afterReload(Class<? extends HawkReloadable> clazz) {
				// 模块关闭检测
				checkModuleClose();
			}
		});
	}

	/**
	 * 模块关闭检测
	 */
	private void checkModuleClose() {
		// 先清空之前的
		closedModule.clear();

		// 所有模块开关
		Map<Integer, Boolean> moduleMap = new HashMap<Integer, Boolean>();

		SysControlProperty sysControlProperty = SysControlProperty.getInstance();
		moduleMap.put(ControlerModule.MAIL_SEND.value(), sysControlProperty.isMailEnable());
		moduleMap.put(ControlerModule.MAIL_REWARD_RECV.value(), sysControlProperty.isMailRewardEnable());
		moduleMap.put(ControlerModule.WORLD_CHAT.value(), sysControlProperty.isWorldChatEnable());
		moduleMap.put(ControlerModule.GUILD_CHAT.value(), sysControlProperty.isGuildChatEnable());
		moduleMap.put(ControlerModule.RECHARGE_PAY.value(), sysControlProperty.isPayEnable());
		moduleMap.put(ControlerModule.RECHARGE_SHOP.value(), sysControlProperty.isShopEnable());
		
		moduleMap.put(ControlerModule.PREMIUM_GIFT.value(), sysControlProperty.isPremiumGiftEnable());
		moduleMap.put(ControlerModule.SALES_GOODS.value(), sysControlProperty.isSalesGoodsEnable());
		moduleMap.put(ControlerModule.VIP_SHOP.value(), sysControlProperty.isVipShopEnable());
		moduleMap.put(ControlerModule.VIP_GIFT.value(), sysControlProperty.isVipGiftEnable());
		moduleMap.put(ControlerModule.INDEPENDENT_ARMS.value(), sysControlProperty.isIndependentArmsEnable());
		moduleMap.put(ControlerModule.ALLIED_DEPOT.value(), sysControlProperty.isAlliedDepotEnable());
		moduleMap.put(ControlerModule.DAILY_TASK.value(), sysControlProperty.isDailyTaskEnable());
		moduleMap.put(ControlerModule.ITEM_USE.value(), sysControlProperty.isItemUseEnable());
		moduleMap.put(ControlerModule.ITEM_GET.value(), sysControlProperty.isItemGetEnable());
		moduleMap.put(ControlerModule.DIAMOND_USE.value(), sysControlProperty.isDiamondUseEnable());
		moduleMap.put(ControlerModule.GOLD_USE.value(), sysControlProperty.isGoldUseEnable());
		moduleMap.put(ControlerModule.SKILL_USE.value(), sysControlProperty.isSkillEnable());
		moduleMap.put(ControlerModule.ACTIVITY.value(), sysControlProperty.isActivityEnable());
		moduleMap.put(ControlerModule.PUSH_GIFT.value(), sysControlProperty.isPushGiftEnable());
		moduleMap.put(ControlerModule.GRABRESENABLE.value(), sysControlProperty.isGrabResEnable());
		moduleMap.put(ControlerModule.GATHERRESINCITY.value(), sysControlProperty.isGatherResInCity());

		// 关闭的模块添加到closeModuleIds
		for (Entry<Integer, Boolean> moduleControl : moduleMap.entrySet()) {
			if (moduleControl.getValue()) {
				continue;
			}

			// 关闭模块
			closeModule(moduleControl.getKey());
		}
	}
	
	/**
	 * 初始化已关闭的系统相关条目
	 */
	public void initClosedSystemTypeItems() {
		for (ControlerModule module : ControlerModule.values()) {
			Set<String> closedItems = LocalRedis.getInstance().getClosedSystemItems(module.value());
			if (!closedItems.isEmpty()) {
				Set<Integer> closedItemSet = new ConcurrentHashSet<>(closedItems.stream().map(e -> Integer.valueOf(e)).collect(Collectors.toList()));
				closedSystemItems.put(module.value(), closedItemSet);
			}
		}
	}
	
	/**
	 * 关闭系统相关条目
	 * @param module
	 * @param systemTypeItem
	 */
	public void closeSystemItem(ControlerModule module, int systemTypeItem) {
		Set<Integer> closedItems = closedSystemItems.get(module.value());
		if (closedItems == null) {
			closedSystemItems.putIfAbsent(module.value(), new ConcurrentHashSet<>());
			closedItems = closedSystemItems.get(module.value());
		}
		
		closedItems.add(systemTypeItem);
	}
	
	/**
	 * 开启系统相关条目
	 * @param module
	 * @param systemTypeItem
	 */
	public void openClosedSystemItem(ControlerModule module, Integer systemTypeItem) {
		Set<Integer> closedItems = closedSystemItems.get(module.value());
		if (closedItems != null) {
			closedItems.remove(systemTypeItem);
		}
	}
	
	/**
	 * 获取一个系统中已关闭的条目
	 * @param module
	 * @return
	 */
	public Set<Integer> getClosedSystemItems(ControlerModule module) {
		if (closedSystemItems.containsKey(module.value())) {
			return closedSystemItems.get(module.value());
		}
		
		return Collections.emptySet();
	}
	
	/**
	 * 判断一个系统下某项是否已关闭
	 * @param systemType
	 * @param systemTypeItem
	 * @return
	 */
	public boolean isSystemItemsClosed(ControlerModule module, int... systemTypeItem) {
		if (isModuleClosed(module)) {
			return true;
		}
		
		int systemItem = systemTypeItem.length > 0 ? systemTypeItem[0] : 0;
		Set<Integer> systemTypeItemSet = closedSystemItems.get(module.value());
		if (systemTypeItemSet != null && systemTypeItemSet.contains(systemItem)) {
			return true;
		}
		
		return false;
	}
}

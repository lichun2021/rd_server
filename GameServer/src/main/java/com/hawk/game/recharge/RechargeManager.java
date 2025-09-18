package com.hawk.game.recharge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.util.HawkClassScaner;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hawk.game.GsConfig;
import com.hawk.game.config.PayCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.data.RechargeInfo;
import com.hawk.game.entity.RechargeDailyEntity;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Recharge.GoodsItem;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.protocol.Recharge.RechargeInfoSync;
import com.hawk.game.recharge.impl.DefaultRecharge;
import com.hawk.game.util.GameUtil;
import com.hawk.game.recharge.RechargeType;

/**
 * 充值管理器
 *
 * @author hawk
 *
 */
public class RechargeManager extends HawkAppObj {
	
	static final Logger logger = LoggerFactory.getLogger("Recharge");
	/**
	 * 具体的礼包直购策略对象
	 */
	private Map<Integer, AbstractGiftRecharge> giftRechargeMap = new HashMap<>();
	
	/**
	 * 全局实例对象
	 */
	private static RechargeManager instance = null;

	public static RechargeManager getInstance() {
		return instance;
	}

	/**
	 * 构造
	 *
	 * @param xid
	 */
	public RechargeManager(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		return scanGiftRecharge();
	}

	/**
	 * 生成订单号
	 * 
	 * @param puid
	 * @param playerId
	 * @param platform
	 * @param channel
	 * @param deviceId
	 * @param goodsId
	 * @param goodsCount
	 * @param goodsPrice
	 * @param currency
	 * @param extra
	 * @return
	 */
	public String generateOrder(String puid, String playerId, String platform, String channel, String country, String deviceId,
			String goodsId, int goodsCount, int goodsPrice, String currency, String extra) {
		try {
			// 记录请求参数
			String orderId = UUID.randomUUID().toString().replace("-", "");
			logger.info("generate order, "
					+ "orderId: {}, puid: {}, playerId: {}, platform: {}, channel: {}, deviceId: {}, "
					+ "goodsId: {}, goodsCount: {}, goodsPrice: {}, currency: {}, extra: {}",
					orderId, puid, playerId, platform, channel, deviceId, goodsId, goodsCount, goodsPrice, currency, extra);

			// 参数有效性判断
			if (HawkOSOperator.isEmptyString(puid) ||
					HawkOSOperator.isEmptyString(playerId) ||
					HawkOSOperator.isEmptyString(platform) ||
					HawkOSOperator.isEmptyString(channel) ||
					HawkOSOperator.isEmptyString(goodsId) ||
					HawkOSOperator.isEmptyString(currency)) {
				return null;
			}

			// 把生成的订单存储到redis中
			if (RedisProxy.getInstance().updateOrderInfo(orderId, GsConfig.getInstance().getServerId(),
					puid, playerId, platform, channel, country, deviceId==null?"":deviceId, goodsId, goodsCount, goodsPrice, currency, extra)) {
				return orderId;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 创建充值订单信息
	 * 
	 * @param player        
	 * @param billno        订单号
	 * @param token         订单token
	 * @param goodsId       礼包ID
	 * @param goodsCount    一次购买礼包的个数
	 * @param payMoney      实际支付的金额  goodsPrice=payMoney*10
	 * @param goodsPrice    以Q点为单位，1Q币=10Q点，单价的制定需遵循腾讯定价规范  
	 * @param currency      支付所用的币种
	 * @param rechargeType  充值类型：礼包直购或是充值买砖
	 * @param rechargeTime  充值时间（单位秒）
	 * @param diamonds      充值获得钻石数
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public boolean createRechargeRecord(Player player, String billno, String token, String goodsId, int payMoney, int goodsPrice, 
			String currency, int rechargeType, int rechargeTime, int diamonds) {
		try {
			if (player.getData().getPlayerRechargeDailyEntity(billno) != null) {
				logger.error("recharge record has exist, billno: {}, token: {}, playerId: {}, goodsId: {}", billno, token, player.getId(), goodsId);
				return false;
			}
			
			player.getRechargeTotal(); //只为刷新数据（兼容历史）
			// 添加当日充值记录
			RechargeDailyEntity entity = newRechargeDailyEntity(player, billno, token, goodsId, payMoney, goodsPrice, currency, rechargeType, rechargeTime, diamonds);
			if (entity.create(true)) {
				player.getData().addPlayerRechargeDailyEntity(entity);
			} else {
				logger.error("recharge record entity create failed, billno: {}, token: {}, playerId: {}, goodsId: {}", billno, token, player.getId(), goodsId);
				if (rechargeDblandFailed(entity)) {
					return false;
				}
			}
			
			// 添加总的充值记录
			RechargeEntity rechargeEntity = entity.toRechargeEntity();
			if (rechargeEntity.create(true)) {
				player.getData().addPlayerRechargeEntity(rechargeEntity);
			}
			
			if (entity.getType() == RechargeType.GIFT) {
				//本服充值计数添加
				player.rechargeTotalAdd(entity.getPayMoney());
				//全服充值计数添加
				RechargeInfo rechargeInfo = new RechargeInfo(player.getOpenId(), player.getId(), player.getPlatId(), player.getServerId(), rechargeTime, diamonds, rechargeType);
				rechargeInfo.setGoodsId(goodsId);
				RedisProxy.getInstance().addRechargeInfo(rechargeInfo);
				RedisProxy.getInstance().roleRechargeTotalAdd(rechargeInfo.getOpenid(), rechargeInfo.getPlayerId(), rechargeInfo.getCount());
			}

			logger.info("create recharge record success, billno: {}, token: {}, playerId: {}, goodsId: {}, goodsPrice: {}, payMoney: {}, currency: {}",
					billno, token, player.getId(), goodsId, goodsPrice, payMoney, currency);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return false;
	}
	
	/**
	 * 创建entity
	 * @return
	 */
	private RechargeDailyEntity newRechargeDailyEntity(Player player, String billno, String token, String goodsId, int payMoney, 
			int goodsPrice, String currency, int rechargeType, int rechargeTime, int diamonds) {
		RechargeDailyEntity entity = new RechargeDailyEntity();
		entity.setBillno(billno);
		entity.setToken(token);
		entity.setServerId(GsConfig.getInstance().getServerId());
		entity.setPlayerId(player.getId());
		entity.setPuid(player.getPuid());
		entity.setType(rechargeType);
		entity.setTime(rechargeTime * 1000L);
		entity.setGoodsId(goodsId);
		entity.setCurrency(currency);
		entity.setPayMoney(payMoney);
		entity.setGoodsPrice(goodsPrice);
		entity.setDiamonds(diamonds);
		return entity;
	}
	
	/**
	 * 判断订单数据落地是否失败
	 * 
	 * @param entity
	 * @return 
	 */
	private boolean rechargeDblandFailed(RechargeDailyEntity entity) {
		List<RechargeDailyEntity> queryList = HawkDBManager.getInstance().query("from RechargeDailyEntity where billno = ?", entity.getBillno());
		return queryList.isEmpty();
	}
	
	/**
	 * 同步商品列表
	 */
	public void syncRechargeInfo(Player player) {
		if (player == null) {
			return;
		}
		
		int priceType = GameUtil.getPriceType(player.getChannel()).intVal();
		RechargeInfoSync.Builder rechargeInfo = RechargeInfoSync.newBuilder();
		rechargeInfo.setPriceType(priceType);
		
		ConfigIterator<PayCfg> payCfgs = HawkConfigManager.getInstance().getConfigIterator(PayCfg.class);
		for (PayCfg payCfg: payCfgs) {
			// 不可见的礼包
			if (payCfg.getPayOrNot() == 0) {
				continue;
			}
			
			GoodsItem.Builder builder = payCfg.toGoodsItem();
			rechargeInfo.addItems(builder);
		}
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.RECHARGE_INFO_SYNC_VALUE, rechargeInfo);
		player.sendProtocol(protocol);
	}
	
	/**
	 * 扫描具体的礼包直购实现类
	 */
	private boolean scanGiftRecharge() {
		List<Class<?>> allClasses = HawkClassScaner.getAllClasses("com.hawk.game.recharge.impl");
		for (Class<?> clazz : allClasses) {
			if (clazz.isAssignableFrom(AbstractGiftRecharge.class)) {
				continue;
			}
			
			try {
				AbstractGiftRecharge obj = (AbstractGiftRecharge) clazz.newInstance();
				if (giftRechargeMap.containsKey(obj.getGiftType())) {
					throw new RuntimeException("GiftRecharge impl repeated: " + obj.getGiftType());
				}
				giftRechargeMap.put(obj.getGiftType(), obj);
			} catch (InstantiationException | IllegalAccessException e) {
				HawkException.catchException(e);
				return false;
			}
		}
		
		giftRechargeMap.put(RechargeType.DEFAULT, new DefaultRecharge());
		
		return true;
	}
	
	/**
	 * 直购礼包购买判断
	 * 
	 * @param giftType
	 * @return
	 */
	public boolean giftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		int giftType = giftCfg.getGiftType();
		if (!giftRechargeMap.containsKey(giftType) && !PayGiftCfg.getAllGiftType().contains(giftType)) {
			HawkLog.errPrintln("payGift check, giftType error, playerId: {}, giftType: {}", player.getId(), giftType);
			return false;
		}
		
		AbstractGiftRecharge giftRecharge = giftRechargeMap.containsKey(giftType) ? giftRechargeMap.get(giftType) : giftRechargeMap.get(RechargeType.DEFAULT);
		if (!giftRecharge.giftBuyCheckPub(player, giftCfg, req, protocol)) {
			return false;
		}
		
		return giftRecharge.detailGiftBuyCheck(player, giftCfg, req, protocol);
	}
	
	/**
	 * 直购礼包购买发货
	 * 
	 * @param giftType
	 * @return
	 */
	public boolean deliverGoods(Player player, PayGiftCfg giftCfg, RechargeDailyEntity rechargeEntity) {
		int giftType = giftCfg.getGiftType();
		if (!giftRechargeMap.containsKey(giftType) && !PayGiftCfg.getAllGiftType().contains(giftType)) {
			HawkLog.errPrintln("payGift deliverGoods, giftType error, playerId: {}, giftType: {}", player.getId(), giftType);
			return false;
		}
		
		GlobalData.getInstance().addRechargePlayer(player.getId());
		AbstractGiftRecharge giftRecharge = giftRechargeMap.containsKey(giftType) ? giftRechargeMap.get(giftType) : giftRechargeMap.get(RechargeType.DEFAULT);
		giftRecharge.deliverGoodsPub(player, giftCfg, rechargeEntity);
		RechargeEntity entity = rechargeEntity.toRechargeEntity();
		return giftRecharge.deliverGoodsDetail(player, giftCfg, entity);
	}
	
}
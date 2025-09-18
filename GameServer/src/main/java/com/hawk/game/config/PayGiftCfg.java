package com.hawk.game.config;

import java.util.HashSet;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.hawk.activity.type.impl.greatGift.cfg.GreatGiftBagCfg;
import com.hawk.activity.type.impl.luckyStar.cfg.LuckyStarGiftCfg;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.protocol.Recharge.GiftItem;
import com.hawk.game.protocol.Recharge.MonthCardItem;
import com.hawk.game.recharge.RechargeType;

/**
 * 充值(道具直购)信息配置
 *
 * @author hawk
 *
 */
@HawkConfigManager.XmlResource(file = "xml/payGift.xml")
public class PayGiftCfg extends HawkConfigBase {
	// 商品id
	@Id
	protected final String id;
	// 支付产品id
	protected final String saleId;
	// 平台类型，安卓还是ios
	protected final String channelType;
	// 是否出售（0否1是）
	protected final int payOrNot;
	// 人民币价格
	protected final int payRMB;
	// 可获得的钻石
	protected final int gainDia;
	// 显示图标
	protected final String show;
	// 奖励ID
	protected final int serverAwardId;
	// 每日购买次数上限
	protected final int payCount;
	protected final String nameData;
	protected final String desData;
	protected final String showPicUrl;
	// 是否免费，购买月卡后在生效期内每日可领取一次免费宝箱
	protected final int isFree;
	// 月卡类型
	protected final int monthCardType;
	// 直购礼包类型
	protected final int giftType;
	
	protected final int pioneerGiftType;
	
	private static Table<String, String, String> saleIdCfgTable = HashBasedTable.create();
	/**
	 * 所有的直购礼包类型
	 */
	private static Set<Integer> giftTypeSet = new HashSet<>();
	
	public PayGiftCfg() {
		id = "";
		channelType = "";
		saleId = null;
		payOrNot = 0;
		payRMB = 0;
		gainDia = 0;
		serverAwardId = 0;
		payCount = 0;
		show = "";
		nameData = "";
		desData = "";
		showPicUrl = "";
		isFree = 0;
		monthCardType = 0;
		giftType = 0;
		pioneerGiftType = 0;
	}
	
	public static String getId(String saleId, String platform) {
		return saleIdCfgTable.get(saleId, platform);
	}

	public String getSaleId() {
		return saleId;
	}

	public int getPayOrNot() {
		return payOrNot;
	}
	
	public int getPioneerGiftType() {
		return pioneerGiftType;
	}

	public GiftItem.Builder toGoodsItem(int lastCount) {
		GiftItem.Builder builder = GiftItem.newBuilder();
		builder.setGoodsId(id);
		builder.setSaleId(saleId);
		builder.setPayOrNot(payOrNot);
		builder.setGainDia(gainDia);
		builder.setPayRMB(payRMB);
		builder.setShowIcon(show);
		builder.setAlreadyTimes(lastCount);
		builder.setTimesLimit(payCount);
		builder.setAward("");
		builder.setShowPrice("");
		ServerAwardCfg cfg = AssembleDataManager.getInstance().getServerAwardByAwardId(serverAwardId);
		if (cfg != null) {
			builder.setAward(cfg.getReward());
			builder.setShowPrice(cfg.getShowPrice());
		}
		return builder;
	}
	
	public MonthCardItem.Builder toMonthCardItem() {
		MonthCardItem.Builder builder = MonthCardItem.newBuilder();
		builder.setGoodsId(id);
		builder.setSaleId(saleId);
		builder.setPayOrNot(payOrNot);
		builder.setPayRMB(payRMB);
		builder.setMonthCardType(monthCardType);
		return builder;
	}

	public String getId() {
		return id;
	}

	public int getPayRMB() {
		return payRMB;
	}

	public int getGainDia() {
		return gainDia;
	}

	public String getShow() {
		return show;
	}

	public String getChannelType() {
		return channelType;
	}

	public int getPayCount() {
		return payCount;
	}

	public int getServerAwardId() {
		return serverAwardId;
	}

	public String getNameData() {
		return nameData;
	}

	public String getDesData() {
		return desData;
	}

	public String getShowPicUrl() {
		return showPicUrl;
	}
	
	public boolean isFree() {
		return isFree == 1;
	}

	public boolean isMonthCard() {
		return monthCardType > 0;
	}
	
	public int getMonthCardType() {
		return monthCardType;
	}
	
	public int getGiftType() {
		return giftType;
	}
	
	public boolean isLuckyStar(){
		LuckyStarGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(LuckyStarGiftCfg.class, getId());
		if(cfg != null){
			return true;
		}
		return false;
	}
	
	/***
	 * 判断是否是超值好礼的直购
	 * @param giftId
	 * @return
	 */
	public boolean isGreatGift(){
		GreatGiftBagCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GreatGiftBagCfg.class, getId());
		if(cfg != null){
			return true;
		}
		return false;
	}
	
	public static Set<Integer> getAllGiftType() {
		return giftTypeSet;
	}
	
	@Override
	public boolean assemble() {
		if (giftType == RechargeType.DEFAULT) {
			HawkLog.errPrintln("payGift giftType error, giftId: {}", id);
			return false;
		}
		
		giftTypeSet.add(giftType);
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		if (saleIdCfgTable.contains(saleId, channelType)) {
			HawkLog.errPrintln("PayGiftCfg saleId repeated, saleId: {}, channelType: {}", saleId, channelType);
			return false;
		}
		
		if (HawkOSOperator.isEmptyString(channelType)) {
			saleIdCfgTable.put(saleId, "ios", id);
			saleIdCfgTable.put(saleId, "android", id);
		} else {
			saleIdCfgTable.put(saleId, channelType, id);
		}
		
		if (monthCardType > 0) {
			return true;
		}
		
		if (serverAwardId != 0) {
			boolean isServerAwardTrue = false;
			ConfigIterator<ServerAwardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ServerAwardCfg.class);
			while (iterator.hasNext()) {
				ServerAwardCfg cfg = iterator.next();
				if (cfg.getServerAwardId() == serverAwardId) {
					isServerAwardTrue = true;
					break;
				}
			}
			
			if (!isServerAwardTrue) {
				HawkLog.errPrintln("ServerAwardCfg... serverAwardId: {}", serverAwardId);
				return false;
			}
		}
		
		switch (getGiftType()) {
		case RechargeType.MONTH_CARD:
			if(!isMonthCard()){
				HawkLog.errPrintln("monthcard giftType error, id: {}", id);
				return false;
			}
			break;
		case RechargeType.LUCKY_STAR:
			if(!isLuckyStar()){
				HawkLog.errPrintln("luckyStar giftType error, id: {}", id);
				return false;
			}
			break;
		case RechargeType.GREAT_GIFT:
			if(!isGreatGift()){
				HawkLog.errPrintln("greatGift giftType error, id: {}", id);
				return false;
			}
			break;
		default:
			break;
		}
		
		return true;
	}
}

package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.game.protocol.Recharge.GoodsItem;

/**
 * 充值信息配置
 *
 * @author hawk
 *
 */
@HawkConfigManager.XmlResource(file = "xml/pay.xml")
public class PayCfg extends HawkConfigBase {

	// 商品id
	@Id
	protected final String id;
	// 支付产品id
	protected final String saleId;
	// 是否出售（0否1是）
	protected final int payOrNot;
	// 是否remain 0不是 1是
	protected final int hot;
	// 人民币价格
	protected final int payRMB;
	// 可获得的钻石
	protected final int gainDia;
	// 首充赠送的钻石数
	protected final int present;
	// 普通赠送
	protected final int additional;
	// 显示图标
	protected final String show;
	// 最小钻石数
	private static int smallestDiamonds;

	public PayCfg() {
		id = "";
		saleId = null;
		payOrNot = 0;
		hot = 0;
		payRMB = 0;
		gainDia = 0;
		present = 0;
		show = "";
		additional = 10;
	}
	
	public String getId() {
		return id;
	}

	public String getSaleId() {
		return saleId;
	}

	public int getPayOrNot() {
		return payOrNot;
	}

	public int getHot() {
		return hot;
	}

	public int getPayRMB() {
		return payRMB;
	}

	public int getGainDia() {
		return gainDia;
	}

	public int getPresent() {
		return present;
	}

	public String getShow() {
		return show;
	}
	
	public boolean assemble() {
		if (smallestDiamonds == 0 || smallestDiamonds > gainDia) {
			smallestDiamonds = gainDia;
		}
		return true;
	}
	
	public static int getSmallestDiamonds() {
		return smallestDiamonds;
	}
	
	public GoodsItem.Builder toGoodsItem() {
		GoodsItem.Builder builder = GoodsItem.newBuilder();
		builder.setGoodsId(id);
		builder.setSaleId(saleId);
		builder.setPayOrNot(payOrNot);
		builder.setGainDia(gainDia);
		builder.setHot(hot);
		builder.setPayRMB(payRMB);
		builder.setShowIcon(show);
		return builder;
	}
	
}

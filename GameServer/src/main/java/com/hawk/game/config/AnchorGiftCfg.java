package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.game.item.ItemInfo;

/**
 * 主播礼物表
 * @author zhenyu.shang
 * @since 2018年4月8日
 */
@HawkConfigManager.XmlResource(file = "xml/anchor_gift.xml")
public class AnchorGiftCfg extends HawkConfigBase {

	@Id
	protected final int id;
	// 礼物名称
	protected final String name;
	// 消耗类型
	protected final String consume;
	// 增加魅力值
	protected final int glamour;
	// 增加积分
	protected final int score;
	
	private ItemInfo itemInfo;
	
	public AnchorGiftCfg() {
		this.id = 0;
		this.name = "";
		this.consume = "";
		this.glamour = 0;
		this.score = 0;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getConsume() {
		return consume;
	}

	public int getGlamour() {
		return glamour;
	}

	public int getScore() {
		return score;
	}
	
	public ItemInfo getItemInfo() {
		return itemInfo.clone();
	}

	@Override
	protected boolean assemble() {
		this.itemInfo = ItemInfo.valueOf(consume);
		return super.assemble();
	}
	
	@Override
	protected boolean checkValid() {
		try {
			ItemInfo.checkItemInfo(itemInfo.getType(), itemInfo.getItemId(), itemInfo.getCount());
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
}

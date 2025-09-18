package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkRand;

import com.hawk.game.util.RandomUtil;
import com.hawk.game.util.WeightAble;

/**
 * 玩家头像配置
 *
 * @author david
 *
 */
@HawkConfigManager.XmlResource(file = "xml/player_show.xml")
public class PlayerShowCfg extends HawkConfigBase implements WeightAble {
	
	public static final int ID_BASE = 701000;
	 
	@Id
	protected final int id;
	/**
	 * 类型type，0免费，1需要购买
	 */
	private final int type;
	/**
	 * 价格
	 */
	private final int price;
	
	public PlayerShowCfg() {
		id = 0;
		type = 0;
		price = 0;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getPrice() {
		return price;
	}
	
	/**
	 * 新玩家随机取一个
	 */
	public static int randPlayerShow() {
		return HawkRand.randInt(0, 1) == 0 ? 0 : 10; //策划需求，新玩家从这两id里随机一个
	}
	
	public static int randmShow() {
		ConfigIterator<PlayerShowCfg> poolList = HawkConfigManager.getInstance().getConfigIterator(PlayerShowCfg.class);
		PlayerShowCfg mingCfg = RandomUtil.random(poolList.toList());
		return mingCfg.getId() - ID_BASE;
	}

	@Override
	public int getWeight() {
		return 1;
	}
}

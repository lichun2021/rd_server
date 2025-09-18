package com.hawk.game.config;

import java.security.InvalidParameterException;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.game.util.RandomUtil;
import com.hawk.game.util.WeightAble;

/***
 * 头像配置表
 * @author yang.rao
 *
 */

@HawkConfigManager.XmlResource(file = "xml/portrait_img.xml")
public class PlayerImageCfg extends HawkConfigBase  implements WeightAble{
	
	/** 默认头像框 **/
	public static final int define_Image = 1;
	
	/** 头像id **/
	private final int id;
	
	/** 名字 **/
	private final String name;
	
	/** 1为默认就可以使用的头像，0为其它 **/
	private final int define;
	
	/** 解锁类型 **/
	private final int unlockType;
	
	/** 解锁条件 **/
	private final int unlockParam;
	
	private final int tabType;
	
	private final int specialType;

	public PlayerImageCfg(){
		id = 0;
		name = "";
		define = 0;
		unlockType = 0;
		unlockParam = 0;
		specialType = 0;
		tabType = 0;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getDefine() {
		return define;
	}
	
	public int getSpecialType() {
		return specialType;
	}
	
	public boolean isSpecialType(){
		return specialType == 1;
	}

	public int getUnlockType() {
		return unlockType;
	}

	public int getUnlockParam() {
		return unlockParam;
	}

	public int getTabType() {
		return tabType;
	}

	@Override
	protected boolean checkValid() {
		if(define != 0 && define != PlayerFrameCfg.define_frame){
			throw new InvalidParameterException(String.format("PlayerImageCfg error, id: %d, Class name: %s ", getId(), getClass().getName())); 
		}
		return super.checkValid();
	}
	
	public static int randmIamge(){
		ConfigIterator<PlayerImageCfg> poolList = HawkConfigManager.getInstance().getConfigIterator(PlayerImageCfg.class);
		PlayerImageCfg mingCfg = RandomUtil.random(poolList.toList());
		return mingCfg.getId();
	}

	@Override
	public int getWeight() {
		return 1;
	}
}

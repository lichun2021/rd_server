package com.hawk.game.config;

import java.security.InvalidParameterException;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/***
 * 头像框配置表
 * @author yang.rao
 *
 */
@HawkConfigManager.XmlResource(file = "xml/portrait_frame.xml")
public class PlayerFrameCfg extends HawkConfigBase {
	
	/** 默认头像框 **/
	public static final int define_frame = 1;
	
	/** 头像id **/
	private final int id;

	/** 名字 **/
	private final String name;
	
	/** 1为默认就可以使用的头像，0为其它 **/
	private final int define;
	
	/** 获取途径描述 **/
	private final String getDes;
	
	/** 使用条件描述 **/
	private final String useDes;
	
	/** 解锁类型 **/
	private final int unlockType;
	
	/** 解锁条件 **/
	private final int unlockParam;
	
	private final String chatBg;
	
	private final String headBgAniRes;
	
	private final String headBg;
	
	private final int tabType;
	
	public PlayerFrameCfg(){
		id = 0;
		name = "";
		define = 0;
		getDes = "";
		useDes = "";
		unlockType = 0;
		unlockParam = 0;
		chatBg = "";
		headBgAniRes = "";
		headBg = "";
		tabType = 0;
	}

	public int getId() {
		return id;
	}


	public String getName() {
		return name;
	}

	public String getGetDes() {
		return getDes;
	}

	public String getUseDes() {
		return useDes;
	}


	public int getUnlockType() {
		return unlockType;
	}

	public int getUnlockParam() {
		return unlockParam;
	}


	public int getDefine() {
		return define;
	}


	public String getChatBg() {
		return chatBg;
	}

	public String getHeadBgAniRes() {
		return headBgAniRes;
	}

	public String getHeadBg() {
		return headBg;
	}

	public int getTabType() {
		return tabType;
	}

	@Override
	protected boolean checkValid() {
		if(define != 0 && define != define_frame){
			throw new InvalidParameterException(String.format("PlayerFrameCfg error, id: %d, Class name: %s ", getId(), getClass().getName())); 
		}
		return super.checkValid();
	}
	
	
}

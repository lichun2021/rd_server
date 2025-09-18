package com.hawk.game.config;

import java.util.HashMap;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 聊天表情配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/chatImg.xml")
public class ChatImgCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	private final String icon; //0x001.png
	
	private final String titleIcon; //0x001.png
	
	private final int page;
	
	private final int openItem; //解锁道具id
	
	private static Map<Integer, Integer> openItemCfgMap = new HashMap<>();
	
	public ChatImgCfg() {
		id = 0;
		icon = "";
		titleIcon = "";
		page = 0;
		openItem = 0;
	}
	
	@Override
	protected boolean assemble() {
		if (openItem > 0) {
			openItemCfgMap.put(openItem, id);
		}
		return true;
	}

	public int getId() {
		return id;
	}

	public String getIcon() {
		return icon;
	}

	public String getTitleIcon() {
		return titleIcon;
	}

	public int getPage() {
		return page;
	}

	public int getOpenItem() {
		return openItem;
	}

	public static int getCfgId(int itemId) {
		return openItemCfgMap.getOrDefault(itemId, 0);
	}
}

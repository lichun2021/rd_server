package com.hawk.game.module.lianmengXianquhx.player.module.guildformation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Objects;
import com.hawk.game.entity.item.GuildFormationCell;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.module.lianmengXianquhx.XQHXBattleRoom;
import com.hawk.game.protocol.MassFormation.MassFormationIndex;

public class XQHXGuildFormationObj extends GuildFormationObj {
	private XQHXBattleRoom parent;
	private String serializStr;
	private long lastCheckUpdate;
	@Override
	public void notifyUpdate() {
	}

	/**
	 * 反序列化
	 */
	@Override
	public void unSerializ(String str) {
		lastCheckUpdate = parent.getCurTimeMil();
		if (Objects.equal(str, serializStr)) {
			return;
		}
		serializStr = str;
		if (formations == null) {
			formations = new ConcurrentHashMap<>();
		}
		if (HawkOSOperator.isEmptyString(str)) {
			return;
		}

		JSONArray arr = JSONArray.parseArray(str);
		arr.forEach(s -> {
			XQHXGuildFormationCell cell = new XQHXGuildFormationCell();
			cell.unSerializ(s.toString());
			cell.setParent(this);

			// 如果不是第一次序列化, 有些信息有用
			cell.copyFrom(formations.get(cell.getIndex()));
			
			formations.put(cell.getIndex(), cell);
		});
	}
	
	public XQHXBattleRoom getParent() {
		return parent;
	}

	public void setParent(XQHXBattleRoom parent) {
		this.parent = parent;
	}

	public String getSerializStr() {
		return serializStr;
	}

	public void setSerializStr(String serializStr) {
		this.serializStr = serializStr;
	}

	public long getLastCheckUpdate() {
		return lastCheckUpdate;
	}

	public void setLastCheckUpdate(long lastCheckUpdate) {
		this.lastCheckUpdate = lastCheckUpdate;
	}

}

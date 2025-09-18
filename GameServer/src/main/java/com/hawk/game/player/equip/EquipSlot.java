package com.hawk.game.player.equip;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.EquipmentCfg;
import com.hawk.game.entity.EquipEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.Equip.PBEquipSlot;

public class EquipSlot implements SerializJsonStrAble {
	private String parentId;

	/** 装备位位置 */
	private int pos;

	/** 装备id */
	private String equipId;

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getEquipId() {
		return equipId;
	}

	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}

	public EquipSlot(String playerId) {
		this.parentId = playerId;
	}

	public void takeOff() {
		this.equipId = "";
	}

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("pos", pos);
		obj.put("equipId", equipId);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.pos = obj.getIntValue("pos");
		this.equipId = obj.getString("equipId");
	}

	public PBEquipSlot toPBObject() {
		PBEquipSlot.Builder builder = PBEquipSlot.newBuilder();
		builder.setPos(this.pos);
		builder.setEquipId(this.equipId == null ? "" : this.equipId);
		builder.setCfgId(getEquipCfgId());
		return builder.build();
	}

	public Player getPlayer() {
		return GlobalData.getInstance().makesurePlayer(this.parentId);
	}

	/**
	 * 获取装备属性
	 * 
	 * @param player
	 * @return
	 */
	public Map<Integer, Integer> getEquipAttr() {
		Map<Integer, Integer> attrMap = new HashMap<>();
		EquipmentCfg cfg = getEquipCfg();
		if (cfg != null) {
			attrMap = cfg.getAttrMap();
		}
		return attrMap;
	}

	/**
	 * 获取装备位战力
	 * 
	 * @return
	 */
	public int getEquipPower() {
		int power = 0;
		EquipmentCfg cfg = getEquipCfg();
		if (cfg != null) {
			power = cfg.getPower();
		}
		return power;
	}
	
	/**
	 * 获取装备配置Id
	 * 
	 * @return
	 */
	public int getEquipCfgId() {
		int cfgId = 0;
		EquipmentCfg cfg = getEquipCfg();
		if (cfg != null) {
			cfgId = cfg.getId();
		}
		return cfgId;
	}

	/**
	 * 获取装备配置
	 * 
	 * @param player
	 * @return
	 */
	public EquipmentCfg getEquipCfg() {
		if (HawkOSOperator.isEmptyString(equipId)) {
			return null;
		}
		EquipEntity entity = getPlayer().getData().getEquipEntity(this.equipId);
		EquipmentCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, entity.getCfgId());
		return cfg;
	}

}

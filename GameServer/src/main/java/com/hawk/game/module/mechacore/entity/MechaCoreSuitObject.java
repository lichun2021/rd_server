package com.hawk.game.module.mechacore.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.module.mechacore.PlayerMechaCore;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleSlotCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.MechaCore.MechaCoreSlotPB;
import com.hawk.game.protocol.MechaCore.MechaCoreSuit;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

/**
 * 机甲核心套装信息
 * @author lating
 *
 */
public class MechaCoreSuitObject implements SplitEntity {
	
	private int suitId;
	
	private String suitName = "";
	
	private Map<Integer, String> slotModuleInfo = new HashMap<>();
	/** 
	 * 作用号 
	 */
	private ImmutableMap<Integer, Integer> effValMap = ImmutableMap.of();
	
	public MechaCoreSuitObject() {
	}
	
	public MechaCoreSuitObject(int suitId) {
		this.suitId = suitId;
	}
	
	public static MechaCoreSuitObject valueOf(int suitId) {
		return new MechaCoreSuitObject(suitId);
	}
	
	@Override
	public SplitEntity newInstance() {
		return new MechaCoreSuitObject();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(suitId);
		dataList.add(suitName);
		String slotModuleStr = SerializeHelper.mapToString(slotModuleInfo, "_", ",");
		dataList.add(slotModuleStr);
	}

	@Override
	public void fullData(DataArray dataArray) {
//		dataArray.setSize(3);
//		suitId = dataArray.getInt();
//		suitName = dataArray.getString();
//		String slotModuleStr = dataArray.getString();
//		
//		if (HawkOSOperator.isEmptyString(slotModuleStr)) {
//			return;
//		}
//		for (String str : slotModuleStr.split(",")) {
//			String[] kv = str.split("_");
//			if (kv.length > 1) {
//				slotModuleInfo.put(Integer.parseInt(kv[0]), kv[1]);
//			} else {
//				slotModuleInfo.put(Integer.parseInt(kv[0]), "");
//			}
//		}
	}
	
	public static Map<Integer, MechaCoreSuitObject> stringToMap(String info) {
		Map<Integer, MechaCoreSuitObject> map = new HashMap<>();
		try {
			String[] strs = info.split("\\|");
			for (String str : strs) {
				String[] objStrs = str.split(":");
				if (objStrs.length < 2) {
					continue;
				}
				String[] objInfos = objStrs[1].split(",");
				int suitId = Integer.parseInt(objInfos[0]);
				MechaCoreSuitObject obj = MechaCoreSuitObject.valueOf(suitId);
				map.put(suitId, obj);
				if (objInfos.length > 1) {
					obj.setSuitName(objInfos[1]);
				}
				for (int i = 2; i < objInfos.length; i++) {
					if (HawkOSOperator.isEmptyString(objInfos[i])) {
						continue;
					}
					String[] moduleInfo = objInfos[i].split("_");
					if (moduleInfo.length == 1) {
						obj.slotModuleUpdate(Integer.parseInt(moduleInfo[0]), "");
					} else {
						obj.slotModuleUpdate(Integer.parseInt(moduleInfo[0]), moduleInfo[1]);
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return map;
	}
	
	@Override
	public String toString() {
		return suitId + "_" + suitName;
	}

	public int getSuitId() {
		return suitId;
	}

	public void setSuitId(int suitId) {
		this.suitId = suitId;
	}

	public String getSuitName() {
		return suitName;
	}

	public void setSuitName(String suitName) {
		this.suitName = suitName;
	}

	public Map<Integer, String> getSlotModuleInfo() {
		return slotModuleInfo;
	}
	
	public void slotModuleUpdate(int slotType, String moduleId) {
		slotModuleInfo.put(slotType, moduleId);
	}

	/**
	 * 从模块中加载作用号
	 * @param suit
	 * @param effId
	 * @return
	 */
	public void loadMoudleEffect(Player player) {
		Map<Integer, Integer> map = new HashMap<>();
		Map<Integer, Float> moduleEffValMap = new HashMap<>();
		for (Entry<Integer, String> entry : slotModuleInfo.entrySet()) {
			int slotType = entry.getKey();
			String moduleUuid = entry.getValue();
			if (HawkOSOperator.isEmptyString(moduleUuid)) {
				continue;
			}
			
			MechaCoreModuleEntity moduleEntity = player.getMechaCoreModuleEntity(moduleUuid);
			if (moduleEntity == null) {
				continue; //在兵种转换预览的时候，取到的entity可能为null
			}
			for (MechaCoreModuleEffObject attrObj : moduleEntity.getRandomAttrEff()) {
				map.merge(attrObj.getEffectType(), attrObj.getEffectValue(), (v1, v2) -> v1 + v2);
			}
			
			MechaCoreModuleSlotCfg slotCfg = player.getPlayerMechaCore().getSlotCfg(slotType);
			MechaCoreModuleCfg moduleCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleCfg.class, moduleEntity.getCfgId());
			int quality = moduleCfg.getModuleQuality();
			float factor = slotCfg.getQualityUpFactorMap().getOrDefault(quality, 1) * 1.0f;
			for(Entry<Integer, Integer> attrEntry : moduleCfg.getFixedAttrMap().entrySet()) {
				moduleEffValMap.merge(attrEntry.getKey(), attrEntry.getValue() * factor, (v1, v2) -> v1 + v2);
			}
		}
		
		for(Entry<Integer, Float> entry : moduleEffValMap.entrySet()) {
			int value = (int) Math.floor(entry.getValue() + 0.5); //四舍五入
			map.merge(entry.getKey(), value, (v1, v2) -> v1 + v2);
		}
		
		effValMap = ImmutableMap.copyOf(map);
	}
	
	public int getEffVal(int effId) {
		return effValMap.getOrDefault(effId, 0);
	}
	
	public MechaCoreSuit.Builder toBuilder() {
		return toBuilder(null, false);
	}
	
	public MechaCoreSuit.Builder toBuilder(PlayerMechaCore mechacore, boolean detail) {
		MechaCoreSuit.Builder suitBuilder = MechaCoreSuit.newBuilder();
		suitBuilder.setType(MechaCoreSuitType.valueOf(this.getSuitId()));
		suitBuilder.setUnlocked(1);
		String name = this.getSuitName();
		if (!HawkOSOperator.isEmptyString(name)) {
			suitBuilder.setName(name);
		}
		
		if (detail) {
			for (Entry<Integer, String> entry : slotModuleInfo.entrySet()) {
				int slotType = entry.getKey();
				String moduleUuid = entry.getValue();
				MechaCoreSlotObject slotObj = mechacore.getSlotObj(slotType);
				MechaCoreSlotPB.Builder slotBuilder =  slotObj.toBuilder(mechacore, moduleUuid);
				suitBuilder.addSlotInfo(slotBuilder);
			}
		}
		
		return suitBuilder;
	}
	
}

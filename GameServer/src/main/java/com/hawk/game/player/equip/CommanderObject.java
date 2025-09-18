package com.hawk.game.player.equip;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.ImmutableList;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;

public class CommanderObject {
	/** 指挥官实体 */
	private CommanderEntity commanderEntity;

	/** 装备孔位信息 */
	private ImmutableList<EquipSlot> equipSlots;

	public CommanderObject(CommanderEntity commanderEntity) {
		this.commanderEntity = commanderEntity;
	}

	public static CommanderObject create(CommanderEntity commanderEntity) {
		CommanderObject commander = new CommanderObject(commanderEntity);
		commander.init();
		commanderEntity.recordCommanderObj(commander);
		return commander;
	}

	public void init() {
		this.equipSlots = ImmutableList.copyOf(loadEquips());
	}
	
	/**
	 * 加载指挥官装备位信息
	 * @return
	 */
	private List<EquipSlot> loadEquips() {
		List<EquipSlot> list = new ArrayList<>();
		int slotSize = ConstProperty.getInstance().getCommanderEquipSlotSize();
		if (!HawkOSOperator.isEmptyString(commanderEntity.getEquipInfo())) {
			JSONArray arr = JSONArray.parseArray(commanderEntity.getEquipInfo());
			arr.forEach(str -> {
				EquipSlot slot = new EquipSlot(this.commanderEntity.getPlayerId());
				slot.mergeFrom((String) str);
				list.add(slot);
			});
		} else {
			for (int i = 1; i <= slotSize; i++) {
				EquipSlot slot = new EquipSlot(this.commanderEntity.getPlayerId());
				slot.setPos(i);
				slot.setEquipId("");
				list.add(slot);
			}
			commanderEntity.notifyChanged(true);
		}
		int currSize = list.size();
		// 新加孔位上限
		if (currSize < slotSize) {
			for (int i = currSize + 1; i <= slotSize; i++) {
				EquipSlot slot = new EquipSlot(this.commanderEntity.getPlayerId());
				slot.setPos(i);
				slot.setEquipId("");
				list.add(slot);
			}
			commanderEntity.notifyChanged(true);
		}
		return list;
	}
	
	public Player getPlayer(){
		return GlobalData.getInstance().makesurePlayer(this.commanderEntity.getPlayerId());
	}

	public ImmutableList<EquipSlot> getEquipSlots() {
		return equipSlots;
	}

	public Optional<EquipSlot> getEquipSlot(int pos) {
		return equipSlots.stream().filter(e -> e.getPos() == pos).findAny();
	}

	public Optional<EquipSlot> getEquipSlot(String equipId) {
		return equipSlots.stream().filter(e -> e.getEquipId().equals(equipId)).findAny();
	}
	
	public void notifyChange() {
		commanderEntity.notifyChanged(true);
	}
	
	public String serializEquip() {
		JSONArray equips = new JSONArray();
		equipSlots.stream().map(EquipSlot::serializ).forEach(equips::add);
		return equips.toJSONString();
	}
	
	public boolean hasEmptySlot() {
		Optional<EquipSlot> opSlot = equipSlots.stream()
				.filter(e -> e.getEquipCfgId() == 0)
				.findFirst();
		return opSlot.isPresent();
	}
}

package com.hawk.game.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.annotation.SerializeField;

import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.module.staffofficer.StaffOfficerSkillCollection;
import com.hawk.game.player.PlayerData;

public class NPCPlayerData extends PlayerData {

	private List<ArmourEntity> armourList = new ArrayList<>();
	
	public NPCPlayerData(String playerId){
		super(playerId);
	}
	
	@Override
	public StaffOfficerSkillCollection getStaffOffic(){
		return StaffOfficerSkillCollection.defaultInst;
	}
	
	/**
	 * 获得所有科技实体
	 */
	@Override
	public List<TechnologyEntity> getTechnologyEntities() {
		return Collections.emptyList();
	}

	/**
	 * 获取天赋列表
	 */
	@Override
	public List<TalentEntity> getTalentEntities() {
		return Collections.emptyList();
	}

	@Override
	public PlayerEntity getPlayerEntity() {
		PlayerEntity result = new PlayerEntity();
		result.setPersistable(false);
		result.setId(BattleService.NPC_ID);
		return result;
	}

	@Override
	public List<StatusDataEntity> getStatusDataEntities() {
		return Collections.emptyList();
	}

	@SerializeField
	public List<ArmourEntity> getArmourEntityList() {
		return armourList;
	}
	
	@SerializeField
	public List<EquipResearchEntity> getEquipResearchEntityList() {
		return Collections.emptyList();
	}
}

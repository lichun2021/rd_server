package com.hawk.game.player.skill.tech;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.hawk.game.config.SkillCfg;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.player.skill.ISkill;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Technology.HPTechSkillSync;
import com.hawk.game.protocol.Technology.TechSkillInfo;

/**
 * 科技技能接口
 * @author golden
 *
 */
public interface ITechSkill extends ISkill {
	
	/**
	 * 使用技能
	 * @param player
	 * @param skillCfg
	 * @return
	 */
	public boolean onCastSkill(Player player);
	
	/**
	 * 是否可以使用技能
	 * @param techEntity
	 * @return
	 */
	default boolean canCastSkill(Player player) {
		// 技能配置不存在
		if(getSkillCfg() == null){
			player.sendError(HP.code.CAST_TECH_SKILL_REQ_C_VALUE, Status.Error.SKILL_CONDITION_NOT_MATCH, 0);
			return false;
		}
		TechnologyEntity entity = getTechEntity(player);
		// 技能未解锁
		if(entity == null || entity.getLevel()<1){
			player.sendError(HP.code.CAST_TECH_SKILL_REQ_C_VALUE, Status.Error.SKILL_UNLOCK, 0);
			return false;
		}
		// 技能cd
		if(entity.getSkillCd()<HawkTime.getMillisecond()){
			player.sendError(HP.code.CAST_TECH_SKILL_REQ_C_VALUE, Status.Error.SKILL_IN_CD, 0);
			return false;
		}
		return true;
	}
	
	/**
	 * 技能进入cd
	 * @param techEntity
	 */
	default void enterCD(Player player) {
		long skillCd = HawkTime.getMillisecond() + getSkillCfg().getCd() * 1000;
		getTechEntity(player).setSkillCd(skillCd);
		HPTechSkillSync.Builder builder = HPTechSkillSync.newBuilder();
		builder.addSkillInfo(TechSkillInfo.newBuilder().setCdEndTime(skillCd).setSkillId(getSkillId()));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TECH_SKILL_SYNC_S, builder));
	}

	/**
	 * 获取skillId
	 * @return
	 */
	default int getSkillId() {
		return getClass().getAnnotation(TechSkill.class).skillID();
	}

	/**
	 * 获取技能配置
	 * @return
	 */
	default SkillCfg getSkillCfg(){
		return HawkConfigManager.getInstance().getConfigByKey(SkillCfg.class, getSkillId());
	}

	/**
	 * 获取技能对应的TechEntity
	 * @param player
	 * @return
	 */
	default TechnologyEntity getTechEntity(Player player){
		int techId = AssembleDataManager.getInstance().getTechIdBySkill(getSkillId());
		return player.getData().getTechEntityByTechId(techId);
	}
	
}

package com.hawk.game.player.skill.talent;

import java.util.List;

import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.hawk.game.config.SkillCfg;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.skill.ISkill;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;

/**
 * 天赋技能接口
 * 
 * @author golden
 *
 */
public interface ITalentSkill extends ISkill {

	default void loginCheck(Player player){}
	
	/**
	 * 使用技能
	 * 
	 * @param player
	 * @param skillCfg
	 * @return
	 */
	public Result<?> onCastSkill(Player player, SkillCfg skillCfg, List<Integer> params);
	
	/**
	 * 是否需要在世界线程执行的技能
	 * @return
	 */
	default boolean isWorldSkill() {
		return false;
	}
	
	/**
	 * 是否可以使用技能
	 * 
	 * @param talentEntity
	 * @return
	 */
	default boolean canCastSkill(TalentEntity talentEntity) {
		return talentEntity.getSkillRefTime() < HawkTime.getMillisecond();
	}

	/**
	 * 技能进入cd
	 * 
	 * @param talentEntity
	 */
	default void skillEnterCd(Player player, int skillId, SkillCfg skillCfg) {
		int continueTime = skillCfg.getContinueTime();
		
		int cd = skillCfg.getCd();
		int buff = player.getEffect().getEffVal(EffType.TALENT_SKILL_CD_REDUS);
		cd = cd - (int)(cd * buff * GsConst.EFF_PER);
		cd = Math.max(0, cd);
		
		// 两套技能共计cd
		List<TalentEntity> entities = player.getData().getTalentSkills(skillId);
		for (TalentEntity entity : entities) {
			entity.setSkillRefTime(HawkTime.getMillisecond() + ((cd + continueTime) * 1000));
			entity.setCastSkillTime(HawkTime.getMillisecond());
			entity.setSkillState(GsConst.TalentSkill.IN_CD);
		}
	}
	
	/**
	 * 主动移除技能buff
	 * 
	 * @param talentEntity
	 */
	default void removeSkillBuff(Player player) {
		
	}
	
	/**
	 * 是否触发技能
	 * @return
	 */
	default boolean touchSkill(Player player, String params) {
		return false;
	}
	
	/**
	 * 是否触发技能
	 * @return
	 */
	default boolean touchSkill(Player player, String param1, int param2) {
		return false;
	}
}

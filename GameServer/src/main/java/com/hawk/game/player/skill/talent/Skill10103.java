package com.hawk.game.player.skill.talent;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.hawk.game.config.SkillCfg;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;

/**
 * 救援：使用技能后的第一次单人出征战斗，损失的士兵会成为伤兵进入医护维修站
 * @author golden
 *
 */
@TalentSkill(skillID = 10103)
public class Skill10103 implements ITalentSkill{

	@Override
	public Result<?> onCastSkill(Player player, SkillCfg skillCfg, List<Integer> params) {
		int buffId = skillCfg.getBuffId();
		player.addStatusBuff(buffId);
		return Result.success();
	}
	
	/**
	 * 技能进入cd
	 * @param talentEntity
	 */
	@Override
	public void skillEnterCd(Player player, int skillId, SkillCfg skillCfg) {
		int cd = skillCfg.getCd();
		int buff = player.getEffect().getEffVal(EffType.TALENT_SKILL_CD_REDUS);
		int buff1550 = player.getEffect().getEffVal(EffType.HERO_1550);
		cd = (int) (cd * GsConst.EFF_PER * (10000 - buff - buff1550));
		cd = Math.max(0, cd);
		
		boolean triger1551 = false;
		if (!player.lastBuff1551Trigered && HawkRand.randInt(10000) < player.getEffect().getEffVal(EffType.HERO_1551)) {
			cd = 1;
			triger1551 = true;
		}
		
		int continueTime = skillCfg.getContinueTime();
		// 两套技能共计cd
		List<TalentEntity> entities = player.getData().getTalentSkills(skillId);
		for (TalentEntity entity : entities) {
			if (entity.getSkillRefTime() != Long.MAX_VALUE) {
				entity.setSkillRefTime(Long.MAX_VALUE);
				entity.setCastSkillTime(HawkTime.getMillisecond());
				continue;
			}
			
			player.lastBuff1551Trigered = triger1551;
			entity.setSkillRefTime(HawkTime.getMillisecond() + ((cd + continueTime) * 1000));
			entity.setSkillState(GsConst.TalentSkill.IN_CD);
			WorldMarchService.logger.info("Skill10103 skillEnterCd, playerId:{}, entityId:{}, skillRefTime:{}, skillState:{}", player.getId(), entity.getId(), entity.getSkillRefTime(), entity.getSkillState());
		}
	}
	
	/**
	 * 主动移除技能buff
	 * 
	 * @param talentEntity
	 */
	@Override
	public void removeSkillBuff(Player player) {
		if (player == null) {
			return;
		}

		StatusDataEntity statusEntity = player.getData().getStatusById(Const.EffType.SKILL_LIFESAVING_BUF_VALUE);
		if (statusEntity != null) {
			if (statusEntity.getEndTime() != 0) {
				statusEntity.setVal(0);
				statusEntity.setEndTime(0);
			}
			player.onBufChange(statusEntity.getStatusId(), 0);
		}
		
		
		// 技能未解锁
		int skillId = 10103;
		SkillCfg skillCfg = HawkConfigManager.getInstance().getConfigByKey(SkillCfg.class, skillId);
		if (skillCfg == null) {
			return;
		}
		
		// 技能进入cd
		skillEnterCd(player, skillId, skillCfg);
		player.getPush().syncTalentSkillInfo();
	}
	
	@Override
	public boolean touchSkill(Player player, String params) {
		boolean touchSkill = true;
		if (player.getData().getEffVal(Const.EffType.SKILL_LIFESAVING_BUF) <= 0
				&& player.getData().getTalentSkillMaxRefTime(10103) != Long.MAX_VALUE) {
			touchSkill = false;
		}

		return touchSkill;
	}
}

package com.hawk.game.player.skill.talent;

import java.util.List;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.hawk.game.config.SkillCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.log.Action;

/**
 * 奋斗至虚脱：消耗所有体力(最高xx点)，视为直接消灭灯亮的MM等级怪物
 * 
 * @author golden
 *
 */
@TalentSkill(skillID = 10304)
public class Skill10304 implements ITalentSkill {
	public static final int SKILL_ID = 10304;

	@Override
	public void loginCheck(Player player) {
		TalentEntity talentEntity = player.getData().getTalentSkill(SKILL_ID);
		// 如果技能持续中. 但无法释放
		if (Objects.nonNull(talentEntity) && talentEntity.getSkillRefTime() == Long.MAX_VALUE && !touchSkill(player, null)) {
			forceCoolDown(player);
		}
	}
	
	@Override
	public Result<?> onCastSkill(Player player, SkillCfg skillCfg, List<Integer> params) {
		int buffId = skillCfg.getBuffId();
		player.addStatusBuff(buffId);
		return Result.success();
	}

	/**
	 * 技能进入cd
	 * 
	 * @param talentEntity
	 */
	@Override
	public void skillEnterCd(Player player, int skillId, SkillCfg skillCfg) {
		int cd = skillCfg.getCd();
		int buff = player.getEffect().getEffVal(EffType.TALENT_SKILL_CD_REDUS);
		cd = cd - (int)(cd * buff * GsConst.EFF_PER);
		cd = Math.max(0, cd);
		
		int continueTime = skillCfg.getContinueTime();
		// 两套技能共计cd
		List<TalentEntity> entities = player.getData().getTalentSkills(skillId);
		for (TalentEntity entity : entities) {
			if (entity.getSkillRefTime() != Long.MAX_VALUE) {
				entity.setSkillRefTime(Long.MAX_VALUE);
				entity.setCastSkillTime(HawkTime.getMillisecond());
				continue;
			}
			entity.setSkillRefTime(HawkTime.getMillisecond() + ((cd + continueTime) * 1000));
			entity.setSkillState(GsConst.TalentSkill.IN_CD);
			WorldMarchService.logger.info("Skill10304 skillEnterCd, playerId:{}, entityId:{}, skillRefTime:{}, skillState:{}", player.getId(), entity.getId(),
					entity.getSkillRefTime(), entity.getSkillState());
		}
	}
	
	/***
	 * 没打到不进cd了
	 */
	public void forceCoolDown(Player player) {
		if (player == null) {
			return;
		}
		StatusDataEntity entity = player.getData().getStatusById(Const.EffType.SKILL_10304_VALUE);
		if (entity != null && entity.getEndTime() != 0) {
			entity.setVal(0);
			entity.setEndTime(0);
			player.onBufChange(entity.getStatusId(), 0);

		}

		// 两套技能共计cd
		List<TalentEntity> entities = player.getData().getTalentSkills(SKILL_ID);
		for (TalentEntity tt : entities) {
			tt.setSkillRefTime(HawkTime.getMillisecond());
			tt.setSkillState(GsConst.TalentSkill.CAN_USE);
		}
		player.getPush().syncTalentSkillInfo();
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

		StatusDataEntity statusEntity = player.getData().getStatusById(Const.EffType.SKILL_10304_VALUE);
		if (statusEntity != null) {
			if (statusEntity.getEndTime() != 0) {
				statusEntity.setVal(0);
				statusEntity.setEndTime(0);
			}
			player.onBufChange(statusEntity.getStatusId(), 0);
		}

		// 技能未解锁

		SkillCfg skillCfg = HawkConfigManager.getInstance().getConfigByKey(SkillCfg.class, SKILL_ID);
		if (skillCfg == null) {
			return;
		}

		// 技能进入cd
		skillEnterCd(player, SKILL_ID, skillCfg);
		player.getPush().syncTalentSkillInfo();
	}

	@Override
	public boolean touchSkill(Player player, String params) {
		StatusDataEntity statusEntity = player.getData().getStatusById(Const.EffType.SKILL_10304_VALUE);
		if (statusEntity != null) {
			if (statusEntity.getEndTime() != 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 扣除体力, 移除buff
	 * 
	 * @return 可打怪次数
	 */
	public int getAtkTimesAndCostVitAndRemoveSkillBuff(Player player, WorldEnemyCfg monsterCfg, EffectParams effectParams, WorldMarch march) {
		int costVit = player.getVit();
		if (costVit < 5) { // TODO 配置： 玩家剩余体力不足以打一次野怪
			return 0;
		}
		
		int maxCostVit = 100;
		maxCostVit = maxCostVit + player.getEffect().getEffVal(EffType.SKILL_COST_VIT_ADD, effectParams);
		
		costVit = Math.min(maxCostVit, costVit);
		int killTimes = costVit / 5; // TODO 配置

		player.consumeVit(killTimes * 5, Action.USE_SKILL_10304); // 扣除体力
		player.getPush().syncPlayerInfo();
		removeSkillBuff(player);

		march.setVitCost(march.getVitCost() + killTimes * 5);
		return killTimes;
	}
}

package com.hawk.game.player.skill.talent;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.hawk.game.battle.BattleService;
import com.hawk.game.config.SkillCfg;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Talent.TalentType;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;

/**
 * 决斗：只针对战力在xx以上，基地等级差距不超过mm级的基地。 使用技能 后，下次出征敌方城堡的队伍将会与对方决斗。 被攻击方援助无效，若被攻击方士兵数量多于攻击方，则按照攻击方比例抽取被攻击方士兵(高等级优先)。 双方损失的士兵将直接死亡。 出征上限为nn个兵
 * 
 * @author golden
 *
 */
@TalentSkill(skillID = 10104)
public class Skill10104 implements ITalentSkill {

	@Override
	public Result<?> onCastSkill(Player player, SkillCfg skillCfg, List<Integer> params) {
		int buffId = skillCfg.getBuffId();
		player.addStatusBuff(buffId);
		return Result.success();
	}

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
			WorldMarchService.logger.info("Skill10104 skillEnterCd, playerId:{}, entityId:{}, skillRefTime:{}, skillState:{}", player.getId(), entity.getId(),
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
		StatusDataEntity entity = player.getData().getStatusById(Const.EffType.SKILL_DUEL_BUF_VALUE);
		if (entity != null && entity.getEndTime() != 0) {
			entity.setVal(0);
			entity.setEndTime(0);
			player.onBufChange(entity.getStatusId(), 0);

		}

		// 两套技能共计cd
		List<TalentEntity> entities = player.getData().getTalentSkills(GsConst.SKILL_10104);
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

		StatusDataEntity entity = player.getData().getStatusById(Const.EffType.SKILL_DUEL_BUF_VALUE);
		if (entity == null) {
			return;
		}

		if (entity.getEndTime() != 0) {
			entity.setVal(0);
			entity.setEndTime(0);
		}
		player.onBufChange(entity.getStatusId(), 0);

		// 技能未解锁
		int skillId = 10104;
		SkillCfg skillCfg = HawkConfigManager.getInstance().getConfigByKey(SkillCfg.class, skillId);
		if (skillCfg == null) {
			return;
		}

		// 技能进入cd
		skillEnterCd(player, skillId, skillCfg);
		player.getPush().syncTalentSkillInfo();
	}

	public int maxSoldier(Player player) {
		SkillCfg skillCfg = HawkConfigManager.getInstance().getConfigByKey(SkillCfg.class, 10104);
		TalentType[] tvalues = TalentType.values();
		int effVal = 0;
		for (TalentType pate : tvalues) {
			EffectParams params = new EffectParams();
			params.setTalent(pate.getNumber());
			effVal = Math.max(effVal, player.getEffect().getEffVal(EffType.SOLDIER_1497, params));
		}
		return NumberUtils.toInt(skillCfg.getParam3(), 150000) + effVal;
	}

	/**
	 * 是否触发技能
	 */
	@Override
	public boolean touchSkill(Player player, String param1, int param2) {
		// param1 战斗力， 1000万
		// param2 次数，5次
		// param3 兵力上限 15万
		Player defPlayer = GlobalData.getInstance().makesurePlayer(param1);
		SkillCfg skillCfg = HawkConfigManager.getInstance().getConfigByKey(SkillCfg.class, 10104);
		if (skillCfg == null) {
			// 版本删掉这个技能了。 配置可能为null
			return false;
		}
		boolean touchSkill = true;
		// 战力小于param1不触发
		if (defPlayer.getPower() < BattleService.getInstance().getDuelPower()) {
			touchSkill = false;
		}
		// 挨打太多
		if (RedisProxy.getInstance().todayDueled(param1) >= NumberUtils.toInt(skillCfg.getParam2())) {
			touchSkill = false;
		}
		// 出兵超过param3不触发
		if (param2 > maxSoldier(player)) {
			touchSkill = false;
		}
		return touchSkill;
	}
}

package com.hawk.game.guild.championship.member;

import java.util.Collections;
import java.util.List;

import com.hawk.game.entity.HeroEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.player.PlayerData;

public class CHAMPlayerData extends PlayerData {
	@Override
	public List<HeroEntity> getHeroEntityList() {
		return Collections.emptyList();
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
			return result;
		}
}

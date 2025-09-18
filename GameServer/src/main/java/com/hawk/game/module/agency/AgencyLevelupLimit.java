package com.hawk.game.module.agency;

import java.util.List;
import java.util.Optional;

import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.superweapon.SuperWeaponService;

/**
 *  ID	        类型	                参数
 1	 建筑达到某等级           带等级的建筑ID
 2	 英雄升级到X星            英雄ID，英雄大星级，英雄小星级
 3  英雄升级到X级             英雄ID，英雄等级
 4 	 指挥官等级	       	等级
 5      研究完成某项科技    	科技ID
 6  所在联盟占领战区        战区数量
 7 	 解锁了某项兵种          兵种ID
 8     野怪等级      	       当前击杀的最高野怪等级
 * @author chechangda
 *
 */
public enum AgencyLevelupLimit {

	BUILD_LEVEL(1) {
		@Override
		public boolean checkLimit(Player player,List<int[]> params) {
			for(int[] arr : params){
				int ctype =arr[0];
				int level = arr[1];
				BuildingType btype =BuildingType.valueOf(ctype);
				if(btype == null){
					return false;
				}
				int buildLevel = player.getData().getBuildingMaxLevel(ctype);
				if(buildLevel < level){
					return false;
				}
			}
			return true;
		}
	},
	HERO_STAR(2) {
		@Override
		public boolean checkLimit(Player player, List<int[]> params) {
			for(int[] arr : params){
				int heroId =arr[0];
				int star = arr[1];
				int step = arr[2];
				Optional<PlayerHero> optional = player.getHeroByCfgId(heroId);
				if(!optional.isPresent()){
					return false;
				}
				PlayerHero hero = optional.get();
				if(hero.getStar() < star||
						(hero.getStar() == star && hero.getStep() < step)){
					return false;
				}
			}
			return true;
		}
	},
	HERO_LEVEL(3) {
		@Override
		public boolean checkLimit(Player player,List<int[]> params) {
			for(int[] arr : params){
				int heroId = arr[0];
				int level = arr[1];
				Optional<PlayerHero> heroOp = player.getHeroByCfgId(heroId);
				if(!heroOp.isPresent()){
					return false;
				}
				PlayerHero hero = heroOp.get();
				if(hero.getLevel() < level){
					return false;
				}
			}
			return true;
		}
	},
	COMMANDER_LEVEL(4) {
		@Override
		public boolean checkLimit(Player player,List<int[]> params) {
			if(params.isEmpty()){
				return true;
			}
			int[] arr = params.get(0);
			int level = arr[0];
			if(player.getLevel() < level){
				return false;
			}
			return true;
		}
	},
	TECHNOLOGY_UNLOCK(5) {
		@Override
		public boolean checkLimit(Player player,List<int[]> params) {
			if(params.isEmpty()){
				return true;
			}
			for(int[] arr : params){
				int techId = arr[0];
				int level =  arr[1];
				TechnologyEntity entity = player.getData().getTechEntityByTechId(techId);
				if (entity == null) {
					return false;
				}
				if(entity.getLevel() < level){
					return false;
				}
			}
			return true;
		}
	},
	GUILD_HOLD_WAR_BUILD(6) {
		@Override
		public boolean checkLimit(Player player,List<int[]> params) {
			if(params.isEmpty()){
				return true;
			}
			int[] arr = params.get(0);
			int countCfg = arr[0];
			String guildId = player.getGuildId();
			int controlCount = SuperWeaponService.getInstance().
					getGuildControlSuperWeapon(guildId).size();
			if(controlCount < countCfg){
				return false;
			}
			return true;
		}
	},
	SOLDIER_UNLOCK(7) {
		@Override
		public boolean checkLimit(Player player,List<int[]> params) {
			if(params.isEmpty()){
				return true;
			}
			int[] arr = params.get(0);
			List<Integer> unlockSoldiers = player.getUnlockedSoldierIds();
			for(int id : arr){
				if(!unlockSoldiers.contains(id)){
					return false;
				}
			}
			return true;
		}
	},
	MONSTER_KILL_MAX_LEVEL(8) {
		@Override
		public boolean checkLimit(Player player, List<int[]> params) {
			if(params.isEmpty()){
				return true;
			}
			int[] arr = params.get(0);
			int limitCfg = arr[0];
			int killLevelMax = player.getData().getMonsterEntity().getMaxLevel();
			if(killLevelMax < limitCfg){
				return false;
			}
			return true;
		}
	}
	;
	
	
	public abstract boolean checkLimit(Player player,List<int[]> params);
	private int type;
	
	AgencyLevelupLimit(int type){
		this.type = type;
	}
	
	public int getType(){
		return this.type;
	}
	
	
	public static AgencyLevelupLimit valueOf(int type){
		switch (type) {
		case 1:return AgencyLevelupLimit.BUILD_LEVEL;
		case 2:return AgencyLevelupLimit.HERO_STAR;
		case 3:return AgencyLevelupLimit.HERO_LEVEL;
		case 4:return AgencyLevelupLimit.COMMANDER_LEVEL;
		case 5:return AgencyLevelupLimit.TECHNOLOGY_UNLOCK;
		case 6:return AgencyLevelupLimit.GUILD_HOLD_WAR_BUILD;
		case 7:return AgencyLevelupLimit.SOLDIER_UNLOCK;
		case 8:return AgencyLevelupLimit.MONSTER_KILL_MAX_LEVEL;
		}
		return null;
	}
	
}

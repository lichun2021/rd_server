package com.hawk.game.player.strength.imp.bonus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.config.HeroCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.TalentSlot;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Hero.PBHeroAttr;
import com.hawk.game.util.GsConst;

/**
 * 战力计算-兵营委任
 * @author Golden
 *
 */
@StrengthType(strengthType = 50)
public class StrengthImp50 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		// 坦克
		HawkTuple2<Integer, Integer> calc1 = calcOnce(playerData, typeCfg.getParam1(), soldierType);
		// 空指部
		HawkTuple2<Integer, Integer> calc2 = calcOnce(playerData, typeCfg.getParam2(), soldierType);
		// 兵营
		HawkTuple2<Integer, Integer> calc3 = calcOnce(playerData, typeCfg.getParam3(), soldierType);
		// 战车工厂
		HawkTuple2<Integer, Integer> calc4 = calcOnce(playerData, typeCfg.getParam4(), soldierType);
		
		int atkValue = Math.min(typeCfg.getAtkAttrMax(), NumberUtils.max(new int[]{calc1.first, calc2.first, calc3.first, calc4.first}));
		int hpValue = Math.min(typeCfg.getHpAttrMax(), NumberUtils.max(new int[]{calc1.second, calc2.second, calc3.second, calc4.second}));
		
		cell.setAtk(atkValue);
		cell.setHp(hpValue);
	}
	
	/**
	 * 单兵营的攻击/血量加成
	 * @param playerData
	 * @param param
	 * @return
	 */
	private HawkTuple2<Integer, Integer> calcOnce(PlayerData playerData, String param, SoldierType soldierType) {
		String[] split = param.split("_");
		
		// 英雄id
		List<Integer> heroIds = new ArrayList<>();
		for (int i = 0; i < split.length - 1; i++) {
			heroIds.add(Integer.parseInt(split[i]));
		}
		
		// 取英雄数量
		int heroCount = Integer.parseInt(split[split.length - 1]);
		
		// 创建一个有序集合,以军事值排序 军事值相同,取id大的
		TreeSet<PlayerHero> heroSet = new TreeSet<>(new Comparator<PlayerHero>() {
			@Override
			public int compare(PlayerHero o1, PlayerHero o2) {
				int val1 = o1.attrs().get(101).getNumber();
				int val2 = o2.attrs().get(101).getNumber();
				if (val1 == val2) {
					return o2.getCfgId() - o1.getCfgId();
				}
				return val2 - val1;
			}
		});
		
		// 取所有英雄
		List<PlayerHero> heros = playerData.getHeroEntityByCfgId(heroIds).stream()
				.map(HeroEntity::getHeroObj)
				.collect(Collectors.toList());
		for (PlayerHero hero : heros) {
			PBHeroAttr pbHeroAttr = hero.attrs().get(101);
			if (pbHeroAttr == null) {
				continue;
			}
			heroSet.add(hero);
		}
		
		if (heroSet.isEmpty()) {
			return new HawkTuple2<Integer, Integer>(0, 0);
		}
		
		int atkValue = 0;
		int hpValue = 0;
		for (int i = 0; i < heroCount; i++) {
			PlayerHero hero = heroSet.pollFirst();
			if (hero == null) {
				continue;
			}
			// 后勤值
			PBHeroAttr pbHeroAttr = hero.attrs().get(103);
			if (pbHeroAttr == null) {
				continue;
			}
			long heroAttr = pbHeroAttr.getNumber();
			
			// 天赋值
			long talentExp = 0;
			for (TalentSlot slot : hero.getTalentSlots()) {
				if (slot == null || slot.getTalent() == null) {
					continue;
				}
				talentExp += slot.getTalent().getExp();
			}
			
			HeroCfg heroCfg = hero.getConfig();
			atkValue += Math.min(heroAttr * 10000 / heroCfg.getMaxLogistics(), 10000) * heroCfg.getOfficeSelfAtkAttr(soldierType.getNumber()) * GsConst.EFF_PER;
			atkValue += Math.min(talentExp * 10000 / heroCfg.getMaxExclusiveTalent(), 10000) * heroCfg.getOfficeTalentAtkAttr(soldierType.getNumber()) * GsConst.EFF_PER;
			
			hpValue += Math.min(heroAttr * 10000 / heroCfg.getMaxLogistics(), 10000) * heroCfg.getOfficeSelfHpAttr(soldierType.getNumber()) * GsConst.EFF_PER;
			hpValue += Math.min(talentExp * 10000 / heroCfg.getMaxExclusiveTalent(), 10000) * heroCfg.getOfficeTalentHpAttr(soldierType.getNumber()) * GsConst.EFF_PER;
		}
		
		return new HawkTuple2<Integer, Integer>(atkValue, hpValue);
	}
}
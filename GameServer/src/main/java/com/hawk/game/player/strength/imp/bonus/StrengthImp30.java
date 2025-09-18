package com.hawk.game.player.strength.imp.bonus;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.hawk.game.config.HeroCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Hero.PBHeroAttr;
import com.hawk.game.util.GsConst;

/**
 * 出征英雄
 * @author Golden
 *
 */
@StrengthType(strengthType = 30)
public class StrengthImp30 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
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

		List<PlayerHero> heros = playerData.getHeroEntityList().stream().map(HeroEntity::getHeroObj)
				.collect(Collectors.toList());
		for (PlayerHero hero : heros) {
			HeroCfg heroCfg = hero.getConfig();
			if (heroCfg.getMaxMilitary() > 0) {
				heroSet.add(hero);
			}
		}

		if (heroSet.isEmpty()) {
			return;
		}
		
		int atkValue = 0;
		int hpValue = 0;

		// 取军事值前几的英雄
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		int heroCount = Integer.parseInt(typeCfg.getParam1());
		for (int i = 0; i < heroCount; i++) {
			PlayerHero hero = heroSet.pollFirst();
			if (hero == null) {
				continue;
			}
			PBHeroAttr pbHeroAttr = hero.attrs().get(101);
			if (pbHeroAttr == null) {
				continue;
			}
			long heroAttr = pbHeroAttr.getNumber();
			HeroCfg heroCfg = hero.getConfig();
			atkValue += Math.min(heroAttr * 10000 / heroCfg.getMaxMilitary(), 10000) * heroCfg.getMarchSelfAtkAttr(soldierType.getNumber()) * GsConst.EFF_PER;
			hpValue += Math.min(heroAttr * 10000 / heroCfg.getMaxMilitary(), 10000) * heroCfg.getMarchSelfHpAttr(soldierType.getNumber()) * GsConst.EFF_PER;
		}
		cell.setAtk(Math.min(atkValue, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpValue, typeCfg.getHpAttrMax()));
	}
}

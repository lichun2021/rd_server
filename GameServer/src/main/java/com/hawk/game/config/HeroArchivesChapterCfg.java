package com.hawk.game.config;

import java.util.HashSet;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 英雄档案馆章节配置
 *
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/hero_archives_chapter.xml")
public class HeroArchivesChapterCfg extends HawkConfigBase {
	@Id
	protected final int chapterId;
	// 章节英雄
	protected final String chapterHeroId;

	private Set<Integer> heroIdSet;
	
	public HeroArchivesChapterCfg() {
		chapterId = 0;
		chapterHeroId = "";
	}

	public int getChapterId() {
		return chapterId;
	}
	
	/**
	 * 章节是否存在英雄
	 * @param heroId
	 * @return
	 */
	public boolean hasHero(int heroId) {
		return heroIdSet.contains(heroId);
	}
	
	/**
	 * 获取存在档案数量
	 * @param heroIds
	 * @return
	 */
	public int containsCount(Set<Integer> heroIds) {
		int count = 0;
		for (Integer id : heroIds) {
			if (heroIdSet.contains(id)) {
				count++;
			}
		}
		return count;
	}
	
	@Override
	protected boolean assemble() {
		Set<Integer> heroIdSet = new HashSet<>();
		if (!HawkOSOperator.isEmptyString(chapterHeroId)) {
			String[] split = chapterHeroId.split(",");
			for (int i = 0; i < split.length; i++) {
				heroIdSet.add(Integer.valueOf(split[i]));
			}
		}
		this.heroIdSet = heroIdSet;
		return true;
	}
}

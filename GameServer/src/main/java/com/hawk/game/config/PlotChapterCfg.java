package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 剧情战役 关卡表
 * 章节表包含关卡表
 * @author jm
 *
 */
@HawkConfigManager.XmlResource(file = "xml/plot_chapter.xml")
public class PlotChapterCfg extends HawkConfigBase {
	/**
	 * 章节ID
	 */
	@Id
	protected final int chapterId;
	/**
	 * 序号
	 */
	protected final  int sequenceNo;
	/**
	 * 章节名字
	 */
	protected final String chapterName;
	
	

	public PlotChapterCfg() {
		chapterId = 1;
		sequenceNo = 1;
		chapterName = "";
	}
	
	public int getChapterId() {
		return chapterId;
	}
	
	public int getSequenceNo() {
		return sequenceNo;
	}
	
	public String getChapterName() {
		return chapterName;
	}
	
	public static boolean isExistChapterId(int chapterId) {
		return HawkConfigManager.getInstance().getConfigByKey(PlotChapterCfg.class, chapterId) != null;
	}
}

package com.hawk.game.util;

import com.hawk.game.service.GuildService;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.GuildFlagCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.protocol.Status;

public class GuildUtil {
	
	/**
	 * 随机联盟简称后缀
	 * @param tryTimes
	 * @return
	 */
	public static String getTagRandomStr(int tryTimes) {
		int a = 'a';
		if (tryTimes < 26) {
			char c = (char) (a + tryTimes);
			return String.valueOf(c);
		} else if (tryTimes >= 26 && tryTimes < 26 * 26 + 26) {
			tryTimes = tryTimes - 26;
			int first = tryTimes / 26;
			int second = tryTimes % 26;
			char f = (char) (a + first);
			char s = (char) (a + second);
			return String.valueOf(f) + String.valueOf(s);
		} else if (tryTimes >= 26 * 26 + 26 && tryTimes < 26 * 26 * 26 + 26 * 26 + 26) {
			tryTimes = tryTimes - 26 * 26 - 26;
			int first = tryTimes / (26 * 26);
			int second = (tryTimes) % (26 * 26) / 26;
			int third = (tryTimes) % (26 * 26) % 26;
			char f = (char) (a + first);
			char s = (char) (a + second);
			char t = (char) (a + third);
			return String.valueOf(f) + String.valueOf(s) + String.valueOf(t);
		}
		return "";
	}
	
	/**
	 * 检测联盟名字合法性
	 * 
	 * @param name 联盟名字
	 * @return
	 */
	public static int checkGuildName(String name) {
		int nameLength = GameUtil.getStringLength(name);
		if (nameLength > GuildConstProperty.getInstance().getGuildNameMax() || nameLength < GuildConstProperty.getInstance().getGuildNameMin()) {
			return Status.NameError.NAME_LENGTH_ERROR_VALUE;
		}

		int regexType = GsConst.RegexType.ALLLETTER + GsConst.RegexType.NUM + GsConst.RegexType.CHINESELETTER;
		if (!GameUtil.stringOnlyContain(name, regexType, "-_")) {
			return Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE;
		}

		if (checkGuildNameExist(name)) {
			return Status.NameError.ALREADY_EXISTS_VALUE;
		}

		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 检测联盟领地名字合法性
	 * 
	 * @param name 联盟名字
	 * @return
	 */
	public static int checkGuildManorName(String name) {
		int nameLength = GameUtil.getStringLength(name);
		if (nameLength > GuildConstProperty.getInstance().getManorNameMax() || nameLength < GuildConstProperty.getInstance().getManorNameMin()) {
			return Status.NameError.NAME_LENGTH_ERROR_VALUE;
		}

		int regexType = GsConst.RegexType.ALLLETTER + GsConst.RegexType.NUM + GsConst.RegexType.CHINESELETTER;
		if (!GameUtil.stringOnlyContain(name, regexType, "-_")) {
			return Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE;
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	
	/**
	 * 检测预设行军名字合法性
	 * 
	 * @param name 行军名字
	 * @return
	 */
	public static int checkPresetMarchName(String name) {
		if(HawkOSOperator.isEmptyString(name)){
			return Status.NameError.NAME_BLANK_ERROR_VALUE;
		}

		int nameLength = GameUtil.getStringLength(name);
		if (nameLength > 8 || nameLength < 2) {
			return Status.NameError.MARCH_NAME_LENGTH_ERROR_VALUE;
		}
		int regexType = GsConst.RegexType.ALLLETTER + GsConst.RegexType.NUM + GsConst.RegexType.CHINESELETTER;
		if (!GameUtil.stringOnlyContain(name, regexType, "-_")) {
			return Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE;
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 检测联盟是否存在
	 * 
	 * @param name
	 * @return
	 */
	private static boolean checkGuildNameExist(String name) {
		return GlobalData.getInstance().isGuildNameExist(name);
	}

	/**
	 * 检测联盟简称合法性(特例:以字符串长度判断)
	 * 
	 * @param tag
	 * @return
	 */
	public static int checkGuildTag(String tag) {
		if(HawkOSOperator.isEmptyString(tag)){
			return Status.NameError.NAME_BLANK_ERROR_VALUE;
		}
		int length = tag.length();
		if (length != GuildConstProperty.getInstance().getGuildTagLength()) {
			return Status.NameError.NAME_LENGTH_ERROR_VALUE;
		}

		if (!GameUtil.stringOnlyContain(tag, GsConst.RegexType.ALLLETTER + GsConst.RegexType.NUM + GsConst.RegexType.CHINESELETTER, null)) {
			return Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE;
		}

		if (checkGuildTagExist(tag)) {
			return Status.NameError.ALREADY_EXISTS_VALUE;
		}

		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 检测联盟简称是否存在
	 * 
	 * @param tag
	 * @return
	 */
	private static boolean checkGuildTagExist(String tag) {
		return GlobalData.getInstance().isGuildTagExist(tag);
	}

	/**
	 * 检测联盟旗帜是否存在
	 * 
	 * @param flagId
	 * @return
	 */
	public static int checkFlag(String guildId, int flagId) {
		GuildFlagCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GuildFlagCfg.class, flagId);
		if (cfg == null) {
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		if(cfg.getType() == GuildFlagCfg.REWARD){
			if(!GuildService.getInstance().checkRewardFlag(guildId, flagId)){
				return Status.SysError.CONFIG_ERROR_VALUE;
			}
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 联盟宣言合法性
	 * 
	 * @param announcement
	 * @return
	 */
	public static boolean checkAnnouncement(String announcement) {
		if (announcement.length() > GuildConstProperty.getInstance().getGuildDeclarationLength()) {
			return false;
		}

		return true;
	}

	/**
	 * 联盟通告合法性
	 * 
	 * @param notice
	 * @return
	 */
	public static boolean checkNotice(String notice) {
		if (notice.length() > GuildConstProperty.getInstance().getGuildDeclarationLength()) {
			return false;
		}

		return true;
	}

	/**
	 * 联盟留言合法性
	 * 
	 * @param message
	 * @return
	 */
	public static boolean checkMessage(String message) {
		if (message.length() > GuildConstProperty.getInstance().getAllianceLeaveMsgLenMax()) {
			return false;
		}

		return true;
	}

	/**
	 * 检测联盟等级称谓
	 * 
	 * @param name
	 * @return
	 */
	public static boolean checkGuildLevelName(String name) {
		if(HawkOSOperator.isEmptyString(name)){
			return false;
		}
		int nameLength = GameUtil.getStringLength(name);
		if (nameLength > GuildConstProperty.getInstance().getGuildLvlNameMax() || nameLength < GuildConstProperty.getInstance().getGuildLvlNameMin()) {
			return false;
		}

		int regexType = GsConst.RegexType.ALLLETTER + GsConst.RegexType.NUM + GsConst.RegexType.SPACE + GsConst.RegexType.CHINESELETTER;
		if (!GameUtil.stringOnlyContain(name, regexType, null)) {
			return false;
		}

		return true;
	}

	/**
	 * 过滤emoji表情字符
	 * @param source
	 * @return
	 */
	public static String filterEmoji(String source) {
		if (!HawkOSOperator.isEmptyString(source)) {
			return source.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", " ");
		} else {
			return source;
		}
	}

}

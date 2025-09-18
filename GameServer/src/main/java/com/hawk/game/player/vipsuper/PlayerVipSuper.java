package com.hawk.game.player.vipsuper;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.VipSuperCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Item.SuperVipInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;

public class PlayerVipSuper {
	/**
	 * 每日礼包补发天数上限
	 */
	static final int DAILY_GIFT_TOP_NUM = 7;
	
	/**
	 * 至尊vip会员信息
	 */
	protected PlayerSuperVipInfo superVipInfo;
	/**
	 * 所属玩家
	 */
	protected Player player;
	/**
	 * 上一次点击皮肤特效激活（或取消激活）的时间
	 */
	private long lastSkinActivateClickTime;
	
	public PlayerVipSuper(Player player) {
		this.player = player;
	}
	
	public PlayerSuperVipInfo getSuperVipInfo() {
		return superVipInfo;
	}
	
	public long getLastSkinActivateClickTime() {
		return lastSkinActivateClickTime;
	}

	public void setLastSkinActivateClickTime(long lastSkinActivateClickTime) {
		this.lastSkinActivateClickTime = lastSkinActivateClickTime;
	}
	
	/**
	 * 初始化至尊vip信息
	 */
	private void initSuperVipInfo() {
		superVipInfo = RedisProxy.getInstance().getSuperVipInfo(player.getId());
		if (superVipInfo == null) {
			superVipInfo = new PlayerSuperVipInfo(player.getId());
			superVipInfo.setActivatedPeriodMonth(HawkTime.getYyyyMMddIntVal()/100);
			superVipInfo.setLoginTime(HawkTime.getMillisecond());
			superVipInfo.setDailyGiftRecieveTime(superVipInfo.getLoginTime() - HawkTime.DAY_MILLI_SECONDS);
			superVipInfo.setLoginDays(1);
			superVipInfo.setLastLoginScore(ConstProperty.getInstance().getSuperVipDailyLoginScore());
		} else {
			fixOldPlayerSuperVipState();
		}
		
		// 前面只是部分数据的初始化，此处需要去进行升级操作
		superVipActualLevelup();
	}
	
	/**
	 * 修正老玩家的至尊vip状态（针对至尊vip二期优化更新前，已经达到至尊vip档次的玩家）
	 */
	private void fixOldPlayerSuperVipState() {
		if (superVipInfo.getActivatedLevel() > 0 && superVipInfo.getActiveEndTime() == 0) {
			superVipInfo.setActiveEndTime(-1); // 已经是自动激活的情形
		} else if (superVipInfo.getActivatedLevel() == 0 && superVipInfo.getActiveEndTime() < 0) {
			superVipInfo.setActiveEndTime(0); // 非激活状态
		}
	}
	
	/**
	 * 判断至尊vip是否已激活
	 * @return
	 */
	public boolean isSuperVipActivated() {
		if (!isSuperVipOpen()) {
			return false;
		}
		
		if (isAutoActivated()) {
			return true;
		} 
		
		return superVipInfo.getActiveEndTime() > HawkTime.getMillisecond();
	}
	
	/**
	 * 判断至尊vip是否自动激活
	 * @return
	 */
	public boolean isAutoActivated() {
		return superVipInfo.getActiveEndTime() < 0;
	}
	
	/**
	 * 获取当日可领取的积分数量
	 * 
	 * @param timeNow
	 * @return
	 */
	private int getLoginScore(long timeNow) {
		int crossDay = HawkTime.getCrossDay(timeNow, superVipInfo.getLoginTime(), 0);
		if (crossDay == 0) {
			return superVipInfo.getLastLoginScore();
		}
		
		int loginScore = superVipInfo.getLastLoginScore();
		if (crossDay > 1) {
			loginScore = ConstProperty.getInstance().getSuperVipDailyLoginScore();
		} else if (loginScore < ConstProperty.getInstance().getLoginMaximum()) {
			loginScore += ConstProperty.getInstance().getSuperVipDailyLoginScoreStep();
			loginScore = Math.min(loginScore, ConstProperty.getInstance().getLoginMaximum());
		}
		
		return loginScore;
	}
	
	/**
	 * 至尊vip升级
	 */
	public void superVipLevelUp() {
		if (!isSuperVipOpen()) {
			return;
		}
		
		if (superVipInfo == null) {
			initSuperVipInfo();
		}
		
		// superVipInfo 为 null 时看似会调用两次升级接口，但实际第二次不会进行升级操作了
		superVipActualLevelup();
	}
	
	/**
	 * 至尊vip升级
	 */
	private void superVipActualLevelup() {
		final int oldLevel = superVipInfo.getActualLevel();
		final int finalLevel = VipSuperCfg.getVipSuperLevel(player.getVipExp());
		if (finalLevel <= oldLevel) {
			return;
		}
		
		for (int level = oldLevel + 1; level <= finalLevel; level++) {
			VipSuperCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipSuperCfg.class, level);
			if (vipCfg == null) {
				continue;
			}
			
			List<ItemInfo> items = vipCfg.getFirstGiftItems();
			if (items.isEmpty()) {
				continue;
			}
			
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItemInfos(items);
			awardItems.rewardTakeAffectAndPush(player, Action.UPER_VIP_LEVEL_UP_AWARD);
			MailParames.Builder mailParames = MailParames.newBuilder()
					.setMailId(MailId.SUPER_VIP_LEVE_REWARD)
					.setPlayerId(player.getId())
					.addContents(level)
					.setRewards(items)
					.setAwardStatus(MailRewardStatus.GET);
			SystemMailService.getInstance().sendMail(mailParames.build());
		}
		
		long timeNow = HawkTime.getMillisecond();
		superVipInfo.setActualLevel(finalLevel);
		superVipInfo.setActivatedLevel(finalLevel);
		superVipInfo.setDailyGiftRecieveTime(timeNow - HawkTime.DAY_MILLI_SECONDS);
		superVipInfo.getMonthGiftRecieved().clear();
		// 首次激活时，下个月默认激活（相当于是送一个月）
		if (oldLevel == 0) {
			superVipInfo.setMonthVipScore(VipSuperCfg.getMaxActivatePoints());
		}
		
		updateSuperVipInfo();
		SuperVipInfo.Builder builder = toBuilder(timeNow, true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.SUPER_VIP_INFO_SYNC, builder));
		
		LogUtil.logSuperVipActive(player, 0, finalLevel, 1, 0, 0);
		HawkLog.logPrintln("super vip level up, playerId: {}, oldLevel: {}, newLevel: {}", player.getId(), oldLevel, finalLevel);
	}
	
	/**
	 * 添加本月至尊积分
	 * 
	 * @param score
	 */
	public void addMonthSuperVipScore(int score) {
		if (!isSuperVipOpen()) {
			return;
		}
		
		if (superVipInfo == null) {
			initSuperVipInfo();
		}
		
		superVipInfo.setMonthVipScore(superVipInfo.getMonthVipScore() + score);
		updateSuperVipInfo();
		syncSuperVipInfo(false);
		HawkLog.logPrintln("super vip add score, playerId: {}, super vipLevel: {}, activated level: {}, addScore: {}, newScore: {}", player.getId(), superVipInfo.getActualLevel(), superVipInfo.getActivatedLevel(), score, superVipInfo.getMonthVipScore());
	}
	
	/**
	 * 判断是否开启了至尊vip
	 * 
	 * @return
	 */
	public boolean isSuperVipOpen() {
		return player.getVipExp() >= VipSuperCfg.getMinVipExp();
	}
	
	/**
	 *  外界取实际激活的至尊vip等级
	 *  
	 * @return
	 */
	public int getActivatedSuperVipLevel() {
		if (!isSuperVipOpen()) {
			return 0;
		}
		
		if (superVipInfo == null) {
			checkSuperVipCrossMonth();
		}
		
		return getActivatedLevel();
	}
	
	/**
	 * 获取至尊vip等级
	 * @return
	 */
	private int getActivatedLevel() {
		return isSuperVipActivated() ? superVipInfo.getActivatedLevel() : 0;
	}
	
	/**
	 * 至尊vip皮肤特效已激活等级
	 * 
	 * @return
	 */
	public int getSuperVipSkinActivatedLevel() {
		int activatedVipLevel = getActivatedSuperVipLevel();
		if (activatedVipLevel <= 0) {
			return 0;
		}
		return superVipInfo.getSkinEffActivated();
	}
	
	/**
	 * 检测至尊vip激活周期跨自然月
	 */
	public boolean checkSuperVipCrossMonth() {
		if (!isSuperVipOpen()) {
			return false;
		}
		
		if (superVipInfo == null) {
			initSuperVipInfo();
		}
		
		long monthTime = superVipInfo.getActivatedPeriodMonth();
		int day = HawkTime.getYyyyMMddIntVal();
		int monthNow = day / 100;
		if (monthTime == monthNow) {
			return false;
		}
		
		boolean crossMonth = monthNow - monthTime > 1;
		int lastyear = (int) (monthTime / 100);
		int thisyear = monthNow / 100;
		if (lastyear != thisyear) {
			int tmp = (thisyear-1) * 100 + 12 + monthNow % 100;
			crossMonth = tmp - monthTime > 1;  
		}
		
		long timeNow = HawkTime.getMillisecond();
		// 先把上个月该补的补发完（隔月的情况下）
		checkMonthMissedDailyGift(timeNow, crossMonth);
		
		int monthScore = crossMonth ? 0 : superVipInfo.getMonthVipScore();
		int loginDays = superVipInfo.getLoginDays();
		int loginScore = superVipInfo.getLastLoginScore();
		long dailyGiftTime = superVipInfo.getDailyGiftRecieveTime();
		long activeEndTime = superVipInfo.getActiveEndTime();
		int crossDay = HawkTime.getCrossDay(timeNow, superVipInfo.getLoginTime(), 0);
		
		superVipInfo = new PlayerSuperVipInfo(player.getId());
		superVipInfo.setActualLevel(VipSuperCfg.getVipSuperLevel(player.getVipExp()));
		superVipInfo.setActivatedPeriodMonth(monthNow);
		superVipInfo.setLoginTime(timeNow);
		superVipInfo.setDailyGiftRecieveTime(dailyGiftTime);
		
		// 上个月非自动激活的情况下，激活期已经结束了
		if (activeEndTime >= 0 && GameUtil.isCrossMonth(activeEndTime, timeNow)) {
			int dayNow = day % 100;  // 当前月份的第几天
			long time = HawkTime.getAM0Date().getTime() - (dayNow - 1) * HawkTime.DAY_MILLI_SECONDS;
			superVipInfo.setDailyGiftRecieveTime(time - 5000); // 设定到上个月的最后一天的某个时间点
		}
		
		if (crossDay == 1) {
			superVipInfo.setLoginDays(loginDays + 1);
			if (loginScore < ConstProperty.getInstance().getLoginMaximum()) {
				loginScore += ConstProperty.getInstance().getSuperVipDailyLoginScoreStep();
				loginScore = Math.min(loginScore, ConstProperty.getInstance().getLoginMaximum());
			}
		} else {
			superVipInfo.setLoginDays(crossDay == 0 ? loginDays : 1);
			loginScore = ConstProperty.getInstance().getSuperVipDailyLoginScore();
		}
		
		superVipInfo.setLastLoginScore(loginScore);
		int vipLevel = superVipInfo.getActualLevel();
		VipSuperCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipSuperCfg.class, vipLevel);
		if (monthScore >= vipCfg.getActivatePoints()) {
			superVipInfo.setActivatedLevel(vipCfg.getLevel());
			LogUtil.logSuperVipActive(player, monthScore, vipCfg.getLevel(), 2, 0, -1);
		} else {
			superVipInfo.setActiveEndTime(0);
		}
		
		updateSuperVipInfo();
		HawkLog.logPrintln("superVip active period cross month, playerId: {}, super vipLevel: {}, monthTime: {}, monthNow: {}, activatedLevel: {}", player.getId(), superVipInfo.getActualLevel(), monthTime, monthNow, superVipInfo.getActivatedLevel());
		return true;
	}
	
	/**
	 * 跨月时补发上个月的错过的礼包
	 * 
	 * 情形1：当月自动激活的状态下，离线几天后登录，登录时间还是在当月，从上次领取到现在，最多补6封邮件
	 * 情形2：当月自动激活的状态下，离线几天跨月了，进入到下月还是自动激活状态，不管玩家是在几号登录，从上次领取到现在，最多补6封邮件
	 * 情形3：当月自动激活的状态下，离线几天跨月了，进入到下月是非激活的状态，不管玩家是在几号登录，从登录时间往前追溯7天，7天之中在上个月的那几天补发，剩下的不补
	 * 
	 * 情形4：当月非自动激活状态下，手动激活（激活结束期还在当月），离线几天后登录，登录时间还在当月，从上一次领取时间起，到当前时间或手动激活结束期（取两者中较早的那个时间），最多补6封邮件
	 * 情形5：当月非自动激活状态下，手动激活（激活结束期超出当月了），离线几天后登录，登录时间还在当月，从上一次领取时间起到当前时间，最多补6封邮件
	 * 情形6：当月非自动激活状态下，手动激活，离线几天跨月了，进入到下月是自动激活状态，登录，不管玩家是在几号登录，从登录时间往前追溯7天，7天之中在登录当月的天数直接补发，在上月的天数，看是否在手动激活结束期内，只对结束期内的天数补发
	 * 情形7：当月非自动激活状态下，手动激活，离线几天跨月了，进入到下月是非激活状态，登录，不管玩家是在几号登录，从登录时间往前追溯7天，7天之中在登录当月的天数不补发，在上月的天数，看是否在手动激活结束期内，只对结束期内的天数补发
	 * 
	 */
	private void checkMonthMissedDailyGift(long timeNow, boolean crossMonth) {
		if (crossMonth) {
			return;
		}
		
		int day = HawkTime.getYyyyMMddIntVal();
		int dayNow = day % 100;  // 当前月份的第几天
		if (dayNow >= DAILY_GIFT_TOP_NUM) {
			return;
		}
		
		// 上个月需要补发的天数
		int lastMonthDayNum = DAILY_GIFT_TOP_NUM - dayNow;
		// 获取这个月的0点时刻（即上个月补发的终点），后面加1秒是为了避免正好卡0点可能出现的尬尴问题
		long lastMonthEndTime = HawkTime.getAM0Date().getTime() - (dayNow - 1) * HawkTime.DAY_MILLI_SECONDS + 1000;
		// 获取上个月补发的起始时间点
		long lastMonthStartTime = lastMonthEndTime - lastMonthDayNum * HawkTime.DAY_MILLI_SECONDS;
		
		// 对上个月错失的天数进行补发
		checkMissedSuperVipDailyGift(lastMonthStartTime, lastMonthEndTime);
	}
	
	/**
	 * 判断跨天
	 */
	public boolean checkCrossDay(long nowTime) {
		if (!isSuperVipOpen()) {
			return false;
		}
		
		if (superVipInfo == null) {
			initSuperVipInfo();
		}
		
		long loginTime = superVipInfo.getLoginTime();
		int crossDay = HawkTime.getCrossDay(nowTime, loginTime, 0);
		if (crossDay <= 0) {
			return false;
		}
		
		int loginScore = 0;
		superVipInfo.setLoginTime(nowTime);
		if (crossDay == 1) {
			loginScore = superVipInfo.getLastLoginScore();
			superVipInfo.setLoginDays(superVipInfo.getLoginDays() + 1);
			if (loginScore < ConstProperty.getInstance().getLoginMaximum()) {
				loginScore += ConstProperty.getInstance().getSuperVipDailyLoginScoreStep();
				loginScore = Math.min(loginScore, ConstProperty.getInstance().getLoginMaximum());
			}
		} else {
			superVipInfo.setLoginDays(1);
			loginScore = ConstProperty.getInstance().getSuperVipDailyLoginScore();
		}
		
		superVipInfo.setLastLoginScore(loginScore);
		
		updateSuperVipInfo();
		
		return true;
	}
	
	/**
	 * 检查离线期间错过的至尊vip每日礼包，以邮件的形式给玩家补发
	 */
	public void checkMissedSuperVipDailyGift() {
		checkMissedSuperVipDailyGift(0, 0);
	}
	
	/**
	 * 检查离线期间错过的至尊vip每日礼包，以邮件的形式给玩家补发（上限控制：不管玩家多少天未登录，最多补发6封邮件）
	 */
	private void checkMissedSuperVipDailyGift(long startTime, long endTime) {
		if (!isSuperVipOpen()) {
			return;
		}
		
		if (superVipInfo == null) {
			return;
		}
		
		if (superVipInfo.getActivatedLevel() <= 0) {
			return;
		}
		
		if (superVipInfo.getDailyGiftRecieveTime() <= 0) {
			return;
		}
		
		long lastRecieveTime = superVipInfo.getDailyGiftRecieveTime();
		long activeEndTime = superVipInfo.getActiveEndTime();
		// 手动激活情况下，在有效时间范围内已经领取过了
		if (!isAutoActivated() && lastRecieveTime > activeEndTime) {
			return;
		}

		endTime = endTime > 0 ? endTime : HawkTime.getMillisecond();
		int between = HawkTime.getCrossDay(lastRecieveTime, endTime, 0);
		// 结算时间点的前一天已经领取过，就没必要补了
		if (between <= 1) {
			return;
		}

		try {
			superVipInfo.setDailyGiftRecieveTime(endTime - HawkTime.DAY_MILLI_SECONDS);
			updateSuperVipInfo();
			VipSuperCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipSuperCfg.class, superVipInfo.getActivatedLevel());
			between = Math.min(between, DAILY_GIFT_TOP_NUM);
			while (between > 1) {
				// 手动激活的情况下，最后一天不满24小时，不补
				if (!isAutoActivated() && !HawkTime.isCrossDay(lastRecieveTime, activeEndTime, 0)) {
					break;
				}
				
				// 晚于终止时间点之后的，不补
				if (lastRecieveTime >= endTime) {
					break;
				}
				
				// 早于起始时间点之前的，不补
				if (lastRecieveTime < startTime) {
					lastRecieveTime += HawkTime.DAY_MILLI_SECONDS;
					continue;
				}
				
				MailParames.Builder mailParames = MailParames.newBuilder()
						.setMailId(MailId.SUPER_VIP_DAILY_GIFT_REWARD)
						.setPlayerId(player.getId())
						.setRewards(vipCfg.getVipBenefitItems())
						.setAwardStatus(MailRewardStatus.NOT_GET);
				SystemMailService.getInstance().sendMail(mailParames.build());
				HawkLog.logPrintln("super vip daily gift reissue, playerId: {}, super vipLevel: {}, day: {}, lastTime: {}", player.getId(), vipCfg.getLevel(), between, lastRecieveTime);
				lastRecieveTime += HawkTime.DAY_MILLI_SECONDS;
				between--;
			}
			
			HawkLog.logPrintln("super vip daily gift reissue, playerId: {}, super vipLevel: {}, startTime: {}, endTime: {}", player.getId(), vipCfg.getLevel(), HawkTime.formatTime(startTime), HawkTime.formatTime(endTime));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 更新至尊vip信息
	 */
	public void updateSuperVipInfo() {
		try {
			RedisProxy.getInstance().updateSuperVipInfo(superVipInfo);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 同步至尊会员信息
	 */
	public void syncSuperVipInfo(boolean login) {
		if (!isSuperVipOpen()) {
			return;
		}
		
		long timeNow = HawkTime.getMillisecond();
		if (login) {
			loginRefresh(timeNow);
		}
		
		SuperVipInfo.Builder builder = toBuilder(timeNow, false);
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.SUPER_VIP_INFO_SYNC, builder));
	}
	
	/**
	 * 登录刷新
	 * 
	 * @param timeNow
	 */
	private void loginRefresh(long timeNow) {
		initSuperVipInfo();
		
		// 判断至尊vip激活周期跨自然月， 或跨天
		if (checkSuperVipCrossMonth()) {
			
			// hotfix -- 2023年元月份登录才需要的处理
			if (superVipInfo.getActiveEndTime() >= 0 && superVipInfo.getActivatedPeriodMonth() == 202301) {
				int vipLevel = superVipInfo.getActualLevel();
				VipSuperCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipSuperCfg.class, vipLevel);
				superVipInfo.setActivatedLevel(vipCfg.getLevel());
				LogUtil.logSuperVipActive(player, vipCfg.getActivatePoints(), vipCfg.getLevel(), 2, 0, -1);
				superVipInfo.setActiveEndTime(-1);
				updateSuperVipInfo();
				HawkLog.logPrintln("superVip active period cross month fix on login, playerId: {}, super vipLevel: {}, activatedLevel: {}", player.getId(), superVipInfo.getActualLevel(), superVipInfo.getActivatedLevel());
			}
			
			HawkLog.logPrintln("super vip login cross month, playerId: {}", player.getId());
			return;
		}
		
		// hotfix -- 2023年元月份登录才需要的处理
		if (superVipInfo.getActiveEndTime() >= 0 && superVipInfo.getActivatedPeriodMonth() == 202301) {
			int vipLevel = superVipInfo.getActualLevel();
			VipSuperCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipSuperCfg.class, vipLevel);
			superVipInfo.setActivatedLevel(vipCfg.getLevel());
			LogUtil.logSuperVipActive(player, vipCfg.getActivatePoints(), vipCfg.getLevel(), 2, 0, -1);
			superVipInfo.setActiveEndTime(-1);
			updateSuperVipInfo();
			HawkLog.logPrintln("superVip active period cross month fix on login, playerId: {}, super vipLevel: {}, activatedLevel: {}", player.getId(), superVipInfo.getActualLevel(), superVipInfo.getActivatedLevel());
		}
		
		if (checkCrossDay(timeNow)) {
			HawkLog.logPrintln("super vip login cross day, playerId: {}", player.getId());
		}
	}
	
	private SuperVipInfo.Builder toBuilder(long timeNow, boolean levelup) {
		SuperVipInfo.Builder builder = SuperVipInfo.newBuilder();
		builder.setActualLevel(superVipInfo.getActualLevel());
		builder.setActivated(isSuperVipActivated());
		builder.setMonthVipScore(superVipInfo.getMonthVipScore());
		builder.setDailyActiveScoreGot(!HawkTime.isCrossDay(timeNow, superVipInfo.getDailyActiveRecieveTime(), 0));
		builder.setDailyLoginScoreGot(!HawkTime.isCrossDay(timeNow, superVipInfo.getDailyLoginRecieveTime(), 0));
		builder.setDailyGiftGot(!HawkTime.isCrossDay(timeNow, superVipInfo.getDailyGiftRecieveTime(), 0));
		builder.setMonthGiftGot(!superVipInfo.getMonthGiftRecieved().isEmpty());
		builder.setLoginDays(superVipInfo.getLoginDays());
		
		int loginScore = getLoginScore(timeNow);
		builder.setLoginScore(loginScore);
		
		if (loginScore < ConstProperty.getInstance().getLoginMaximum()) {
			loginScore += ConstProperty.getInstance().getSuperVipDailyLoginScoreStep();
			loginScore = Math.min(loginScore, ConstProperty.getInstance().getLoginMaximum());
		}
		builder.setNextLoginScore(loginScore);
		builder.setActivatedSkinLevel(superVipInfo.getSkinEffActivated());
		builder.setLevelup(levelup);
		builder.setAutoActive(isAutoActivated());
		builder.setActiveEndTime(superVipInfo.getActiveEndTime());
		return builder;
	}
	
}

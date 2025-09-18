package com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.MailConst.MailId;
@HawkConfigManager.XmlResource(file = "activity/dw_gift/dw_gift_drop.xml")
public class DragonBoatCelebrationDropCfg extends HawkConfigBase {
	/**
	 * 操作ID
	 */
	private final int id;

	/**
	 * 收集项名称
	 */
	private final String name;

	/**
	 * 掉落参数
	 */
	private final int dropParam;

	/**
	 * 掉落限制
	 */
	private final int dropLimit;

	/**
	 * 掉落ID
	 */
	private final int dropId;

	/**
	 * 邮件ID
	 */
	private final int mailId;

	/**
	 * 收集类型
	 */
	@Id
	private final int funcType;

	public DragonBoatCelebrationDropCfg() {
		this.id = 0;
		this.name = "";
		this.dropParam = 0;
		this.dropLimit = 0;
		this.dropId = 0;
		this.mailId = 0;
		this.funcType = 0;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getDropId() {
		return dropId;
	}

	public int getDropParam() {
		return dropParam;
	}

	public int getDropLimit() {
		return dropLimit;
	}

	public int getMailId() {
		return mailId;
	}

	public int getFuncType() {
		return funcType;
	}

	@Override
	public boolean checkValid() {
		MailId enumMailId = MailConst.MailId.valueOf(mailId);
		if (enumMailId == null) {
			HawkLog.errPrintln("activityItemCollect mailId error cfgId: {}, mailId: {}", id, mailId);

			return false;
		}

		Activity.BrokenExchangeOper oper = Activity.BrokenExchangeOper.valueOf(funcType);

		if (oper == null) {
			HawkLog.errPrintln("activityItemCollect oper error cfgId: {}, funcType: {}", id, funcType);

			return false;
		}

		return true;
	}
}

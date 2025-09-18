package com.hawk.game.config;

import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.MailConst.MailId;

/**
 * 攻防模拟战
 * 
 * @author jm
 *
 */
@HawkConfigManager.XmlResource(file = "xml/simulate_war_award.xml")
public class SimulateWarRewarCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 胜利场次
	 */
	private final int winCount;
	/**
	 * 奖励
	 */
	private final String award;
	/**
	 * 奖励列表
	 */
	private List<ItemInfo> awardList;
	/**
	 * 邮件ID
	 */
	private final int mailsysId;
	
	/**
	 * 奖励邮件ID
	 */
	private MailId  rewardMailId;
	
	public SimulateWarRewarCfg() {
		this.id = 0;
		this.winCount = 0;
		this.award = "";
		this.mailsysId = 0;
	}
	/**
	 * 
	 */
	public boolean assemble() {		
		awardList = Collections.unmodifiableList(ItemInfo.valueListOf(award));
		this.rewardMailId = MailId.valueOf(mailsysId);
		
		return true;
	}

	public int getWinCount() {
		return winCount;
	}

	public List<ItemInfo> getAwardList() {
		return awardList;
	}
	public MailId getRewardMailId() {
		return rewardMailId;
	}
	public int getId() {
		return id;
	}
}

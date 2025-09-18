package com.hawk.activity.type.impl.honourMobilize.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.gamelib.activity.ConfigChecker;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/honour_mobilize/%s/honour_mobilize_reward.xml", autoLoad=false, loadParams="366")
public class HonourMobilizeRewardCfg extends HawkConfigBase{

	@Id
	private final int id;

	private final int heroId;
	
	private final int award;
	
	private final String achieveId;
	

	public HonourMobilizeRewardCfg() {
		this.id = 0;
		this.heroId = 0;
		this.award = 0;
		achieveId = "";
	}
	
	public int getId() {
		return id;
	}

	public int getHeroId() {
		return heroId;
	}
	
	public int getAward() {
		return award;
	}
	
	
	public List<Integer> getAchieveId() {
		return SerializeHelper.stringToList(Integer.class, 
				this.achieveId, SerializeHelper.BETWEEN_ITEMS);
	}
	
	@Override
	protected boolean assemble() {
		return super.assemble();
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().chectAwardIdValid(award);
		if (!valid) {
			throw new InvalidParameterException(String.format("HonourMobilizeRewardCfg reward error, id: %s , award: %s", id, award));
		}
		return super.checkValid();
	}

	
}

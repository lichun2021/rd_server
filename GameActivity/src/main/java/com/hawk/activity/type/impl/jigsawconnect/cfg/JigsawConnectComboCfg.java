package com.hawk.activity.type.impl.jigsawconnect.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import java.util.List;


/**
 * 双十一拼图活动活动积分奖励配置
 * @author hf
 *
 */
@HawkConfigManager.XmlResource(file = "activity/ssy_jigsaw_connect/ssy_jigsaw_connect_combo.xml")
public class JigsawConnectComboCfg extends HawkConfigBase {
	/** */
	@Id
	private final int connectId;
	
	/** 连线的四个成就Id */
	private final String achieveConnect;
	

	private List<Integer> achieveConnectList;
	
	public JigsawConnectComboCfg() {
		connectId = 0;
		achieveConnect = "";
	}
	
	@Override
	protected boolean assemble() {
		try {
			achieveConnectList = SerializeHelper.stringToList(Integer.class, achieveConnect, SerializeHelper.ATTRIBUTE_SPLIT);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	public int getConnectId() {
		return connectId;
	}

	public String getAchieveConnect() {
		return achieveConnect;
	}

	public List<Integer> getAchieveConnectList() {
		return achieveConnectList;
	}

	public void setAchieveConnectList(List<Integer> achieveConnectList) {
		this.achieveConnectList = achieveConnectList;
	}
}

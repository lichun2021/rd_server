package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.GsConfig;
import com.hawk.game.util.GameUtil;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 系统基础配置
 *
 * @author hawk
 *
 */
@HawkConfigManager.KVResource(file = "xml/xzq_const.xml")
public class XZQConstCfg extends HawkConfigBase {
	// 开服前多少天使用时间配置 xzq_openserver_time.xml
	private final int openTimeDays;
	//小站区开放时间
	private final String xzqOpenTime;
	private final String xzqCloseTime;
	// 正赛占领需要时间
	private final int battleControlTime;

	
	private final int alliancePointUpperLimitBase;
	
	private final int noPointAllianceSignUpLevel;

	private final int allianceColorNeedPointNum;

	
	private final int attackAchiveticketNum;
	
	private final String allianceColors;
	
	private final String xzqLimitServer;
	private final String xzqLimitGuildScience;
	private final int colorResetTime;
	private final String xzqFixTime;
	
	private long xzqOpenTimeValue;
	private long xzqCloseTimeValue;
	private List<String> xzqLimitServerList;
	private List<Integer> xzqLimitGuildScienceList;
	private List<String> allianceColorList;
	private long xzqFixTimeValue;
	
	private final boolean systemOpenFlag;
	/**
	 * 全局静态对象
	 */
	private static XZQConstCfg instance = null;

	/**
	 * 获取全局静态对象
	 *
	 * @return
	 */
	public static XZQConstCfg getInstance() {
		return instance;
	}

	public XZQConstCfg() {
		instance = this;
		openTimeDays = 0;
		xzqOpenTime = "";
		battleControlTime = 0;
		alliancePointUpperLimitBase = 0;
		noPointAllianceSignUpLevel = 0;
		allianceColorNeedPointNum = 0;
		attackAchiveticketNum = 1;
		xzqLimitServer  = "";
		xzqLimitGuildScience = "";
		allianceColors = "";
		colorResetTime = 0;
		xzqFixTime= "";
		xzqCloseTime = "";
		systemOpenFlag= false;
	}

	@Override
	protected boolean assemble() {
		xzqOpenTimeValue = HawkTime.parseTime(xzqOpenTime);
		if(HawkOSOperator.isEmptyString(xzqCloseTime)){
			xzqCloseTimeValue = Long.MAX_VALUE;
		}else{
			xzqCloseTimeValue = HawkTime.parseTime(xzqCloseTime);
		}
		xzqLimitServerList = SerializeHelper.stringToList(String.class, xzqLimitServer, SerializeHelper.BETWEEN_ITEMS);
		xzqLimitGuildScienceList = SerializeHelper.stringToList(Integer.class, xzqLimitGuildScience, SerializeHelper.BETWEEN_ITEMS);
		allianceColorList = SerializeHelper.stringToList(String.class, allianceColors, SerializeHelper.ATTRIBUTE_SPLIT);
		if(!HawkOSOperator.isEmptyString(xzqFixTime)){
			xzqFixTimeValue = HawkTime.parseTime(xzqFixTime);
		}
		return true;
	}

	public int getOpenTimeDays() {
		return openTimeDays;
	}




	public int getBattleControlTime() {
		return battleControlTime;
	}

	public int getAlliancePointUpperLimitBase() {
		return alliancePointUpperLimitBase;
	}

	public int getNoPointAllianceSignUpLevel() {
		return noPointAllianceSignUpLevel;
	}

	public int getAllianceColorNeedPointNum() {
		return allianceColorNeedPointNum;
	}

	public int getAttackAchiveticketNum() {
		return attackAchiveticketNum;
	}


	public long getXzqOpenTimeValue() {
		return xzqOpenTimeValue;
	}

	
	
	public long getXzqCloseTimeValue() {
		return xzqCloseTimeValue;
	}


	public List<String> getAllianceColorList() {
		return allianceColorList;
	}
	
	

	public int getColorResetTime() {
		return colorResetTime;
	}
	
	

	public long getXzqFixTimeValue() {
		return xzqFixTimeValue;
	}

	public boolean isOpen(){
		//systemOpenFlag 字段强行关闭了此功能，后续开放请酌情处理相关世界点信息
		if(!this.systemOpenFlag){
			return false;
		}
		long serverOpenTime = GameUtil.getServerOpenTime();
		if(serverOpenTime > this.xzqOpenTimeValue && 
				serverOpenTime < this.xzqCloseTimeValue){
			return true;
		}
		if(serverOpenTime > this.xzqCloseTimeValue){
			return false;
		}
		if(this.xzqLimitServerList == null){
			return true;
		}
		if(this.xzqLimitServerList.isEmpty()){
			return true;
		}
		String serverId = GsConfig.getInstance().getServerId();
		if(this.xzqLimitServerList.contains(serverId)){
			return true;
		}
		return false;
	}
	
	public int getXZQSignupScience(){
		if(this.xzqLimitGuildScienceList == null){
			return 0;
		}
		if(this.xzqLimitGuildScienceList.isEmpty()){
			return 0;
		}
		return this.xzqLimitGuildScienceList.get(0);
	}
	public boolean isLimitGuildScience(int sid){
		if(this.isOpen()){
			return false;
		}
		if(this.xzqLimitGuildScienceList == null){
			return false;
		}
		if(this.xzqLimitGuildScienceList.isEmpty()){
			return false;
		}
		if(!this.xzqLimitGuildScienceList.contains(sid)){
			return false;
		}
		return true;
	}

}

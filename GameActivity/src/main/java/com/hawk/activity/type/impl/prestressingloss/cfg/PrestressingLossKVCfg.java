package com.hawk.activity.type.impl.prestressingloss.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;

@HawkConfigManager.KVResource(file = "activity/loss/loss_activity.xml")
public class PrestressingLossKVCfg extends HawkConfigBase {

	/** 
	 * 服务器开服延时开启活动时间 单位:s 
	 */
	private final int serverDelay;
	
	/**
	 * 注册时间
	 */
	private final int registrationTime;
	/**
	 * 冷却期
	 */
	private final int coolingTime;
	private final int coolingTimeFloat;
	/**
	 * 空置期
	 */
	private final int vacancyPeriod;
	private final int vacancyPeriodFloat;
	/**
	 * 一期活动轮回时间
	 */
	private final int lossTime;
	/**
	 * 请求tx接口的地址
	 */
	private final String lossDataAddr;
	/**
	 * l5参数
	 */
	private final String dataAddrL5;
	/**
	 * 测试数据
	 */
	private final String testData;

	/**
	 * l5的分解参数
	 */
	private int l5_modId = 0;
	private int l5_cmdId = 0;
	
	private boolean l5Req = false;
	private String subAttr;
	private long registerTimeVal;

	public PrestressingLossKVCfg() {
		serverDelay = 0;
		registrationTime = 0;
		coolingTime = 0;
		coolingTimeFloat = 0;
		vacancyPeriod = 0;
		vacancyPeriodFloat = 0;
		lossTime = 0;
		lossDataAddr = "";
		testData = "";
		dataAddrL5 = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public long getRegisterTime() {
		return registerTimeVal;
	}

	public int getCoolTime() {
		return coolingTime;
	}

	public int getVacancyTime() {
		return vacancyPeriod;
	}

	public long getCircleTime() {
		return lossTime * 1000l;
	}

	public int getVacancyPeriodFloat() {
		return vacancyPeriodFloat;
	}

	public int getCoolingTimeFloat() {
		return coolingTimeFloat;
	}

	public String getAddr() {
		return lossDataAddr;
	}
	
	public String getSubAttr() {
		return subAttr;
	}
	
	public String getTestData() {
		return testData;
	}

	public int getL5_modId() {
		return l5_modId;
	}

	public int getL5_cmdId() {
		return l5_cmdId;
	}
	
	public boolean isL5() {
		return l5Req;
	}
	
	public boolean assemble() {
		if (!HawkOSOperator.isEmptyString(dataAddrL5)) {
			String infos[] = dataAddrL5.split(":");
			this.l5_modId = Integer.valueOf(infos[0]);
			this.l5_cmdId = Integer.valueOf(infos[1]);
			l5Req = true;
			
			String originString = lossDataAddr;
			if (originString.startsWith("https")) {
				originString = originString.replaceFirst("https://", "");
			} else {
				originString = originString.replaceFirst("http://", "");
			}
			int index = originString.indexOf("/");
			subAttr = originString.substring(index + 1, originString.length());
			HawkLog.logPrintln("PrestressingLossKVCfg config, l5_modId: {}, l5_cmdId: {}, subAttr: {}", l5_modId, l5_cmdId, subAttr);
		}
		
		int time = registrationTime <= 0 ? 5 : registrationTime;
		registerTimeVal = time * 1000l;
		
		return true;
	}

}

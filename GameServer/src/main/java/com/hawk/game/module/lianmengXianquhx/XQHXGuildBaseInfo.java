package com.hawk.game.module.lianmengXianquhx;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.module.lianmengXianquhx.XQHXRoomManager.XQHX_CAMP;
import com.hawk.game.module.lianmengXianquhx.cfg.XQHXOrderBuffCfg;
import com.hawk.game.protocol.Const.EffType;

public class XQHXGuildBaseInfo {
	public XQHX_CAMP camp;
	public XQHXBornPointRing bornPointList;
	public String campGuild = "";
	public String campGuildName = "";
	public String campGuildTag = "";
	public String campServerId = "";
	public int campguildFlag;
	public int campGuildWarCount;
	public int campNuclearSendCount;
	/**A击杀机甲数*/
	public int campNianKillCount;
	/**首站积分*/
	public int firstControlHonor;
	public int campHonor;
	/** 号令点数*/
	public int campOrder;
	
	public double buildControlHonor; //控制建筑得分
	public double monsterHonor; //能量塔得分
	public double pylonHonor; //消灭野怪得分

	private XQHXOrderBuffCfg orderBuff;
	/** 建筑加*/
	public ImmutableMap<EffType, Integer> battleEffVal = ImmutableMap.of();
	public void tick() {
		ConfigIterator<XQHXOrderBuffCfg> ocfit = HawkConfigManager.getInstance().getConfigIterator(XQHXOrderBuffCfg.class);
		for (XQHXOrderBuffCfg cfg : ocfit) {
			if (campOrder >= cfg.getOrder()) {
				if (orderBuff == null || orderBuff.getId() < cfg.getId()) {
					orderBuff = cfg;
				}
			}
		}
	}

	public int getOrderEffVal(EffType eff) {
		if (orderBuff == null) {
			return 0;
		}
		return orderBuff.getEffVal(eff);

	}
}

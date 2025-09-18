package com.hawk.game.lianmengxzq.timecontroller;

import java.util.Objects;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.game.GsConfig;
import com.hawk.game.config.XZQConstCfg;
import com.hawk.game.config.XZQTimeCfg;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.util.GameUtil;

public class XZQTimeController extends IXZQController{
	private static XZQTimeController instance;

	public static XZQTimeController getInstance() {
		if (Objects.isNull(instance)) {
			synchronized (XZQTimeController.class) {
				instance = new XZQTimeController();
			}
		}
		return instance;
	}
	
	public void init(){
		
	}

	@Override
	protected Optional<IXZQTimeCfg> getTimeCfg(long now) {
		long xzqOpenTime = XZQConstCfg.getInstance().getXzqOpenTimeValue();
		long serverOpenTime = GameUtil.getServerOpenTime();
		boolean newServer = xzqOpenTime <= serverOpenTime;
		long serverOpenAM0 = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		int serverOpenDays = XZQConstCfg.getInstance().getOpenTimeDays();
		long timeLimit = serverOpenAM0 + serverOpenDays * HawkTime.DAY_MILLI_SECONDS;
		String serverId = GsConfig.getInstance().getServerId();
		Long serverMergeTime = AssembleDataManager.getInstance().getServerMergeTime(serverId);
		ConfigIterator<XZQTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(XZQTimeCfg.class);
		for (XZQTimeCfg timeCfg : it) {
			if (now >= timeCfg.getSignupTimeValue() && now < timeCfg.getEndTimeValue()) {
				// 延迟显示时间节点超出活动展示时间,则本期活动不参与
				if (newServer && timeLimit > timeCfg.getSignupTimeValue()) {
					return Optional.empty();
				}
				// 开放时间超出展示时间，则本期活动不参与
				if(xzqOpenTime > timeCfg.getSignupTimeValue()){
					return Optional.empty();
				}
				//如果合服处于活动中，则本期不参与
				if(serverMergeTime!= null && serverMergeTime > timeCfg.getSignupTimeValue()
						&&serverMergeTime < timeCfg.getEndTimeValue()){
					return Optional.empty();
				}
				return Optional.of(timeCfg);
			}
		}
		return Optional.empty();
	}

			
}

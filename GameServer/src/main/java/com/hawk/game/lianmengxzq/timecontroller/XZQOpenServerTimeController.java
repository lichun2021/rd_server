package com.hawk.game.lianmengxzq.timecontroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.game.config.XZQConstCfg;
import com.hawk.game.config.XZQOpenServerTimeCfg;
import com.hawk.game.protocol.XZQ.PBXZQTimeInfo;
import com.hawk.game.util.GameUtil;

public class XZQOpenServerTimeController extends IXZQController{

	private static XZQOpenServerTimeController instance;

	public static XZQOpenServerTimeController getInstance() {
		if (Objects.isNull(instance)) {
			synchronized (XZQOpenServerTimeController.class) {
				instance = new XZQOpenServerTimeController();
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
		if(serverOpenTime < xzqOpenTime){
			return Optional.empty();
		}
		ConfigIterator<XZQOpenServerTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(XZQOpenServerTimeCfg.class);
		for (XZQOpenServerTimeCfg timeCfg : it) {
			if (now >= timeCfg.getSignupTimeValue() && now < timeCfg.getEndTimeValue()) {
				return Optional.of(timeCfg);
			}
		}
		return Optional.empty();
	}
	
	
	
	public List<PBXZQTimeInfo> genPBXZQTimeInfoList(){
		List<PBXZQTimeInfo> list = new ArrayList<>();
		List<XZQOpenServerTimeCfg> cfgList =  HawkConfigManager.getInstance()
				.getConfigIterator(XZQOpenServerTimeCfg.class).toList();
		for(XZQOpenServerTimeCfg cfg : cfgList){
			list.add(cfg.genPBXZQTimeInfoBuilder().build());
		}
		return list;
	}
	
	
}

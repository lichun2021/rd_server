package com.hawk.game.module.lianmengyqzz.march.service.state;

import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZActivityStateData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityJoinState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZActivityState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;

public class YQZZState000Hidden  extends IYQZZServiceState {

	public YQZZState000Hidden(YQZZMatchService parent) {
		super(parent);
	}

	@Override
	public void init() {
		this.getDataManager().getStateData().setState(YQZZActivityState.HIDDEN);
		this.getDataManager().getStateData().setJoinGame(YQZZActivityJoinState.OUT);
		this.getDataManager().getStateData().setSaveServerInfo(0);
		this.getDataManager().getStateData().saveRedis();
		//清除数据
		this.getDataManager().clearData();
	}
	@Override
	public void tick() {
		YQZZActivityStateData curData = this.calcInfo();
		//如果不在当前状态，则往下个状态推进
		if(curData.getTermId() != 0 && this.getDataManager().getStateData().getState() 
				!= curData.getState()){
			this.getParent().updateState(YQZZActivityState.START_SHOW);
			return;
		}
	}

}

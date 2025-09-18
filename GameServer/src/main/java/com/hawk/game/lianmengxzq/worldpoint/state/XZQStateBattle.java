package com.hawk.game.lianmengxzq.worldpoint.state;

import java.util.Objects;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.GsApp;
import com.hawk.game.config.XZQConstCfg;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.lianmengxzq.XZQTlog;
import com.hawk.game.lianmengxzq.worldpoint.XZQWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.world.WorldMarchService;

/** 正赛
 *  代码有些重复,但是暂不提取.将来可以不同场会有不同的推送什么的.
 */
public class XZQStateBattle extends IXZQPointState {

	private long occupyTime; // 占领开始时间
	private String occupyGuild; // 占领盟

	public XZQStateBattle(XZQWorldPoint parent) {
		super(parent);
		
	}

	@Override
	public void init() {
		
	}

	@Override
	public void ontick() {
		final long currTime = GsApp.getInstance().getCurrentTime();
		Player occupyLeader = WorldMarchService.getInstance().getXZQLeader(getParent().getId());
		if (Objects.isNull(occupyLeader)) {
			if(!HawkOSOperator.isEmptyString(this.occupyGuild) || this.occupyTime > 0){
				String lastOccupyGuild = this.occupyGuild;
				long lastOccupyTime = this.occupyTime;
				occupyGuild = null;
				occupyTime = 0;
				this.getParent().updateWorldScene();
				int termId = XZQService.getInstance().getXZQTermId();
				int controlTime = (int) ((currTime - lastOccupyTime)/1000);
				XZQTlog.XZQSControlTime(termId, lastOccupyGuild, this.getParent().getXzqCfg().getId(), controlTime);
			}
			
		} else {
			if (!Objects.equals(occupyGuild, occupyLeader.getGuildId())) {
				String lastOccupyGuild = this.occupyGuild;
				long lastOccupyTime = this.occupyTime;
				occupyGuild = occupyLeader.getGuildId();
				occupyTime = currTime;
				getParent().updateWorldScene();
				//Tlog
				int termId = XZQService.getInstance().getXZQTermId();
				XZQTlog.XZQRecordOccupy(occupyLeader, termId, occupyLeader.getGuildId(), this.getParent().getXzqCfg().getId());
				if(!HawkOSOperator.isEmptyString(lastOccupyGuild) && lastOccupyTime >0){
					int controlTime = (int) ((currTime - lastOccupyTime)/1000);
					XZQTlog.XZQSControlTime(termId, lastOccupyGuild, this.getParent().getXzqCfg().getId(), controlTime);
				}
			}
			XZQConstCfg constCfg = XZQConstCfg.getInstance();
			long occupyEnd = occupyTime + constCfg.getBattleControlTime() * 1000L;
			String controGuild = this.getParent().getGuildControl();
			if (currTime > occupyEnd && !Objects.equals(controGuild, occupyGuild)) {
				int termId = XZQService.getInstance().getXZQTermId();
				getParent().xzqWinOver(currTime,termId);
				getParent().updateWorldScene();
				//控制建筑TLog
				int controlTime = (int) ((currTime - this.occupyTime)/1000);
				XZQTlog.XZQSControlTime(termId, this.occupyGuild, this.getParent().getXzqCfg().getId(), controlTime);
				int count1 = XZQService.getInstance().getControlList(occupyGuild).size();
				XZQTlog.XZQControl(occupyLeader, termId, occupyLeader.getGuildId(), this.getParent().getXzqCfg().getId(),1,count1);
				if(!HawkOSOperator.isEmptyString(controGuild)){
					int count2 = XZQService.getInstance().getControlList(controGuild).size();
					XZQTlog.XZQControl(occupyLeader, termId, occupyLeader.getGuildId(), this.getParent().getXzqCfg().getId(),0,count2);
				}
			}

		}
		
	}
	
	@Override
	public String getOccupyGuild() {
		return this.occupyGuild;
	}

	@Override
	public String serializ() {
		return null;
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		
	}


	public long getControlStartTime() {
		return occupyTime;
	}
	
	@Override
	public long getControlEndTime() {
		if(this.occupyTime > 0){
			return XZQConstCfg.getInstance().getBattleControlTime() * 1000 + this.occupyTime;
		}
		return super.getControlEndTime();
	}

	
}

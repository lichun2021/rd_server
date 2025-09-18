package com.hawk.activity.type.impl.plantFortress.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.gamelib.GameConst;

/**
 * 时空好礼时空之门直购礼包道具
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/tiberium_fortress/tiberium_fortress_comm_reward.xml")
public class PlantFortressCommReward extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	
	private final int type;
	
	private final String rewards;
	
	
	public PlantFortressCommReward() {
		id = 0;
		type = 0;
		rewards = "";
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public String getRewards() {
		return rewards;
	}

	
	
	public List<Integer> getExtendIds(int id,int xMax,int yMax){
		int x = id / GameConst.RANDOM_MYRIABIT_BASE;
		int y = id % GameConst.RANDOM_MYRIABIT_BASE;
		List<Integer> ids = new ArrayList<Integer>();
		//横向
		if(getType() ==  2){
			for(int i=1;i<=xMax;i++){
				int addId = i *  GameConst.RANDOM_MYRIABIT_BASE + y;
				ids.add(addId);
			}
		}else if(getType() ==  3){
		//纵向
			for(int i=1;i<=yMax;i++){
				int addId = x *  GameConst.RANDOM_MYRIABIT_BASE + i;
				ids.add(addId);
			}
		}else if(getType() ==  4){
		//周围一圈
			//x+1 y
			if(x+1 <= xMax){
				int addId = (x+1) *  GameConst.RANDOM_MYRIABIT_BASE + y;
				ids.add(addId);
			}
			//x-1 y
			if(x-1 >= 1){
				int addId = (x-1) *  GameConst.RANDOM_MYRIABIT_BASE + y;
				ids.add(addId);
			}
			// x   y+1
			if(y+1 <= yMax){
				int addId = x *  GameConst.RANDOM_MYRIABIT_BASE + (y+1);
				ids.add(addId);
			}
			// x   y-1
			if(y-1 >= 1){
				int addId = x *  GameConst.RANDOM_MYRIABIT_BASE + (y-1);
				ids.add(addId);
			}
			//x+1   y+1
			if((x+1)<= xMax && (y+1) <= yMax){
				int addId = (x+1) *  GameConst.RANDOM_MYRIABIT_BASE +(y+1);
				ids.add(addId);
			}
			//x+1   y-1
			if((x+1)<= xMax && (y-1) >= 1){
				int addId = (x+1) *  GameConst.RANDOM_MYRIABIT_BASE +(y-1);
				ids.add(addId);
			}
			//x-1   y+1
			if((x-1) >= 1 && (y+1) <= yMax){
				int addId = (x-1) *  GameConst.RANDOM_MYRIABIT_BASE +(y+1);
				ids.add(addId);
			}
			//x-1   y-1
			if((x-1) >= 1 && (y-1) >= 1){
				int addId = (x-1) *  GameConst.RANDOM_MYRIABIT_BASE +(y-1) ;
				ids.add(addId);
			}
		}
		return ids;
	}
	

}

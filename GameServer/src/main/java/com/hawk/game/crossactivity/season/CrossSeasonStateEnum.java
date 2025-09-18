package com.hawk.game.crossactivity.season;

import org.hawk.os.HawkException;

import com.hawk.game.crossactivity.season.state.CrossSeasonStateEnd;
import com.hawk.game.crossactivity.season.state.CrossSeasonStateEndReward;
import com.hawk.game.crossactivity.season.state.CrossSeasonStateHidden;
import com.hawk.game.crossactivity.season.state.CrossSeasonStateOpen;
import com.hawk.game.crossactivity.season.state.CrossSeasonStateShow;
import com.hawk.game.crossactivity.season.state.ICrossSeasonState;

public enum CrossSeasonStateEnum {
    HIDDEN(100,CrossSeasonStateHidden.class),
    SHOW(200,CrossSeasonStateShow.class),
    OPEN(300,CrossSeasonStateOpen.class),
    END(400,CrossSeasonStateEnd.class),
	REWARD(500,CrossSeasonStateEndReward.class);

    private int stateNum;
    private Class<? extends ICrossSeasonState> stateCls;

    CrossSeasonStateEnum(int index,Class<? extends ICrossSeasonState> stateCls){
        this.stateNum = index;
        this.stateCls = stateCls;
    }
   
    public int getNum() {
        return stateNum;
    }
    
    
    public ICrossSeasonState createSeasonState(){
    	try {
			return this.stateCls.newInstance();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
    	return null;
    }


    public static CrossSeasonStateEnum seasonStateOf(int num){
        switch (num){
            case 100:return HIDDEN;
            case 200:return SHOW;
            case 300:return OPEN;
            case 400:return END;
            case 500:return REWARD;
            default:return HIDDEN;
        }
    }

   
}

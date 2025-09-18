package com.hawk.game.msg.starwars;
import org.hawk.msg.HawkMsg;
import com.hawk.gamelib.GameConst;

public class StarWarsEnterCrossInstanceMsg extends HawkMsg {
			public StarWarsEnterCrossInstanceMsg() {
				super(GameConst.MsgId.STAR_WARS_ENTER_INSTANCE);
			}
}

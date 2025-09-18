package com.hawk.game.module;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.StrengthenGuide.StrengthenGuideInfoRes;
import com.hawk.game.strengthenguide.op.SGPlayerEntityOP;

public class PlayerStrengthenGuideModule extends PlayerModule {
	public PlayerStrengthenGuideModule(Player player) {
		super(player);
	}

	/**
	 * 我要变强数据获取
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.STRENGTHEN_GUIDE_INFO_REQ_C_VALUE)
	public void onProtocolMessage(HawkProtocol protocol) {
		HawkTaskManager.getInstance().postExtraTask(new HawkTask(){
			@Override
			public Object run() {
				try {
					StrengthenGuideInfoRes.Builder builder = SGPlayerEntityOP.getPlayerStrengthenGuideInfoResp(player);
					player.sendProtocol(HawkProtocol.valueOf(HP.code.STRENGTHEN_GUIDE_INFO_RES_S_VALUE, builder));	
				} catch (Exception e) {
					// 做个保险 虽然按照逻辑不可能发生
					HawkException.catchException(e,
							" STRENGTHEN_GUIDE_INFO_REQ_C_VALUE request scores while player not login!!");
				}
				return null;
			}
		}, 0);
	}
}

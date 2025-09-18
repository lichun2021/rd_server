package com.hawk.game.invoker;

import com.hawk.game.module.autologic.PlayerAutoModule;
import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import com.hawk.game.item.ConsumeItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;

import static com.hawk.game.util.GsConst.ModuleType.AUTO_GATHER;

public class AutoPutModuleInvoker extends HawkMsgInvoker {
	
	private Player player;
	/**
	 * 	执行何种操作
	 * 	AUTO_PUT_FAIL = 1;
	 * 	AUTO_PUT_ALREADY = 2;
	 * 	AUTO_PUT_SET_FIRST = 3;
	 */
	private int op;

	public AutoPutModuleInvoker(Player player, int op) {
		this.player = player;
		this.op = op;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		PlayerAutoModule module = this.player.getModule(AUTO_GATHER);
		if(op == PlayerAutoModule.AUTO_PUT_FAIL){
			if(module.isFirstPut()){
				//发送错误码停自动
				module.stopPutAndSendErrCode();
			}
			else{//发邮件，停自动
				module.stopPutAndSendMail();
			}
		}else if(op == PlayerAutoModule.AUTO_PUT_ALREADY){
			module.setFirstPut(false);
		}else if(op == PlayerAutoModule.AUTO_PUT_SET_FIRST){
			module.setFirstPut(true);
		}
		return true;
	}

	public Player getPlayer() {
		return player;
	}
}

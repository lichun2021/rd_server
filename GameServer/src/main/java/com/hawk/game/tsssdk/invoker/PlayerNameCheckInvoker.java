package com.hawk.game.tsssdk.invoker;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.module.PlayerOperationModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Login.LoginandRename;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LoginUtil;
import com.hawk.log.Action;

@Category(scene = GameMsgCategory.PLAYER_NAME_CHECK_LOGIN)
public class PlayerNameCheckInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String callback) {
		if (result != 0) {
			// 提示玩家名字包含敏感词
			String beforeName = player.getName();
			String playerName = LoginUtil.randomPlayerName(player.getId(), player.getPuid());
			PlayerOperationModule module = player.getModule(GsConst.ModuleType.OPERATION_MODULE);
			module.changeName(playerName, Action.PLAYER_CHANGE_NAME, 0);
			
			HawkLog.logPrintln("login check, player name contain sens, playerId: {}, beforeName: {}, afterName: {}", player.getId(), beforeName, playerName);
			boolean sync = Integer.parseInt(callback) > 0;
			// 创建玩家时检测名字不需要同步
			if (sync) {
				player.getPush().syncPlayerInfo();
				LoginandRename.Builder builder = LoginandRename.newBuilder();
				builder.setBeforeName(beforeName);
				builder.setAfterName(playerName);
				player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_NAME_CONTAIN_SENS_VALUE, builder));
				
				String key = "change_name_card_send:" + player.getId() + ":" + HawkTime.getYyyyMMddIntVal();
				String times = RedisProxy.getInstance().getRedisSession().getString(key);
				int limit = ConstProperty.getInstance().getLoginandRenameDailyTime();
				// 没有达到次数则发邮件
				if (HawkOSOperator.isEmptyString(times) || Integer.parseInt(times) < limit) {
					RedisProxy.getInstance().getRedisSession().increaseBy(key, 1, 86400);
					SystemMailService.getInstance().sendMail(MailParames.newBuilder()
							.setPlayerId(player.getId())
							.setMailId(MailId.LOGIN_CHANGE_NAME)
							.setAwardStatus(MailRewardStatus.NOT_GET)
							.setRewards(ConstProperty.getInstance().getLoginandRename())
							.build());
				}
			}
		}
		
		return 0;
	}

}

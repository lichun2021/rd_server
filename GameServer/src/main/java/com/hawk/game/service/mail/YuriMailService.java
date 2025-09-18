package com.hawk.game.service.mail;

import com.hawk.game.service.MailService;

/**
 * 尤里残部邮件服务类
 * 
 * @author Nannan.Gao
 * @date 2016-11-30 18:28:14
 */
public class YuriMailService extends MailService {

	private static final YuriMailService instance = new YuriMailService();
	
	public static YuriMailService getInstance() {

		return instance;
	}
	
	
}

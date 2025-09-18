package com.hawk.game.service.mail;

import com.hawk.game.service.MailService;

/**
 * 采集报告邮件服务类
 * 
 * @author Nannan.Gao
 * @date 2016-11-30 18:28:14
 */
public class CollectMailService extends MailService {

	private static final CollectMailService instance = new CollectMailService();

	public static CollectMailService getInstance() {

		return instance;
	}


}

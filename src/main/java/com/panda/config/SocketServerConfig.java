package com.panda.config;

import com.alibaba.fastjson.JSONObject;
import com.panda.utils.socket.server.SocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author
 * @date 2019-01-24 22:24
 */
@Configuration
@Slf4j
public class SocketServerConfig {

	@Bean
	public SocketServer socketServer() {

		SocketServer socketServer = new SocketServer(60000);
		socketServer.setLoginHandler(parkId -> {log.info("处理socket用户身份验证,parkId:{}", parkId);
			//用户名中包含了dingxu则允许登陆
			return parkId.contains("Parking-Dongyang");
		});
		socketServer.setMessageHandler((connection, receiveDto) -> log.info("处理socket消息,parkId:{},receiveDto:{}", connection.getParkId(),JSONObject.toJSONString(receiveDto)));
		socketServer.start();
		return socketServer;
	}




}

package com.panda.utils.socket.server;

import com.alibaba.fastjson.JSONObject;
import com.panda.utils.socket.dto.*;
import com.panda.utils.socket.enums.FunctionCodeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;

/**
 * 每一个client连接开一个线程
 *
 * @author camel
 */
@Slf4j
@Data
public class ConnectionThread extends Thread {

	/**
	 * 客户端的socket
	 */
	private Socket socket;

	/**
	 * 服务socket
	 */
	private SocketServer socketServer;

	/**
	 * 封装的客户端连接socket
	 */
	private Connection connection;

	/**
	 * 判断当前连接是否运行
	 */
	private boolean isRunning;

	public ConnectionThread(Socket socket, SocketServer socketServer) {
		this.socket = socket;
		this.socketServer = socketServer;
		connection = new Connection(socket, this);
		Date now = new Date();
		connection.setCreateTime(now);
		connection.setLastOnTime(now);
		isRunning = true;
	}

	@Override
	public void run() {
		while (isRunning) {
			// Check whether the socket is closed.
			if (socket.isClosed()) {
				isRunning = false;
				break;
			}
			BufferedReader reader;
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String message;
				while ((message = reader.readLine()) != null) {
					log.info("服务端收到消息：" + message);
					/*ServerReceiveDto receiveDto;
					try {
						receiveDto = JSONObject.parseObject(message, ServerReceiveDto.class);
					} catch (Exception e) {
						ServerSendDto dto = new ServerSendDto();
						dto.setStatusCode(999);
						dto.setErrorMessage("data error");
						connection.println(JSONObject.toJSONString(dto));
						break;
					}
					Integer functionCode = receiveDto.getFunctionCode();*/

					if (message.contains("USERSTATE")) {
						//心跳类型
						connection.setLastOnTime(new Date());
						/*ServerSendDto dto = new ServerSendDto();
						dto.setFunctionCode(FunctionCodeEnum.HEART.getValue());
						dto.setMessage("服务端💗💗💗");*/
						connection.println("服务端心跳包"+"\r\n");
					} else if ( message.startsWith("Parking-Dongyang")) {
						//登陆，身份验证
						String parkId = message;
						if (socketServer.getLoginHandler().canLogin(parkId)) {
							connection.setLogin(true);
							connection.setParkId(parkId);
							if (socketServer.getExistSocketMap().containsKey(parkId)) {
								//存在已登录的用户，发送登出指令并主动关闭该socket
								Connection existConnection = socketServer.getExistSocketMap().get(parkId);
								ServerSendDto dto = new ServerSendDto();
								dto.setStatusCode(999);
								dto.setFunctionCode(FunctionCodeEnum.MESSAGE.getValue());
								dto.setErrorMessage("force logout");
								existConnection.println(JSONObject.toJSONString(dto));
								existConnection.getConnectionThread().stopRunning();
								log.error("========用户被客户端重入踢出，parkId:{}", parkId);
							}
							//添加到已登录map中
							socketServer.getExistSocketMap().put(parkId, connection);
							/*ServerSendDto serverSendDto = new ServerSendDto();
							serverSendDto.setFunctionCode(FunctionCodeEnum.MESSAGE.getValue());
							serverSendDto.setMessage("GETPARKID");
							connection.println(JSONObject.toJSONString(serverSendDto));*/
							log.info("登录成功");
							connection.println("GETPARKID 15"+"\r\n");
						} else {
							//用户鉴权失败
							ServerSendDto dto = new ServerSendDto();
							dto.setStatusCode(999);
							dto.setFunctionCode(FunctionCodeEnum.MESSAGE.getValue());
							dto.setErrorMessage("user valid failed");
							connection.println(JSONObject.toJSONString(dto));
							log.error("用户鉴权失败,parkId:{}", parkId);
						}
					} else if (message.startsWith("GET_CAR_POSITION")) {
						log.info("很重要的message：{}" , message );
						String start = "GET_CAR_POSITION" ;
						String jsonString = message.substring(start.length()) ;
						GetCarPositionDto getCarPositionDto = JSONObject.parseObject(jsonString , GetCarPositionDto.class);
						Position position = getCarPositionDto.getPosition().get(0) ;
						log.info("=================最终的结果 position：{}" , position);
						//发送一些其他消息等
						socketServer.getMessageHandler().onReceive(connection, getCarPositionDto);
					} else if (message.startsWith("GETPARKID") ){
						log.info("收到signature的信息了,signature: {}" , message);
						//GETPARKID{"parkId":"1","time":"2015-11-24 10:33:58","signature":"112233afadf"}\r\n
						String jsonString = message.substring(9) ;
						GetParkIdDto receiveDto = JSONObject.parseObject(jsonString, GetParkIdDto.class);
						String str = receiveDto.getParkId() + receiveDto.getTime() ;
						if(!receiveDto.getSignature().equals(DigestUtils.md5DigestAsHex(str.getBytes()))){
							log.info("signature 不一样");
							this.stopRunning();
						}
					}/*else if (functionCode.equals(FunctionCodeEnum.CLOSE.getValue())) {
						//主动关闭客户端socket
						log.info("客户端主动登出socket");
						this.stopRunning();
					}*/

				}
			} catch (IOException e) {
				log.error("ConnectionThread.run failed. IOException:{}", e.getMessage());
				this.stopRunning();
			}
		}
	}

	public void stopRunning() {
		if (this.connection.isLogin()) {
			log.info("停止一个socket连接,ip:{},parkId:{}", this.socket.getInetAddress().toString(),
					this.connection.getParkId());
		} else {
			log.info("停止一个还未身份验证的socket连接,ip:{}", this.socket.getInetAddress().toString());
		}
		isRunning = false;
		try {
			socket.close();
		} catch (IOException e) {
			log.error("ConnectionThread.stopRunning failed.exception:{}", e);
		}
	}
}
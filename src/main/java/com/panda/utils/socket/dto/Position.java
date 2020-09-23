package com.panda.utils.socket.dto;

import lombok.Data;

/**
 * @author camel
 * @version 1.0
 * @date 2020/9/23 8:37 下午
 */
@Data
public class Position {

    //{"parkName":"停车场名称","parkSpace":"车位名称","plateId":"A1234"}

    private String parkName ;

    private String parkSpace ;

    private String plateId ;


}

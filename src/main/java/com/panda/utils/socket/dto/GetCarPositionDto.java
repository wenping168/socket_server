package com.panda.utils.socket.dto;

import lombok.Data;

import java.util.List;

/**
 * @author camel
 * @version 1.0
 * @date 2020/9/23 8:36 下午
 */
@Data
public class GetCarPositionDto {

    //{"status":"success","plate":"123","position":[{"parkName":"停车场名称","parkSpace":"车位名称","plateId":"A1234"},{"parkName":"停车场名称","parkSpace":"车位名称","plateId":"A1235"},{"parkName":"停车场名称","parkSpace":"车位名称","plateId":"A1236"}]}
    private String status ;

    private String plate ;

    private List<Position> position ;



}

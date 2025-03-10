package org.bh_foundation.e_sign.dto;

import lombok.Data;

@Data
public class ResponseDto<T> {
    
    private Integer status;
    private String Message;
    private T payload;

    public ResponseDto(
        Integer status,
        String message,
        T payload
    ) {
        this.status = status;
        this.Message = message;
        this.payload = payload;
    }

}

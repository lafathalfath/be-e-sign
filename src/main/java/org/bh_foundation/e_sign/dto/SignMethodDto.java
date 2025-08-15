package org.bh_foundation.e_sign.dto;

import lombok.Data;

@Data
public class SignMethodDto {

    private String filename = null;
    private byte[] blob;

}

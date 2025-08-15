package org.bh_foundation.e_sign.dto;


import lombok.Data;

@Data
public class VerificationResult {
    
    private Integer number = 1;
    private boolean validity = false;
    private String signer = null;
    private String serialNumber = null;
    private String certifiedBy = null;
    private String signedAt = null;

}

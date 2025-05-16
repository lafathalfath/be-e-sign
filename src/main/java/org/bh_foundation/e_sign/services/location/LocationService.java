package org.bh_foundation.e_sign.services.location;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LocationService {
    
    public String getCountryByIp() {
        String ipApiUrl = "http://ip-api.com/json";
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.getForObject(ipApiUrl, String.class);
        if (jsonResponse != null && jsonResponse.contains("country")) {
            String country = jsonResponse.split("\"country\":\"")[1].split("\"")[0];
            return country;
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

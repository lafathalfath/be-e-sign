package org.bh_foundation.e_sign.services.auth;

import java.time.LocalDateTime;

import org.bh_foundation.e_sign.models.Session;
import org.bh_foundation.e_sign.repository.SessionRepository;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    private String getIpUserAgent(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forward-For");
        String userAgent = request.getHeader("User-Agent");
        if (xfHeader == null || xfHeader.length() == 0) {
            return request.getRemoteAddr() + " " + userAgent;
        }
        return xfHeader.split(",")[0] + " " + userAgent;
    }

    private void increaseLoginAttempt(Session sessionData) {
        sessionData.setAttempts(sessionData.getAttempts() + 1);
        sessionRepository.save(sessionData);
    }

    private Session getSessionData(HttpServletRequest request) {
        String ipUserAgent = getIpUserAgent(request);
        Session sessionData = sessionRepository.findByInfo(ipUserAgent);
        if (sessionData == null) {
            Session data = new Session();
            data.setInfo(ipUserAgent);
            data.setAttempts(1);
            data.setExpiredAt(LocalDateTime.now().plusDays(1));
            sessionRepository.save(data);
            return data;
        }
        increaseLoginAttempt(sessionData);
        return sessionData;
    }

    public boolean checkLoginAttempt(HttpServletRequest request) {
        Session sessionData = getSessionData(request);
        if (sessionData.getAttempts() >= 10 && LocalDateTime.now().isBefore(sessionData.getExpiredAt()))
            return false;
        return true;
    }

    public void clearLoginAttempt(HttpServletRequest request) {
        Session sessionData = getSessionData(request);
        sessionData.setAttempts(1);
        sessionRepository.save(sessionData);
    }

}

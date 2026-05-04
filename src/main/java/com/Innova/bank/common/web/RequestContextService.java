package com.Innova.bank.common.web;

import com.Innova.bank.common.constant.RequestConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class RequestContextService {

    public String getRequestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestConstants.REQUEST_ID_ATTRIBUTE);
        return requestId != null ? requestId.toString() : null;
    }

    public String getIp(HttpServletRequest request) {
        String forwarded = RequestConstants.FORWARDED_FOR_HEADER;

        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    public String getUserAgent(HttpServletRequest request) {
        return RequestConstants.USER_AGENT_HEADER;
    }

    public String getPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    public String getMethod(HttpServletRequest request) {
        return request.getMethod();
    }
}
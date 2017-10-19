package com.wwy.common.lang;

/**
 * @author wangxiaosan
 * @date 2017/10/18
 */
public enum ResponseCode {
    OK(200, "OK"),

    CREATED(201, "Created"),

    ACCEPTED(202, "Accepted"),

    NO_CONTENT(204, "No Content"),

    BAD_REQUEST(400, "Bad Request"),

    SC_UNAUTHORIZED(401, "Unauthorized Access"),

    FORBIDDEN(403, "Forbidden"),

    RESOURCE_NOT_FOUND(404, "Resource Not Found"),

    RESOURCE_NOT_ACCEPTABLE(406, "Resource Not Acceptable"),

    REQUEST_TIMEOUT(408, "Request Timeout"),

    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

    SERVICE_UNAVAILABLE(503, "Service Unavailable"),

    DATA_ACCESS_ERROR(510, "Data Access Error"),

    NOT_SUPPORTED_ERROR(512, "Not supported Error"),

    BUSINESS_ERROR(550, "Business Error");

    private final int code;
    private final String reason;

    ResponseCode(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int code() {
        return code;
    }

    public String reason() {
        return reason;
    }

    /**
     * Convert a numerical status code into the corresponding Status
     *
     * @param statusCode
     *            the numerical status code
     * @return the matching Status or null is no matching Status is defined
     */
    public static ResponseCode fromStatusCode(final int statusCode) {
        for (ResponseCode s : ResponseCode.values()) {
            if (s.code == statusCode) {
                return s;
            }
        }
        return null;
    }
}

package logger.utilities;

import logger.constants.AuditConstants;
import java.util.Map;

public final class NetworkUtils {
    private static final String[] IP_HEADER_CANDIDATES = {
            AuditConstants.HEADER_CLIENT_IP,
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    private NetworkUtils() {
        // Prevent instantiation
    }

    public static String getClientIp(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return AuditConstants.DEFAULT_UNKNOWN_SOURCE;
        }

        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = headers.get(header);
            if (ipList == null) {
                // Try case-insensitive lookup
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(header)) {
                        ipList = entry.getValue();
                        break;
                    }
                }
            }

            if (ipList != null && !ipList.isEmpty() && !"unknown".equalsIgnoreCase(ipList)) {
                // X-Forwarded-For might contain multiple IPs, the first one is the client
                int index = ipList.indexOf(',');
                if (index != -1) {
                    return ipList.substring(0, index).trim();
                }
                return ipList.trim();
            }
        }

        return AuditConstants.DEFAULT_UNKNOWN_SOURCE;
    }
}

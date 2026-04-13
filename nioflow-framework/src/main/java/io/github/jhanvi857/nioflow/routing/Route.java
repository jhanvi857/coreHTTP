package io.github.jhanvi857.nioflow.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {
    private final String method;
    private final Pattern pattern;
    private final List<String> paramNames;
    private final RouteHandler handler;

    public Route(String method, String pathDefinition, RouteHandler handler) {
        this.method = method.toUpperCase();
        this.handler = handler;
        this.paramNames = new ArrayList<>();

        String regex = pathDefinition;

        // Handle wildcard /* -> matches everything after
        if (regex.endsWith("/*")) {
            regex = regex.substring(0, regex.length() - 2) + "(?:/.*)?";
        }

        // Extract :paramName
        Matcher m = Pattern.compile(":([a-zA-Z0-9_]+)").matcher(regex);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            paramNames.add(m.group(1));
            m.appendReplacement(sb, "([^/]+)");
        }
        m.appendTail(sb);

        String finalRegex = sb.toString();
        if (finalRegex.equals("/")) {
            finalRegex = "^/$";
        } else if (!finalRegex.contains(".*")) {
            finalRegex = "^" + finalRegex + "/?$"; // optional trailing slash
        } else {
            finalRegex = "^" + finalRegex + "$";
        }

        this.pattern = Pattern.compile(finalRegex);
    }

    public boolean matches(String requestMethod, String requestPath) {
        if (!this.method.equals(requestMethod) && !this.method.equals("ANY")) {
            return false;
        }
        return pattern.matcher(requestPath).matches();
    }

    public Map<String, String> extractPathParams(String requestPath) {
        Map<String, String> params = new HashMap<>();
        Matcher m = pattern.matcher(requestPath);
        if (m.matches()) {
            for (int i = 0; i < paramNames.size(); i++) {
                params.put(paramNames.get(i), m.group(i + 1)); // group 0 is the entire match
            }
        }
        return params;
    }

    public RouteHandler getHandler() {
        return handler;
    }
}

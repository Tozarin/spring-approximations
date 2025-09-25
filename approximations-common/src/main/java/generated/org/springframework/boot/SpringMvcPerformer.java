package generated.org.springframework.boot;

import generated.org.springframework.boot.pinnedValues.PinnedValueSource;
import generated.org.springframework.boot.pinnedValues.PinnedValueStorage;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.usvm.api.Engine;
import org.usvm.spring.api.SpringEngine;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static generated.org.springframework.boot.pinnedValues.PinnedValueSource.REQUEST_USER;
import static generated.org.springframework.boot.pinnedValues.PinnedValueSource.RESOLVED_EXCEPTION_CLASS;
import static generated.org.springframework.boot.pinnedValues.PinnedValueSource.RESOLVED_EXCEPTION_MESSAGE;
import static generated.org.springframework.boot.pinnedValues.PinnedValueSource.UNHANDLED_EXCEPTION_CLASS;
import static generated.org.springframework.boot.pinnedValues.PinnedValueSource.UNHANDLED_EXCEPTION_MESSAGE;
import static generated.org.springframework.boot.pinnedValues.PinnedValueSource.VIEW_NAME;
import static generated.org.springframework.boot.pinnedValues.PinnedValueStorage.getPinnedValue;
import static generated.org.springframework.boot.pinnedValues.PinnedValueStorage.writePinnedValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

public class SpringMvcPerformer {

    public static void perform(MockMvc mockMvc) {
        List<List<Object>> allPaths = SpringEngine.allControllerPaths();
        boolean securityEnabled = SpringEngine.isSecurityEnabled();
        for (List<Object> pathData : allPaths) {
            boolean pathFound = Engine.makeSymbolicBoolean();
            if (!pathFound) {
                SpringEngine.markAsGoodPath();
                continue;
            }

            SpringEngine.markAsGoodPath();
            String controllerName = (String) pathData.get(0);
            String path = (String) pathData.get(2);
            Integer paramCount = (Integer) pathData.get(3);
            String methodName = (String) pathData.get(4);

            SpringUtils.internalLog("[USVM] starting to analyze path ", path, " of controller ", controllerName);
            writePinnedValue(PinnedValueSource.REQUEST_PATH, path);
            writePinnedValue(PinnedValueSource.REQUEST_METHOD, methodName);

            Object[] pathArgs = new Object[paramCount];
            Arrays.fill(pathArgs, 0);
            try {
                HttpMethod method = HttpMethod.valueOf(methodName);
                MockHttpServletRequestBuilder request = request(method, path, pathArgs);
                if (securityEnabled) {
                    // Makes successful authorization
                    UserDetails userDetails = createUser();
                    fillSecurityHeaders();
                    request = request.with(user(userDetails));
                }
                MvcResult result = mockMvc.perform(request).andReturn();
                writeResult(result);
                SpringUtils.internalLog("[USVM] end of path analysis", path);
            } catch (Throwable e) {
                Throwable root = e;
                while (root.getCause() != null) root = root.getCause();
                writePinnedValue(UNHANDLED_EXCEPTION_CLASS, root.getClass());
                writePinnedValue(UNHANDLED_EXCEPTION_MESSAGE, root.getMessage());
                SpringUtils.internalLog("[USVM] analysis finished with exception", path);
            } finally {
                if (securityEnabled && getPinnedValue(REQUEST_USER, UserDetails.class) == null)
                    writePinnedValue(REQUEST_USER, createUser());
                PinnedValueStorage.preparePinnedValues();
            }
            return;
        }
    }

    private static void writeResponse(MockHttpServletResponse response) {
        writePinnedValue(PinnedValueSource.RESPONSE_STATUS, response.getStatus());
        try {
            writePinnedValue(PinnedValueSource.RESPONSE_CONTENT, response.getContentAsString());
        } catch (UnsupportedEncodingException e) {
            SpringUtils.internalLog("[ERROR] Writing response content failed because of unsupported encoding: %s".formatted(e.getMessage()));
        }
        for (String headerName : response.getHeaderNames()) {
            writePinnedValue(PinnedValueSource.RESPONSE_HEADER, headerName, response.getHeaders(headerName));
        }
        for (Cookie cookie : response.getCookies()) {
            writePinnedValue(PinnedValueSource.REQUEST_COOKIE, cookie.getName(), cookie);
        }
    }

    private static void fillSecurityHeaders() {
        writePinnedValue(PinnedValueSource.REQUEST_HEADER, "AUTHORIZATION", null, String.class);
    }

    private static UserDetails createUser() {
        String username = "Test user";
        String password = "Test password";
        return new User(username, password, Collections.emptyList());
    }

    private static void writeResult(MvcResult result) {
        writeResponse(result.getResponse());
        Throwable resolvedException = result.getResolvedException();

        if (resolvedException != null) {
            writePinnedValue(RESOLVED_EXCEPTION_CLASS, resolvedException.getClass());
            writePinnedValue(RESOLVED_EXCEPTION_MESSAGE, resolvedException.getMessage());
        }

        ModelAndView mav = result.getModelAndView();
        if (mav != null) {
            writePinnedValue(VIEW_NAME, mav.getViewName());
        }
    }
}

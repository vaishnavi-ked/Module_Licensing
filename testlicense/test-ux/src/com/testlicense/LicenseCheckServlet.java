package com.testlicense;

import com.tridium.json.JSONObject;

import javax.baja.user.BUser;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LicenseCheckServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // Set response type first
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Get authentication info
        BUser user = BUser.getCurrentAuthenticatedUser();
        String userName = user != null ? user.getUsername() : "unknown";
        String lang = req.getLocale().toLanguageTag();

        System.out.println("License check request from user: " + userName +
                " (language: " + lang + ")");

        JSONObject json = new JSONObject();

        try {
            // Check authentication
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                json.put("status", "error")
                        .put("message", "Authentication required");
                resp.getWriter().write(json.toString());
                return;
            }

            BLicense license = new BLicense();
            boolean verified = license.verifyLicense();

            json.put("status", verified ? "verified" : "not verified")
                    .put("message", license.getValidationMessage())
                    .put("user", userName)
                    .put("language", lang);

            resp.setStatus(verified ? HttpServletResponse.SC_OK :
                    HttpServletResponse.SC_FORBIDDEN);

        } catch (Exception e) {
            System.err.println("License check error:");
            e.printStackTrace();

            json.put("status", "error")
                    .put("message", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        System.out.println("Returning response: " + json);
        resp.getWriter().write(json.toString());
    }
}
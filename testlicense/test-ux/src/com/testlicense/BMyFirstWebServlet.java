package com.testlicense;

import javax.baja.nre.annotations.NiagaraProperty;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.*;
import javax.baja.web.BWebServlet;
import javax.baja.web.WebOp;

@NiagaraType
@NiagaraProperty(
        name = "servletName",
        type = "baja:String",
        flags = Flags.READONLY,
        defaultValue = "LicenseCheckServlet"
)
public final class BMyFirstWebServlet extends BWebServlet {
    /*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
    public static final Property servletName = BComplex.newProperty(Flags.READONLY, "LicenseCheckServlet", null);
    public String getServletName() { return getString(servletName); }
    public void setServletName(String v) { setString(servletName, v, null); }
    @Override public Type getType() { return TYPE; }
    public static final Type TYPE = Sys.loadType(BMyFirstWebServlet.class);
    /*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/

    // Map the servlet to the root path "/LicenseCheckServlet"
    public String[] getPaths() {
        return new String[]{"/" + getServletName()};
    }

    @Override
    public void doGet(WebOp op) throws Exception {
        op.getResponse().setContentType("text/html");
        op.getHtmlWriter()
                .w("<!DOCTYPE html>").nl()
                .w("<html>").nl()
                .w("<head><title>License Check</title></head>").nl()
                .w("<body>").nl()
                .w("<h1>License Verification</h1>").nl()
                .w("<p>Servlet is working correctly!</p>").nl()
                .w("</body>").nl()
                .w("</html>");
    }
}
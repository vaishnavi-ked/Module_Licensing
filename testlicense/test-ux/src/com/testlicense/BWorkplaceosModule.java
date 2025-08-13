package com.testlicense;



import javax.baja.naming.BOrd;
import javax.baja.sys.*;
import javax.baja.web.BIFormFactorMini;
import javax.baja.web.BIOffline;
import javax.baja.web.js.BIJavaScript;
import javax.baja.web.js.JsInfo;

public class BWorkplaceosModule extends BSingleton
        implements BIJavaScript, BIFormFactorMini, BIOffline {
    private BWorkplaceosModule() {}

    public JsInfo getJsInfo(Context cx) { return JS_INFO; }

    private static final JsInfo JS_INFO = JsInfo.make(
            BOrd.make("module://test/rc/TestWidget.js"),
            BWorkplaceosModuleJsBuild.TYPE
    );
}

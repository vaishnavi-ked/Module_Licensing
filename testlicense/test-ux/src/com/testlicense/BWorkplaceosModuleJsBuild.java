package com.testlicense;

import javax.baja.naming.BOrd;
import javax.baja.web.js.BJsBuild;


public class BWorkplaceosModuleJsBuild extends BJsBuild {
    public static final BWorkplaceosModuleJsBuild INSTANCE = new BWorkplaceosModuleJsBuild(
            "test", //webdev ID
            new BOrd[] {
                    BOrd.make("module://test/rc/test.built.min.js")
            }
    );

    protected BWorkplaceosModuleJsBuild(String id, BOrd[] builtFiles) {
        super(id, builtFiles);
    }
}


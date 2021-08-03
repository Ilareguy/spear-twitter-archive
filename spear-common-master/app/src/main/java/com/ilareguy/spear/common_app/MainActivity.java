package com.ilareguy.spear.common_app;

import com.ilareguy.spear.ActivityAbstract;
import com.ilareguy.spear.PageAbstract;

public class MainActivity extends ActivityAbstract {

    protected PageAbstract buildMainPage(){
        return new TabsPage(this);
    }

}

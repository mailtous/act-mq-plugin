package com.artlongs.act.mq.plugin.core;

import act.Act;
import act.app.App;
import act.app.conf.AutoConfig;
import act.plugin.AppServicePlugin;
import org.omg.PortableInterceptor.ACTIVE;
import org.osgl.$;
import org.osgl.util.Const;

import act.app.conf.AutoConfigPlugin;

import static act.app.conf.AutoConfigPlugin.loadPluginAutoConfig;

/**
 * ACT-MQ-PLUGIN
 * Created by leeton on 2018/11/27.
 */
public class MqPlugin extends AppServicePlugin {

    @Override
    protected void applyTo(App app) {
        loadPluginAutoConfig(MqConfig.class, app);
    }

    @Override
    public void register() {
        super.register();
    }
}

package com.artlongs.act.mq.plugin.core;

import act.app.conf.AppConfigPlugin;
import act.plugin.Plugin;

/**
 * ACT-MQ-PLUGIN
 * Created by leeton on 2018/11/27.
 */
public class MqPlugin implements Plugin {

    @Override
    public void register() {
        AppConfigPlugin.foundConfigurator(MqConfig.class);

    }
}

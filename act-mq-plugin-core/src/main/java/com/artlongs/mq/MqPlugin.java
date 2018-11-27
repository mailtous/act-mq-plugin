package com.artlongs.mq;

import act.app.conf.AppConfigPlugin;
import act.plugin.Plugin;

import javax.inject.Inject;

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

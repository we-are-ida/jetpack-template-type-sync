package be.idamediafoundry.jetpack.templatetypesync.service.impl;

import be.idamediafoundry.jetpack.templatetypesync.service.SyncTemplateExecutor;
import be.idamediafoundry.jetpack.templatetypesync.service.SyncTemplateExecutorConfiguration;
import be.idamediafoundry.jetpack.templatetypesync.service.SyncTemplateService;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author michael
 * @since 09/05/2020
 */
@Component(service = SyncTemplateExecutor.class,
        name = "Jetpack Sync Templates Executor",
        immediate = true
        //configurationPolicy = ConfigurationPolicy.REQUIRE
)
@Designate(ocd = SyncTemplateExecutorConfiguration.class)
public class SyncTemplateExecutorImpl implements SyncTemplateExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SyncTemplateExecutorImpl.class);

    @Reference
    private SyncTemplateService syncTemplateService;

    private SyncTemplateExecutorConfiguration config;

    @Activate
    @Modified
    protected void activate(SyncTemplateExecutorConfiguration config) {
        this.config = config;
        if (config.enabled()) {
            syncTemplates();
        }
    }

    @Override
    public void syncTemplates() {
        for (String config : config.names()) {
            //TODO try + catch
            syncTemplateService.syncTemplates("/conf/" + config);
        }
    }
}

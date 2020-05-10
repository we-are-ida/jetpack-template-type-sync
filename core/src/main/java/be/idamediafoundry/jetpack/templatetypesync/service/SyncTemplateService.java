package be.idamediafoundry.jetpack.templatetypesync.service;

import org.apache.sling.api.resource.Resource;

/**
 * @author michael
 * @since 09/05/2020
 */
public interface SyncTemplateService {

    boolean syncTemplates(Resource confResource);

    boolean syncTemplates(String confPath);
}

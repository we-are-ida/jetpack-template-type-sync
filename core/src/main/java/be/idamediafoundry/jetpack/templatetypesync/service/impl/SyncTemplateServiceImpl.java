package be.idamediafoundry.jetpack.templatetypesync.service.impl;

import be.idamediafoundry.jetpack.templatetypesync.service.SyncTemplateService;
import com.day.cq.commons.servlets.HtmlStatusResponseHelper;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.TemplateManager;
import org.apache.commons.collections.Predicate;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.*;
import org.apache.sling.api.servlets.HtmlResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author michael
 * @since 09/05/2020
 */
@Component(service = SyncTemplateService.class,
        name = "Jetpack Sync Templates Service"
)
public class SyncTemplateServiceImpl implements SyncTemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(SyncTemplateServiceImpl.class);

    private static final String DEFAULT_USER = "jetpack-template-type-sync";
    private static final String DEFAULT_SERVICE = "be.idamediafoundry.jetpack.templatetypesync.core";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public boolean syncTemplates(String confPath) {
        try (ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(getCredentials())) {
            Resource resource = resourceResolver.getResource(confPath);
            //TODO
            return true;
        } catch (LoginException e) {
            LOG.error("Couldn't login to get the Configuration", e);
            return false;
        }
    }

    @Override
    public boolean syncTemplates(Resource confResource) {

        //TODO: throw exception if wrong

        TemplateManager templateManager = confResource.getResourceResolver().adaptTo(TemplateManager.class);
        ResourceResolver resourceResolver = confResource.getResourceResolver();

        //TODO confResource exists?
        String confPath = confResource.getPath();

        try {
            List<Template> templateTypes = templateManager.getTemplateTypes(confResource.getPath());
            templateTypes.forEach(templateType -> processTemplate(templateType, templateManager, confPath, resourceResolver));

            resourceResolver.commit();

            return true;
        } catch (PersistenceException ex) {
            //TODO log
            return false;
        }
    }

    private void processTemplate(Template templateType, TemplateManager templateManager, String conf, ResourceResolver resourceResolver) {
        String type = templateType.getPath();
        Resource templateTypePolicies = resourceResolver.getResource(type + "/policies");

        //get templates for templateType
        List<Template> templates = templateManager.getTemplates(new TemplatePredicate(conf, type));
        templates.forEach(template -> syncTemplateTypeWithTemplate(templateTypePolicies, type, template, resourceResolver));
    }

    private void syncTemplateTypeWithTemplate(Resource templateTypePolicies, String type, Template template, ResourceResolver resourceResolver) {
        templateTypePolicies.getChildren().forEach(resource -> {
            String templatePolicyPath = resource.getPath().replace(type, template.getPath());
            Resource r = resourceResolver.getResource(templatePolicyPath);
            if (r == null) {
                try {
                    String copyLocation = templatePolicyPath.substring(0, templatePolicyPath.lastIndexOf("/"));
                    resourceResolver.copy(resource.getPath(), copyLocation);

                    Resource policyConfig = resourceResolver.getResource(template.getPath() + "/policies/jcr:content");
                    if (policyConfig != null) {
                        Node policyConfigNode = policyConfig.adaptTo(Node.class);
                        policyConfigNode.setProperty("cq:lastModified", Calendar.getInstance());
                    }
                } catch (Exception pex) {
                    //TODO
                }
            } else {
                syncTemplateTypeWithTemplate(resource, type, template, resourceResolver);
            }
        });
    }

    private class TemplatePredicate implements Predicate {

        private String conf;
        private String templateType;

        private TemplatePredicate(String conf, String templateType) {
            this.conf = conf;
            this.templateType = templateType;
        }

        @Override
        public boolean evaluate(Object o) {
            Template template = ((Template) o);
            if (template.getPath().startsWith(conf) && !template.getPath().contains("/template-types/")) {
                String templateType = template.getProperties().get("cq:templateType", String.class);
                return templateType != null && templateType.equals(this.templateType);
            }
            return false;
        }
    }

    private Map<String, Object> getCredentials() {
        Map<String, Object> credentials = new HashMap<>();
        credentials.put(ResourceResolverFactory.USER, DEFAULT_USER);
        credentials.put(ResourceResolverFactory.SUBSERVICE, DEFAULT_SERVICE);
        return credentials;
    }
}

package be.ida_mediafoundry.jetpack.templatetypesync.servlets;

import com.day.cq.commons.servlets.HtmlStatusResponseHelper;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.TemplateManager;
import com.day.cq.wcm.api.commands.WCMCommand;
import com.day.cq.wcm.api.commands.WCMCommandContext;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HtmlResponse;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import java.util.List;

/**
 * Sync Template Types
 * Will Sync the Policies for the template-types to the Templates for the /conf folder provided in the "path" request parameter.
 */
@Component(service = WCMCommand.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Jetpack - Sync Template-Types"

        })
public class SyncTemplateTypesWcmCommand implements WCMCommand {

    @Override
    public String getCommandName() {
        return "syncTemplateTypes";
    }

    @Override
    public HtmlResponse performCommand(WCMCommandContext wcmCommandContext,
                                       SlingHttpServletRequest slingHttpServletRequest,
                                       SlingHttpServletResponse slingHttpServletResponse,
                                       PageManager pageManager) {

        Resource resource = slingHttpServletRequest.getResource();

        RequestParameter path = slingHttpServletRequest.getRequestParameter(PATH_PARAM);
        String confPath = path.getString();

        if (StringUtils.isBlank(confPath)) {
            return HtmlStatusResponseHelper.createStatusResponse(false, "No Path provided");
        }

        return doProcess(resource, path, confPath);
    }

    private HtmlResponse doProcess(Resource resource, RequestParameter path, String confPath) {
        TemplateManager templateManager = resource.getResourceResolver().adaptTo(TemplateManager.class);
        ResourceResolver resourceResolver = resource.getResourceResolver();


        try {
            List<Template> templateTypes = templateManager.getTemplateTypes(confPath);
            templateTypes.forEach(templateType -> processTemplate(templateType, templateManager, path.getString(), resourceResolver));

            resourceResolver.commit();

            return HtmlStatusResponseHelper.createStatusResponse(true, "Synced", confPath);
        } catch (PersistenceException ex) {
            //TODO log
            return HtmlStatusResponseHelper.createStatusResponse(false, "Not saved", confPath);
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
                } catch (PersistenceException pex) {
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
}

package be.idamediafoundry.jetpack.templatetypesync.servlets;

import be.idamediafoundry.jetpack.templatetypesync.service.SyncTemplateService;
import com.day.cq.commons.servlets.HtmlStatusResponseHelper;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.commands.WCMCommand;
import com.day.cq.wcm.api.commands.WCMCommandContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HtmlResponse;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Sync Template Types
 * Will Sync the Policies for the template-types to the Templates for the /conf folder provided in the "path" request parameter.
 */
@Component(service = WCMCommand.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Jetpack - Sync Template-Types"

        })
public class SyncTemplateTypesWcmCommand implements WCMCommand {

    @Reference
    private SyncTemplateService syncTemplateService;

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
        ResourceResolver resourceResolver = resource.getResourceResolver();

        RequestParameter path = slingHttpServletRequest.getRequestParameter(PATH_PARAM);
        String confPath = path.getString();

        if (StringUtils.isBlank(confPath)) {
            return HtmlStatusResponseHelper.createStatusResponse(false, "No Path provided");
        }

        HtmlResponse htmlResponse = null;
        try {
            Resource confResource = resourceResolver.getResource(confPath);
            if (confResource == null) {
                htmlResponse = HtmlStatusResponseHelper.createStatusResponse(false, "Configuration node found", confPath);
            } else {
                boolean success = syncTemplateService.syncTemplates(confResource);
                htmlResponse = HtmlStatusResponseHelper.createStatusResponse(success, "Synced", confPath);
            }
        } catch (Exception ex) {
            //TODO log
            htmlResponse = HtmlStatusResponseHelper.createStatusResponse(false, "Not saved", confPath);
        }

        return htmlResponse;
    }
}

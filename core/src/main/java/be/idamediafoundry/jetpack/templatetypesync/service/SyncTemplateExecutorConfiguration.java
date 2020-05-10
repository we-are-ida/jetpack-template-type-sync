package be.idamediafoundry.jetpack.templatetypesync.service;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Jetpack Sync Templates configuration",
    description = "Jetpack Sync Templates configuration"
)
public @interface SyncTemplateExecutorConfiguration {

    @AttributeDefinition(
            name = "Enable",
            description = "Enable syncing the templates on deploy"
    )
    boolean enabled();

    @AttributeDefinition(
        name = "All configurations to check",
        description = "Add the name of the config like you can find it in this path: /conf/config-name"
    )
    String[] names() default {};
}

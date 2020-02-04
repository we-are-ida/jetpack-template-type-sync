(function ($) {
    "use strict";

    var COMMAND_URL = Granite.HTTP.externalize("/bin/wcmcommand");
    var COMMAND_NAME = "syncTemplateTypes";

    var registry = $(window).adaptTo("foundation-registry");

    function showSuccessMessage(path) {
        var dialog = new Coral.Dialog().set({
            id: 'success-sync',
            variant: "success",
            header: {
                innerHTML: 'Template-Type Sync'
            },
            content: {
                innerHTML: 'Templates synced successfully with Template-types for ' + path
            },
            footer: {
                innerHTML: '<button is="coral-button" variant="primary" coral-close>Close</button>'
            }
        });
        dialog.show();
    }

    function showErrorMessage(path) {
        var dialog = new Coral.Dialog().set({
            id: 'failed-sync',
            variant: "error",
            header: {
                innerHTML: 'Template-Type Sync'
            },
            content: {
                innerHTML: 'Synced failed for' + path
            },
            footer: {
                innerHTML: '<button is="coral-button" variant="primary" coral-close>Close</button>'
            }
        });
        dialog.show();
    }

    registry.register("foundation.collection.action.action", {
        name: "jetpack.configuration-browser.sync",
        handler: function (name, el, config, collection, selections) {
            var path = null;
            selections.forEach(function (selection) {
                var $sel = $(selection);
                path = $sel.data('foundation-collection-item-id');
            });

            $.ajax({
                url: COMMAND_URL,
                type: "POST",
                data: {
                    _charset_: "UTF-8",
                    cmd: COMMAND_NAME,
                    path: path
                }
            }).fail(function() {
                showErrorMessage(path);
            }).done(function() {
                showSuccessMessage(path);
            });
        }
    });

})(jQuery);
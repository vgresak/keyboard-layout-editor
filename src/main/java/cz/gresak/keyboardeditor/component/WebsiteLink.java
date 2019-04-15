package cz.gresak.keyboardeditor.component;

import cz.gresak.keyboardeditor.service.ServiceLoader;
import cz.gresak.keyboardeditor.service.api.CommandExecutor;
import javafx.application.HostServices;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;

public class WebsiteLink extends Hyperlink {
    private static final HostServices HOST_SERVICES = ServiceLoader.lookup(HostServices.class);
    private static final CommandExecutor COMMAND_EXECUTOR = ServiceLoader.lookup(CommandExecutor.class);

    public WebsiteLink(String text, String url) {
        super(text);
        setTooltip(new Tooltip(url));
        setOnAction(event -> openInBrowser(url));
    }

    private void openInBrowser(String url) {
        try {
            HOST_SERVICES.showDocument(url);
        } catch (NullPointerException npe) {
            // HostServices are not available, try xdg-open
            COMMAND_EXECUTOR.execute(String.format("xdg-open %s", url));
        }
    }
}

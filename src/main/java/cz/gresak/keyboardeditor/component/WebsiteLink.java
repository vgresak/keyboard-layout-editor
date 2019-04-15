package cz.gresak.keyboardeditor.component;

import cz.gresak.keyboardeditor.service.ServiceLoader;
import javafx.application.HostServices;
import javafx.scene.control.Hyperlink;

public class WebsiteLink extends Hyperlink {
    private static final HostServices HOST_SERVICES = ServiceLoader.lookup(HostServices.class);

    public WebsiteLink(String text, String url) {
        super(text);
        setOnAction(event -> HOST_SERVICES.showDocument(url));
    }
}

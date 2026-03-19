package ${package}.base;

import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinRequest;
import org.springframework.stereotype.Component;

/**
 * Sets the UI locale from the browser's Accept-Language header
 * before any views or layouts are constructed.
 * <p>
 * Without this, the JVM's default locale is used, which on a
 * German macOS would show German translations even when the
 * browser is set to English.
 */
@Component
public class BrowserLocaleInitListener implements UIInitListener {

    @Override
    public void uiInit(UIInitEvent event) {
        VaadinRequest request = VaadinRequest.getCurrent();
        if (request != null) {
            event.getUI().setLocale(request.getLocale());
        }
    }
}

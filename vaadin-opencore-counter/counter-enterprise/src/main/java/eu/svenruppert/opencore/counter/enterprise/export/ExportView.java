package eu.svenruppert.opencore.counter.enterprise.export;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import eu.svenruppert.opencore.counter.app.Application;

public class ExportView extends VerticalLayout {

  public static final String ID_TEXTAREA = "export-json";

  public ExportView() {
    add(new H2("Export"));

    TextArea area = new TextArea();
    area.setId(ID_TEXTAREA);
    area.setWidthFull();
    area.setReadOnly(true);
    area.setValue(ExportJsonBuilder.buildFromStore(
        Application.context().counterService().value()));
    add(area);
  }
}

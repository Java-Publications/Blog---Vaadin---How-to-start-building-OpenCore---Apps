package com.svenruppert.opencore.counter.ui.core;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class AboutView extends VerticalLayout {

  public AboutView() {
    add(new H2("About"));
    add(new Paragraph(
        "This is the OSS / community edition of the OpenCore Counter demo."));
    add(new Paragraph(
        "Additional features appear automatically when the Enterprise "
            + "module is on the classpath."));
    add(new Paragraph(
        "Features are loaded through Java ServiceLoader. There is no "
            + "configuration server, no role check and no licence check."));
  }
}

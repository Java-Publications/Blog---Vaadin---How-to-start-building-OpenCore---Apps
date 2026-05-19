package com.svenruppert.opencore.counter.enterprise.ui.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.Style;

/**
 * Renders a small "Enterprise Edition" pill next to the application
 * title. The styling is applied inline with hard-coded values that
 * mirror Vaadin's Lumo "badge" theme, so the component is fully
 * self-contained — no global CSS file required and the look is the
 * same whether Lumo design tokens cascade through the DOM or not.
 */
public class EnterpriseEditionBadge extends Composite<Span> {

  public static final String LABEL = "Enterprise Edition";

  public EnterpriseEditionBadge() {
    Span root = getContent();
    root.setText(LABEL);

    Style s = root.getStyle();
    s.set("display", "inline-flex");
    s.set("align-items", "center");
    s.set("padding", "0.25rem 0.625rem");
    s.set("color", "#0050b3");
    s.set("background-color", "#e6f4ff");
    s.set("border", "1px solid #91caff");
    s.set("border-radius", "0.25rem");
    s.set("font-size", "0.78rem");
    s.set("font-weight", "600");
    s.set("line-height", "1");
    s.set("letter-spacing", "0.02em");
    s.set("text-transform", "uppercase");
    s.set("white-space", "nowrap");
  }
}

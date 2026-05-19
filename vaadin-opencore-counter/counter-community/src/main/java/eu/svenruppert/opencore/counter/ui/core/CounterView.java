package eu.svenruppert.opencore.counter.ui.core;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import eu.svenruppert.opencore.counter.app.Application;
import eu.svenruppert.opencore.counter.domain.CounterService;

public class CounterView extends VerticalLayout {

  public static final String ID_VALUE_LABEL = "counter-value";
  public static final String ID_INCREMENT_BUTTON = "btn-increment";
  public static final String ID_DECREMENT_BUTTON = "btn-decrement";
  public static final String ID_RESET_BUTTON = "btn-reset";

  private final CounterService counterService;
  private final Span valueLabel;

  public CounterView() {
    this.counterService = Application.context().counterService();

    add(new H2("Counter"));

    valueLabel = new Span(String.valueOf(counterService.value()));
    valueLabel.setId(ID_VALUE_LABEL);
    add(valueLabel);

    Button increment = new Button("+1", e -> {
      counterService.increment();
      refresh();
    });
    increment.setId(ID_INCREMENT_BUTTON);

    Button decrement = new Button("-1", e -> {
      counterService.decrement();
      refresh();
    });
    decrement.setId(ID_DECREMENT_BUTTON);

    Button reset = new Button("Reset", e -> {
      counterService.reset();
      refresh();
    });
    reset.setId(ID_RESET_BUTTON);

    add(new HorizontalLayout(increment, decrement, reset));
  }

  private void refresh() {
    valueLabel.setText(String.valueOf(counterService.value()));
  }
}

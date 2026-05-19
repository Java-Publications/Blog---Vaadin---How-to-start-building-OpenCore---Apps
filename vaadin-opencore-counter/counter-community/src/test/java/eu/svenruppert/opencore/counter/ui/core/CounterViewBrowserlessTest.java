package eu.svenruppert.opencore.counter.ui.core;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonTester;
import com.vaadin.flow.component.html.Span;
import com.vaadin.browserless.ViewPackages;
import eu.svenruppert.opencore.counter.app.Application;
import eu.svenruppert.opencore.counter.app.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CounterView — browserless")
@ViewPackages(packages = {"eu.svenruppert.opencore.counter"})
class CounterViewBrowserlessTest extends BrowserlessTest {

  @BeforeEach
  void freshContext() {
    Application.replaceContext(new ApplicationContext());
  }

  @Test
  @DisplayName("Counter view renders with initial value 0")
  void rendersWithZero() {
    navigate(CounterView.class);
    assertEquals("0", $view(Span.class).id(CounterView.ID_VALUE_LABEL).getText());
  }

  @Test
  @DisplayName("clicking +1 increases the displayed value to 1")
  void incrementUpdatesLabel() {
    navigate(CounterView.class);

    ButtonTester<Button> inc = test($view(Button.class).id(CounterView.ID_INCREMENT_BUTTON));
    inc.click();

    assertEquals("1", $view(Span.class).id(CounterView.ID_VALUE_LABEL).getText());
  }

  @Test
  @DisplayName("clicking -1 decreases the displayed value")
  void decrementUpdatesLabel() {
    navigate(CounterView.class);

    ButtonTester<Button> dec = test($view(Button.class).id(CounterView.ID_DECREMENT_BUTTON));
    dec.click();
    dec.click();

    assertEquals("-2", $view(Span.class).id(CounterView.ID_VALUE_LABEL).getText());
  }

  @Test
  @DisplayName("clicking Reset returns the displayed value to 0")
  void resetReturnsToZero() {
    navigate(CounterView.class);

    test($view(Button.class).id(CounterView.ID_INCREMENT_BUTTON)).click();
    test($view(Button.class).id(CounterView.ID_INCREMENT_BUTTON)).click();
    test($view(Button.class).id(CounterView.ID_INCREMENT_BUTTON)).click();
    assertEquals("3", $view(Span.class).id(CounterView.ID_VALUE_LABEL).getText());

    test($view(Button.class).id(CounterView.ID_RESET_BUTTON)).click();
    assertEquals("0", $view(Span.class).id(CounterView.ID_VALUE_LABEL).getText());
  }
}

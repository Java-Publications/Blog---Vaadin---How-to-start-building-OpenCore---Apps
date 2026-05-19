package com.svenruppert.opencore.counter.extension;

import java.util.List;

public interface CounterEventFeature extends FeatureContribution {

  List<CounterEventListener> counterEventListeners();
}

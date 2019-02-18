/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.util.BackOff;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.Sleeper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class RetryConfig {

  private static final int INITIAL_INTERVAL_MILLIS = 500;

  private final List<Integer> retryStatusCodes;
  private final int maxRetries;
  private final Sleeper sleeper;
  private final ExponentialBackOff.Builder backOffBuilder;

  private RetryConfig(Builder builder) {
    if (builder.retryStatusCodes != null) {
      this.retryStatusCodes = ImmutableList.copyOf(builder.retryStatusCodes);
    } else {
      this.retryStatusCodes = ImmutableList.of();
    }

    checkArgument(builder.maxRetries >= 0, "maxRetries must not be negative");
    this.maxRetries = builder.maxRetries;
    this.sleeper = checkNotNull(builder.sleeper);
    this.backOffBuilder = new ExponentialBackOff.Builder()
        .setInitialIntervalMillis(INITIAL_INTERVAL_MILLIS)
        .setMaxIntervalMillis(builder.maxIntervalMillis)
        .setMultiplier(builder.backOffMultiplier)
        .setRandomizationFactor(0);

    // Force validation of arguments by building the BackOff object
    this.backOffBuilder.build();
  }

  List<Integer> getRetryStatusCodes() {
    return retryStatusCodes;
  }

  int getMaxRetries() {
    return maxRetries;
  }

  int getMaxIntervalMillis() {
    return backOffBuilder.getMaxIntervalMillis();
  }

  double getBackOffMultiplier() {
    return backOffBuilder.getMultiplier();
  }

  Sleeper getSleeper() {
    return sleeper;
  }

  BackOff newBackOff() {
    return backOffBuilder.build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private List<Integer> retryStatusCodes;
    private int maxRetries;
    private int maxIntervalMillis = (int) TimeUnit.MINUTES.toMillis(2);
    private double backOffMultiplier = 2.0;
    private Sleeper sleeper = Sleeper.DEFAULT;

    private Builder() { }

    public Builder setRetryStatusCodes(List<Integer> retryStatusCodes) {
      this.retryStatusCodes = retryStatusCodes;
      return this;
    }

    public Builder setMaxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
      return this;
    }

    public Builder setMaxIntervalMillis(int maxIntervalMillis) {
      this.maxIntervalMillis = maxIntervalMillis;
      return this;
    }

    public Builder setBackOffMultiplier(double backOffMultiplier) {
      this.backOffMultiplier = backOffMultiplier;
      return this;
    }

    @VisibleForTesting
    Builder setSleeper(Sleeper sleeper) {
      this.sleeper = sleeper;
      return this;
    }

    public RetryConfig build() {
      return new RetryConfig(this);
    }
  }
}

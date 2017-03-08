package com.apollographql.android.cache.normalized;

import com.apollographql.android.api.graphql.Mutation;
import com.apollographql.android.api.graphql.Operation;
import com.apollographql.android.api.graphql.Query;

import java.util.Map;

import javax.annotation.Nonnull;

public abstract class CacheKeyResolver {
  public static final CacheKeyResolver DEFAULT = new CacheKeyResolver() {
    @Nonnull @Override public CacheKey resolve(@Nonnull Map<String, Object> jsonObject) {
      return CacheKey.NO_KEY;
    }
  };
  private static final CacheKey QUERY_ROOT_KEY = CacheKey.from("QUERY_ROOT");
  private static final CacheKey MUTATION_ROOT_KEY = CacheKey.from("MUTATION_ROOT");

  public static CacheKey rootKeyForOperation(@Nonnull Operation operation) {
    if (operation instanceof Query) {
      return QUERY_ROOT_KEY;
    } else if (operation instanceof Mutation) {
      return MUTATION_ROOT_KEY;
    }
    throw new IllegalArgumentException("Unknown operation type.");
  }

  @Nonnull public abstract CacheKey resolve(@Nonnull Map<String, Object> jsonObject);
}

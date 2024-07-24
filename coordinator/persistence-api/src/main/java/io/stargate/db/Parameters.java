/*
 * Copyright The Stargate Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.stargate.db;

import static java.lang.String.format;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import javax.annotation.Nonnull;
import org.apache.cassandra.stargate.db.ConsistencyLevel;
import org.apache.cassandra.stargate.transport.ProtocolVersion;
import org.apache.cassandra.stargate.utils.MD5Digest;
import org.immutables.value.Value;

/** Parameters for the execution of requests in the {@link Persistence} API. */
@Value.Immutable(singleton = true)
public abstract class Parameters {
  private static final ConsistencyLevel DEFAULT_CONSISTENCY = ConsistencyLevel.ONE;
  private static final ProtocolVersion DEFAULT_PROTOCOL_VERSION = ProtocolVersion.CURRENT;

  /** A parameters instance using all defaults. */
  public static Parameters defaults() {
    return ImmutableParameters.of();
  }

  public static ImmutableParameters.Builder builder() {
    return ImmutableParameters.builder();
  }

  @Value.Check
  protected void validate() {
    if (pageSize().isPresent() && pageSize().getAsInt() <= 0) {
      throw new IllegalStateException(
          format("Invalid page size %d: must be strictly positive", pageSize().getAsInt()));
    }
  }

  /**
   * The protocol version used for the request. In particular, this must be the version with which
   * the values of the request ({@link Statement#values()}) are encoded, and will be the version
   * with which returned values will be encoded. Defaults to {@link ProtocolVersion#CURRENT}.
   */
  @Value.Default
  public ProtocolVersion protocolVersion() {
    return DEFAULT_PROTOCOL_VERSION;
  }

  /**
   * The optional page size, in rows, for the request. If unset, the request will not page. If set,
   * this must be a strictly positive number.
   */
  public abstract OptionalInt pageSize();

  /** The optional paging state to request the subsequent pages of a paged request. */
  public abstract Optional<ByteBuffer> pagingState();

  public abstract Optional<MD5Digest> resultSetMetadataId();

  /** The consistency level for the request. Defaults to ONE. */
  @Value.Default
  public ConsistencyLevel consistencyLevel() {
    return DEFAULT_CONSISTENCY;
  }

  /**
   * The optional serial consistency level for the request. This is optional in that only
   * conditional (LWT) requests use a serial consistency level. If unset but the request is a
   * conditional one, a default will be used but is unspecified and might depend of the {@link
   * Persistence} implementation.
   */
  public abstract Optional<ConsistencyLevel> serialConsistencyLevel();

  /**
   * The optional default timestamp to use for the request. If unset, the default timestamp will be
   * generated by the {@link Persistence} implementation.
   */
  public abstract OptionalLong defaultTimestamp();

  /**
   * The optional time to use a "now". If unset, the current time will be generated by the {@link
   * Persistence} implementation. This option is mainly meant for testing.
   */
  public abstract OptionalInt nowInSeconds();

  /**
   * The default keyspace to use for the request (only used if the request itself does not specify a
   * keyspace). If unset, the default keyspace used on the underlying {@link Persistence.Connection}
   * the request is made on will be used (and if none if use and the request does not specify a
   * keyspace, the request will error out).
   */
  public abstract Optional<String> defaultKeyspace();

  /** Custom payload that can be used by the underlying {@link Persistence} implementation. */
  public abstract Optional<Map<String, ByteBuffer>> customPayload();

  /**
   * Requests to not include metadata in the result of the request (can be used when paging to
   * potentially save a few cycles since the result metadata is the same for all pages). Not set by
   * default.
   */
  @Value.Default
  public boolean skipMetadataInResult() {
    return false;
  }

  /** Enables tracing for the request. Not set by default. */
  @Value.Default
  public boolean tracingRequested() {
    return false;
  }

  /**
   * Copy these parameters but with the {@link #consistencyLevel()} replaced by the provided one.
   */
  public Parameters withConsistencyLevel(ConsistencyLevel newConsistencyLevel) {
    return toBuilder().consistencyLevel(newConsistencyLevel).build();
  }

  /** Copy these parameters but with the {@link #pagingState()} ()} replaced by the provided one. */
  public Parameters withPagingState(@Nonnull ByteBuffer newPagingState) {
    return toBuilder().pagingState(newPagingState).build();
  }

  /** Copy these parameters but with {@link #skipMetadataInResult()} set. */
  public Parameters withoutMetadataInResult() {
    return toBuilder().skipMetadataInResult(true).build();
  }

  /** Copy these parameters but with {@link #resultSetMetadataId()} set. */
  public Parameters withResultSetMetadataId(MD5Digest resultSetMetadataId) {
    return toBuilder().resultSetMetadataId(resultSetMetadataId).build();
  }

  /** Creates a new parameters builder filled with the values of this builder. */
  public ImmutableParameters.Builder toBuilder() {
    return ImmutableParameters.builder().from(this);
  }

  @Override
  public String toString() {
    Map<String, String> m = new LinkedHashMap<>();
    if (consistencyLevel() != DEFAULT_CONSISTENCY) {
      m.put("consistency", consistencyLevel().toString());
    }
    if (serialConsistencyLevel().isPresent()) {
      m.put("serial_consistency", serialConsistencyLevel().get().toString());
    }
    if (protocolVersion() != DEFAULT_PROTOCOL_VERSION) {
      m.put("protocol", protocolVersion().toString());
    }
    if (pageSize().isPresent() || pagingState().isPresent()) {
      String size = pageSize().isPresent() ? Integer.toString(pageSize().getAsInt()) : "unlimited";
      String state = pagingState().isPresent() ? " (continuation)" : "";
      m.put("paging", format("%s%s", size, state));
    }
    if (customPayload().isPresent()) {
      m.put("custom_payload", format("%d entries", customPayload().get().size()));
    }
    if (skipMetadataInResult()) {
      m.put("skipMetadata", "true");
    }
    if (tracingRequested()) {
      m.put("tracing", "true");
    }
    if (defaultKeyspace().isPresent()) {
      m.put("keyspace", defaultKeyspace().get());
    }
    if (defaultTimestamp().isPresent()) {
      m.put("timestamp", Long.toString(defaultTimestamp().getAsLong()));
    }
    if (nowInSeconds().isPresent()) {
      m.put("now", Integer.toString(nowInSeconds().getAsInt()));
    }
    return m.toString();
  }
}

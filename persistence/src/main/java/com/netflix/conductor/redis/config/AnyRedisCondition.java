/*
 * Copyright 2020 Orkes, Inc.
 * <p>
 * Licensed under the Orkes Community License (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * https://github.com/orkes-io/licenses/blob/main/community/LICENSE.txt
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.netflix.conductor.redis.config;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class AnyRedisCondition extends AnyNestedCondition {

    public AnyRedisCondition() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(name = "conductor.db.type", havingValue = "dynomite")
    static class DynomiteClusterCondition {}

    @ConditionalOnProperty(name = "conductor.db.type", havingValue = "memory")
    static class InMemoryRedisCondition {}

    @ConditionalOnProperty(name = "conductor.db.type", havingValue = "redis_cluster")
    static class RedisClusterConfiguration {}

    @ConditionalOnProperty(name = "conductor.db.type", havingValue = "redis_sentinel")
    static class RedisSentinelConfiguration {}

    @ConditionalOnProperty(name = "conductor.db.type", havingValue = "redis_standalone")
    static class RedisStandaloneConfiguration {}
}

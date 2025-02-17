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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.conductor.redis.dynoqueue.ConfigurationHostSupplier;
import com.netflix.conductor.redis.jedis.JedisSentinel;
import com.netflix.dyno.connectionpool.Host;

import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "conductor.db.type", havingValue = "redis_sentinel")
public class RedisSentinelConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RedisSentinelConfiguration.class);

    @Bean
    protected JedisSentinel getJedisSentinel(RedisProperties properties) {
        GenericObjectPoolConfig<?> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMinIdle(properties.getMinIdleConnections());
        genericObjectPoolConfig.setMaxIdle(properties.getMaxIdleConnections());
        genericObjectPoolConfig.setMaxTotal(properties.getMaxConnectionsPerHost());
        genericObjectPoolConfig.setTestWhileIdle(properties.isTestWhileIdle());
        genericObjectPoolConfig.setMinEvictableIdleTimeMillis(
                properties.getMinEvictableIdleTimeMillis());
        genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(
                properties.getTimeBetweenEvictionRunsMillis());
        genericObjectPoolConfig.setNumTestsPerEvictionRun(properties.getNumTestsPerEvictionRun());
        ConfigurationHostSupplier hostSupplier = new ConfigurationHostSupplier(properties);

        log.info(
                "Starting conductor server using redis_sentinel and cluster "
                        + properties.getClusterName());
        Set<String> sentinels = new HashSet<>();
        for (Host host : hostSupplier.getHosts()) {
            sentinels.add(host.getHostName() + ":" + host.getPort());
        }
        // We use the password of the first sentinel host as password and sentinelPassword
        String password = getPassword(hostSupplier.getHosts());
        if (password != null) {
            return new JedisSentinel(
                    new JedisSentinelPool(
                            properties.getClusterName(),
                            sentinels,
                            genericObjectPoolConfig,
                            Protocol.DEFAULT_TIMEOUT,
                            Protocol.DEFAULT_TIMEOUT,
                            password,
                            properties.getDatabase(),
                            null,
                            Protocol.DEFAULT_TIMEOUT,
                            Protocol.DEFAULT_TIMEOUT,
                            password,
                            null));
        } else {
            return new JedisSentinel(
                    new JedisSentinelPool(
                            properties.getClusterName(),
                            sentinels,
                            genericObjectPoolConfig,
                            Protocol.DEFAULT_TIMEOUT,
                            null,
                            properties.getDatabase()));
        }
    }

    private String getPassword(List<Host> hosts) {
        return hosts.isEmpty() ? null : hosts.get(0).getPassword();
    }
}

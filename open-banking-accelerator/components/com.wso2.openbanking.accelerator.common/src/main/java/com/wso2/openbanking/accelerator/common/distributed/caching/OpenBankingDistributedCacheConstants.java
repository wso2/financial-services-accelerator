/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.accelerator.common.distributed.caching;

/**
 * Open banking distributed cache constants.
 */
public class OpenBankingDistributedCacheConstants {

    // Distributed cache cluster name.
    public static final String CLUSTER_NAME = "OB_DISTRIBUTED_CACHE";

    // Common constants for both TCP and Multicast.
    public static final String ENABLED = "DistributedCache.Enabled";
    public static final String HOST_NAME = "DistributedCache.HostName";
    public static final String PORT = "DistributedCache.Port";
    public static final String DISCOVERY_MECHANISM = "DistributedCache.DiscoveryMechanism";

    // Constants used for Multicast.
    public static final String MULTICAST = "Multicast";
    public static final String MULTICAST_GROUP = "DistributedCache.MulticastGroup";
    public static final String MULTICAST_PORT = "DistributedCache.MulticastPort";
    public static final String TRUSTED_INTERFACES = "DistributedCache.TrustedInterfaces.TrustedInterface";

    // Constants used for TCP.
    public static final String TCP = "TCP";
    public static final String MEMBERS = "DistributedCache.Members.Member";

    // Constants for hazelcast properties.
    public static final String PROPERTY_MAX_HEARTBEAT = "DistributedCache.Properties.MaxHeartbeat";
    public static final String PROPERTY_MAX_MASTER_CONFIRMATION = "DistributedCache.Properties.MasterConfirmation";
    public static final String PROPERTY_MERGE_FIRST_RUN_DELAY = "DistributedCache.Properties.MergeFirstRunDelay";
    public static final String PROPERTY_MERGE_NEXT_RUN_DELAY = "DistributedCache.Properties.MergeNextRunDelay";
    public static final String PROPERTY_LOGGING_TYPE = "DistributedCache.Properties.LoggingType";

    // Hazelcast Constants for hazelcast properties.
    public static final String HAZELCAST_PROPERTY_MAX_HEARTBEAT = "hazelcast.max.no.heartbeat.seconds";
    public static final String HAZELCAST_PROPERTY_MAX_MASTER_CONFIRMATION = "hazelcast.max.no.master." +
            "confirmation.seconds";
    public static final String HAZELCAST_PROPERTY_MERGE_FIRST_RUN_DELAY = "hazelcast.merge.first.run.delay.seconds";
    public static final String HAZELCAST_PROPERTY_MERGE_NEXT_RUN_DELAY = "hazelcast.merge.next.run.delay.seconds";
    public static final String HAZELCAST_PROPERTY_LOGGING_TYPE = "hazelcast.logging.type";
}

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

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.SecurityUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.CLUSTER_NAME;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.DISCOVERY_MECHANISM;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.ENABLED;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.HAZELCAST_PROPERTY_LOGGING_TYPE;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.HAZELCAST_PROPERTY_MAX_HEARTBEAT;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.HAZELCAST_PROPERTY_MAX_MASTER_CONFIRMATION;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.HAZELCAST_PROPERTY_MERGE_FIRST_RUN_DELAY;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.HAZELCAST_PROPERTY_MERGE_NEXT_RUN_DELAY;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.HOST_NAME;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.MEMBERS;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.MULTICAST;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.MULTICAST_GROUP;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.MULTICAST_PORT;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.PORT;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.PROPERTY_LOGGING_TYPE;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.PROPERTY_MAX_HEARTBEAT;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.PROPERTY_MAX_MASTER_CONFIRMATION;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.PROPERTY_MERGE_FIRST_RUN_DELAY;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.PROPERTY_MERGE_NEXT_RUN_DELAY;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.TCP;
import static com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants.TRUSTED_INTERFACES;

/**
 * Singleton class to create a hazelcast cluster member.
 */
public class OpenBankingDistributedMember {
    private boolean enabled;
    private static volatile OpenBankingDistributedMember openBankingDistributedMember;
    private final HazelcastInstance hazelcastInstance;
    private static final Map<String, Object> configurations = OpenBankingConfigParser.getInstance().getConfiguration();

    private static final Log log = LogFactory.getLog(OpenBankingDistributedMember.class);

    /**
     * Private constructor.
     */
    private OpenBankingDistributedMember() {
        setEnabled();

        Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName(CLUSTER_NAME);

        setProperties(hazelcastConfig);

        NetworkConfig network = hazelcastConfig.getNetworkConfig();

        setNetworkConfigurations(network);

        this.hazelcastInstance = Hazelcast.newHazelcastInstance(hazelcastConfig);
    }

    /**
     * Method to get the singleton object.
     *
     * @return distributedMember.
     */
    public static OpenBankingDistributedMember of() {
        if (openBankingDistributedMember == null) {
            synchronized (OpenBankingDistributedMember.class) {
                if (openBankingDistributedMember == null) {
                    openBankingDistributedMember = new OpenBankingDistributedMember();
                }
            }
        }
        return openBankingDistributedMember;
    }

    /**
     * Method to destroy the singleton instance.
     */
    public static synchronized void shutdown() {
        openBankingDistributedMember.getHazelcastInstance().shutdown();
        openBankingDistributedMember = null;
        log.debug("Shutdown distributed cache member.");
    }

    /**
     * Getter for enabled.
     *
     * @return Boolean enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Setter for enabled, using the config file.
     */
    public void setEnabled() {
        Object isEnableConfiguration = configurations.get(ENABLED);
        if (isEnableConfiguration != null) {
            String isEnableConfigurationString = isEnableConfiguration.toString();
            setEnabled(isEnableConfigurationString.equals("true"));
        }
    }

    /**
     * Setter of enabled.
     *
     * @param enabled Boolean enabled.
     */
    public void setEnabled(boolean enabled) {
        if (enabled) {
            log.debug("Distributed Caching enabled");
        } else {
            log.debug("Distributed Caching disabled");
        }
        this.enabled = enabled;
    }

    /**
     * Getter for hazelcast instance.
     *
     * @return this.hazelcastInstance.
     */
    public HazelcastInstance getHazelcastInstance() {
        return this.hazelcastInstance;
    }

    /**
     * Method to set hazelcast properties.
     *
     * @param hazelcastConfig hazelcastConfig.
     */
    @SuppressFBWarnings("CRLF_INJECTION_LOGS")
    // Suppressed content - hazelcastConfig.getProperties()
    // Suppression reason - Warning are appearing on Properties, not strings. Also the properties will be set in config
    //                      by the admin
    // Suppressed warning count - 1
    private synchronized void setProperties(Config hazelcastConfig) {

        Properties hazelcastProperties = new Properties();

        setProperty(hazelcastProperties, PROPERTY_MAX_HEARTBEAT, HAZELCAST_PROPERTY_MAX_HEARTBEAT);
        setProperty(hazelcastProperties, PROPERTY_MAX_MASTER_CONFIRMATION, HAZELCAST_PROPERTY_MAX_MASTER_CONFIRMATION);
        setProperty(hazelcastProperties, PROPERTY_MERGE_FIRST_RUN_DELAY, HAZELCAST_PROPERTY_MERGE_FIRST_RUN_DELAY);
        setProperty(hazelcastProperties, PROPERTY_MERGE_NEXT_RUN_DELAY, HAZELCAST_PROPERTY_MERGE_NEXT_RUN_DELAY);
        setProperty(hazelcastProperties, PROPERTY_LOGGING_TYPE, HAZELCAST_PROPERTY_LOGGING_TYPE);

        hazelcastConfig.setProperties(hazelcastProperties);

        if (log.isDebugEnabled()) {
            log.debug("Hazelcast Properties : " + hazelcastConfig.getProperties());
        }
    }

    /**
     * Method to set hazelcast property.
     *
     * @param property          Property.
     * @param configurationName Name of the configuration in config file.
     * @param hazelcastProperty hazelcast configuration.
     */
    private void setProperty(Properties property, String configurationName, String hazelcastProperty) {
        Object configuration = configurations.get(configurationName);
        if (configuration != null) {
            String configurationString = configuration.toString();
            property.setProperty(hazelcastProperty, configurationString);
        }
    }

    /**
     * Method to set hazelcast network configurations.
     *
     * @param network network.
     */
    private synchronized void setNetworkConfigurations(NetworkConfig network) {

        // Configuring host name of the hazelcast instance.
        Object hostName = configurations.get(HOST_NAME);
        if (hostName != null) {
            String hostNameString = hostName.toString();
            network.setPublicAddress(hostNameString);
        }

        // Configuring port of the hazelcast instance.
        Object port = configurations.get(PORT);
        if (port != null) {
            String portString = port.toString();
            int portInt = Integer.parseInt(portString);
            network.setPort(portInt);
        }

        if (log.isDebugEnabled()) {
            log.debug("Network is set to " + network.getPublicAddress().replaceAll("[\r\n]", "") + ":" +
                    network.getPort());
        }

        // Configuring the discovery mechanism of the hazelcast instance.
        JoinConfig join = network.getJoin();
        Object discoveryMechanism = configurations.get(DISCOVERY_MECHANISM);
        if (discoveryMechanism != null) { // When discovery method is configured.

            String discoveryMechanismString = discoveryMechanism.toString();

            if (discoveryMechanismString.equals(TCP)) {
                // Discovery method TCP.
                setConfigurationsTCP(join);
            } else if (discoveryMechanismString.equals(MULTICAST)) {
                // Discovery method Multicast.
                setConfigurationsMulticast(join);
            }
        } else { // Defaulting Multicast when discovery method is not configured.
            setConfigurationsMulticast(join);
        }
    }

    /**
     * Method to set discovery mechanism TCP.
     *
     * @param join JoinConfig join.
     */
    @SuppressFBWarnings("CRLF_INJECTION_LOGS")
    // Suppressed content - tcpipConfig.getMembers()
    // Suppression reason - False positive: New lines are already removed
    // Suppressed warning count - 1
    private void setConfigurationsTCP(JoinConfig join) {
        log.debug("Discovery mechanism : TCP");
        join.getMulticastConfig().setEnabled(false);
        TcpIpConfig tcpipConfig = join.getTcpIpConfig();
        tcpipConfig.setEnabled(true);

        // Configuring TCP members.
        Object members = configurations.get(MEMBERS);
        if (members != null) {
            ArrayList<String> membersList = new ArrayList<>();
            if (members instanceof ArrayList) {
                membersList.addAll((ArrayList) members);
            } else if (members instanceof String) {
                membersList.add((String) members);
            }
            for (String member : membersList) {
                tcpipConfig.addMember(member.trim());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Members: " + SecurityUtils.sanitize(tcpipConfig.getMembers()));
        }
    }

    /**
     * Method to set discovery mechanism Multicast.
     *
     * @param join JoinConfig join.
     */
    @SuppressFBWarnings("CRLF_INJECTION_LOGS")
    // Suppressed content - multicastConfig.getTrustedInterfaces()
    // Suppression reason - False positive: New lines are already removed
    // Suppressed warning count - 1
    private void setConfigurationsMulticast(JoinConfig join) {
        log.debug("Discovery mechanism : Multicast");
        join.getTcpIpConfig().setEnabled(false);
        MulticastConfig multicastConfig = join.getMulticastConfig();
        multicastConfig.setEnabled(true);

        // Configuring multicast group.
        Object multicastGroup = configurations.get(MULTICAST_GROUP);
        if (multicastGroup != null) {
            String multicastGroupString = multicastGroup.toString();
            multicastConfig.setMulticastGroup(multicastGroupString);
        }

        // Configuring multicast port.
        Object multicastPort = configurations.get(MULTICAST_PORT);
        if (multicastPort != null) {
            String multicastPortString = multicastPort.toString();
            int multicastPortInt = Integer.parseInt(multicastPortString);
            multicastConfig.setMulticastPort(multicastPortInt);
        }

        if (log.isDebugEnabled()) {
            log.debug("Discovery mechanism is set to Multicast.\n\tMulticast Group: " +
                    multicastConfig.getMulticastGroup().replaceAll("[\r\n]", "") +
                    "\n\tMulticast Port: " + multicastConfig.getMulticastPort());
        }
        // Configuring trusted interfaces.
        Object trustedInterfaces = configurations.get(TRUSTED_INTERFACES);
        if (trustedInterfaces != null) {
            ArrayList<String> trustedInterfacesList = new ArrayList<>();
            if (trustedInterfaces instanceof ArrayList) {
                trustedInterfacesList.addAll((ArrayList) trustedInterfaces);
            } else if (trustedInterfaces instanceof String) {
                trustedInterfacesList.add((String) trustedInterfaces);
            }
            for (String trustedInterface : trustedInterfacesList) {
                multicastConfig.addTrustedInterface(trustedInterface.trim());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("\n\tTrusted Interfaces: " + SecurityUtils.sanitize(multicastConfig.getTrustedInterfaces()));
        }
    }
}

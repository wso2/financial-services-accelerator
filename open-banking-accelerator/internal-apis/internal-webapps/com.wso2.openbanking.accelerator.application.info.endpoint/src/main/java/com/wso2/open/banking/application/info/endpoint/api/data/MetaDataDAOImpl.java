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

package com.wso2.open.banking.application.info.endpoint.api.data;

import com.google.common.collect.ImmutableMap;
import com.wso2.open.banking.application.info.endpoint.api.constants.MetaDataSQLStatements;
import com.wso2.openbanking.accelerator.common.persistence.JDBCPersistenceManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;

/**
 * MetaDataDAOImpl.
 *
 * <p>This specifies a DAO Impl for retrieving all client id's for the consent manager application
 */
public class MetaDataDAOImpl {

    private static final Log log = LogFactory.getLog(MetaDataDAOImpl.class);

    /**
     * Returns the List of distinct clientIds from the consent table.
     *
     * @return
     */
    public List<String> getAllDistinctClientIds() {

        MetaDataSQLStatements sqlStatements = new MetaDataSQLStatements();
        final String initialConsentRequest = sqlStatements.getAllClientIds();
        List<String> clientIdList = new ArrayList<>();

        try (Connection connection = JDBCPersistenceManager.getInstance().getDBConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(initialConsentRequest)) {

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    clientIdList.add(rs.getString(1));
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("ClientIds %s provided for bulk retrieval",
                        Arrays.toString(clientIdList.toArray())));
            }

            return clientIdList;
        } catch (SQLException e) {
            log.error("Error occurred while retrieving ClientIds.", e);
            Map<String, String> error = ImmutableMap.of(
                    "error", "Error occurred while retrieving ClientIds");

            throw new InternalServerErrorException(Response.status
                    (Response.Status.INTERNAL_SERVER_ERROR).entity(error).build());
        }

    }

}

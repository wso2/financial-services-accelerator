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

package com.wso2.openbanking.accelerator.identity.app2app.validations.validationorder;

import com.wso2.openbanking.accelerator.identity.common.annotations.validationgroups.MandatoryChecks;
import com.wso2.openbanking.accelerator.identity.common.annotations.validationgroups.SignatureCheck;
import com.wso2.openbanking.accelerator.identity.common.annotations.validationgroups.ValidityChecks;

import javax.validation.GroupSequence;

/**
 * Class to define the order of execution for the hibernate validation groups.
 */
@GroupSequence({SignatureCheck.class , MandatoryChecks.class, ValidityChecks.class})
public interface App2AppValidationOrder {

}


/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

function denyConsent() {
    updateAccountNamesFromPermissions();
    document.getElementById('consent').value = false;
    document.getElementById("oauth2_authz_confirm").submit();
}

// Confirm sharing data
function approvedConsent() {
    updateAccountNamesFromPermissions();
    document.getElementById('consent').value = true;
    document.getElementById("oauth2_authz_confirm").submit();
}

// Set permission uids as names for account inputs before submission
function updateAccountNamesFromPermissions() {
    const hiddenPermissions = document.querySelectorAll('input[type="hidden"][name^="permission-"]');

    hiddenPermissions.forEach(permissionInput => {
        const nameParts = permissionInput.name.split('-');
        const index = nameParts[1];
        const newName = permissionInput.value;

        // Find corresponding account elements
        const accountElements = document.querySelectorAll(`[name="accounts-${index}"]`);
        accountElements.forEach(accountChoice => {
            accountChoice.setAttribute('name', newName);
        });

        // Remove the permission input
        permissionInput.remove();
    });

    // Handle unindexed "accounts"
    const standalonePermission = document.querySelector('input[type="hidden"][name="permission"]');
    const standaloneAccounts = document.querySelectorAll('[name="accounts"]');
    if (standalonePermission && standaloneAccount.length > 0) {
        standaloneAccounts.forEach(standaloneAccount => {
            standaloneAccount.setAttribute('name', standalonePermission.value);
        });
        standalonePermission.remove();
    }
}

function approvedDefaultClaim() {
    var mandatoryClaimCBs = $(".mandatory-claim");
    var checkedMandatoryClaimCBs = $(".mandatory-claim:checked");
    var scopeApproval = $("input[name='scope-approval']");

    // If scope approval radio button is rendered then we need to validate that it's checked
    if (scopeApproval.length > 0) {
        if (scopeApproval.is(":checked")) {
            var checkScopeConsent = $("input[name='scope-approval']:checked");
            $('#consent').val(checkScopeConsent.val());
        } else {
            $("#modal_scope_validation").modal();
            return;
        }
    } else {
        // Scope radio button was not rendered therefore set the consent to 'approve'
        document.getElementById('consent').value = "approve";
    }

    if (checkedMandatoryClaimCBs.length === mandatoryClaimCBs.length) {
        document.getElementById("profile").submit();
    } else {
        $("#modal_claim_validation").modal();
    }
}

function denyDefaultClaim() {
    document.getElementById('consent').value = "deny";
    document.getElementById("profile").submit();
}

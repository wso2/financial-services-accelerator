/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

import React, {useEffect} from "react";
import "../css/Popup.css";
import QRCode  from "qrcode.react";
import { getAccessToken } from "../data/User";
import { DeviceRegistrationContext } from "../context/DeviceRegistrationContext";
import { useContext } from "react";

export const QRButton = (props) => {
  const {deviceRegistrationContextData,getDeviceRegistrationContextInfo} = useContext(DeviceRegistrationContext);
  const deviceRegistrationData = deviceRegistrationContextData.deviceRegistrationData;

  useEffect(() => {
    const accessToken = getAccessToken();
    getDeviceRegistrationContextInfo(accessToken);
    const dropDownMenu = document.getElementsByClassName("dropdown-menu")[0];
    dropDownMenu.style.opacity = 0;
    dropDownMenu.style.pointerEvents = 'none';
    }, []);

  return (
    <span>
        <div className="device-registration">
          <h1 className="device-registration-header">Register device</h1>
          <p className="device-registration-content">Please scan the QR code to register your device</p>
          <div className="device-registration-code">
            <QRCode value={JSON.stringify(deviceRegistrationData)}  size="256"/>
          </div>
        </div>
    </span>
   );
};

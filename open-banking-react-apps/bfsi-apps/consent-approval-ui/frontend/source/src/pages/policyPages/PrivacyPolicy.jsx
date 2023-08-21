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

import React, { useEffect } from "react";
import {Link} from "react-router-dom";

import {FormattedMessage} from "react-intl";

import Header from "../../components/Header.jsx";
import Footer from "../../components/Footer.jsx";

import { content } from "../../utils/functions.js";

import "../../styles/Policy.scss";

/**
 * Privacy Policy page.
 */
const PrivacyPolicy = () => {

    useEffect(() => {
        content("#privacyPolicy");
    }, []);

    return (
        <>
            <Header />
            <div className="policy-container">
                <div className="toc">
                    <h4>On this page</h4>
                    <nav role='navigation' id='table-of-contents'></nav>
                </div>
                <div className="policy">
                    {/* page content */}
                    <h1>
                        <FormattedMessage
                            id="wso2.open.banking"
                            defaultMessage="WSO2 Open Banking"
                        />
                        &nbsp;-&nbsp;
                        <FormattedMessage
                            id="privacy.policy"
                            defaultMessage="Privacy Policy"
                        />
                    </h1>
                    {/* Customizable content. Due to this nature, i18n is not implemented for this section */}
                    <div id="privacyPolicy">
                        <section>
                            <h4><a href="https://wso2.com/solutions/financial/open-banking/">About WSO2 Open Banking
                                Solution</a></h4>
                            <p>WSO2 Open Banking Solution (referred to as &ldquo;WSO2 Open Banking&rdquo;
                                within this policy) is a comprehensive Open Banking Solution that is supporting PSD2
                                compliance along with more value added features.</p>
                        </section>
                        <section>
                            <h2 id="privacy-policy">Privacy Policy</h2>
                            <p>This policy describes how WSO2 Open Banking captures your personal information, the
                                purposes of collection, and information about the retention of your personal
                                information.</p>
                            <p>Please note that this policy is for reference only, and is applicable for the software
                                as a product. WSO2 Inc. and its developers have no access to the information held within
                                WSO2 Open Banking. Please see the
                                <a href="/consentapproval/policy/privacy#disclaimer"> Disclaimer</a> section for
                                more information</p>
                            <p>Entities, organisations or individuals controlling the use and administration of WSO2
                                Open Banking should create their own privacy policies setting out the manner in which
                                data is controlled or processed by the respective entity, organisation or individual.
                            </p>
                        </section>
                        <section>
                            <h2 id="what-is-personal-information">What is personal information?</h2>
                            <p>WSO2 Open Banking considers anything related to you, and by which you may be identified,
                                as your personal information. This includes, but is not limited to:</p>
                            <ul>
                                <li>Your user name (except in cases where the user name created by your employer is
                                    under contract)</li>
                                <li>Your date of birth/age</li>
                                <li>IP address used to log in</li>
                                <li>Your device ID if you use a device (e.g., phone or tablet) to log in</li>
                            </ul>
                            <p>However, WSO2 Open Banking also collects the following information that is not considered
                                personal information, but is used only for <strong>statistical</strong> purposes. The
                                reason for this is that this information can not be used to track you.</p>
                            <ul>
                                <li>City/Country from which you originated the TCP/IP connection</li>
                                <li>Time of the day that you logged in (year, month, week, hour or minute)</li>
                                <li>Type of device that you used to log in (e.g., phone or tablet)</li>
                                <li>Operating system and generic browser information</li>
                            </ul>
                        </section>
                        <section>
                            <h2 id="collection-of-personal-information">Collection of personal information</h2>
                            <p>WSO2 Open Banking collects your information only to serve your access requirements.
                                For example:</p>
                            <ul>
                                <li>WSO2 Open Banking uses your IP address to detect any suspicious login attempts to
                                    your account.</li>
                                <li>WSO2 Open Banking uses attributes like your first name, last name, etc., to provide
                                    a rich and personalized user experience.</li>
                                <li>WSO2 Open Banking uses your security questions and answers only to allow account
                                    recovery.</li>
                            </ul>
                        </section>
                        <section >
                            <h3 id="tracking-technologies">Tracking Technologies</h3>
                            <p>WSO2 Open Banking collects your information by:</p>
                            <ul>
                                <li>Collecting information from the user profile page where you enter your personal
                                    data.</li>
                                <li>Tracking your IP address with HTTP request, HTTP headers, and TCP/IP.</li>
                                <li>Tracking your geographic information with the IP address.</li>
                                <li>Tracking your login history with browser cookies. Please see our&nbsp;
                                    <Link to='/policy/cookie' className="link">
                                        cookie policy</Link> for more information.</li>
                            </ul>
                        </section>
                        <section>
                            <h2 id="user-of-personal-information">Use of personal information</h2>
                            <p>WSO2 Open Banking will only use your personal information for the purposes for which it
                                was collected (or for a use identified as consistent with that purpose).</p>
                            <p>WSO2 Open Banking uses your personal information only for the following purposes.</p>
                            <ul>
                                <li>To provide you with a personalized user experience. WSO2 Open Banking uses your
                                    name and uploaded profile pictures for this purpose.</li>
                                <li>To protect your account from unauthorized access or potential hacking attempts.
                                    WSO2 Open Banking uses HTTP or TCP/IP Headers for this purpose.</li>
                                <ul>
                                    <li>This includes:</li>
                                    <ul>
                                        <li>IP address</li>
                                        <li>Browser fingerprinting</li>
                                        <li>Cookies</li>
                                    </ul>
                                </ul>
                                <li>Derive statistical data for analytical purposes on system performance improvements.
                                    WSO2 Open Banking will not keep any personal information after statistical
                                    calculations. Therefore, the statistical report has no means of identifying an
                                    individual person.</li>
                                <ul>
                                    <li>WSO2 Open Banking may use:</li>
                                    <ul>
                                        <li>IP Address to derive geographic information</li>
                                        <li>Browser fingerprinting to determine the browser
                                            technology or/and version</li>
                                    </ul>
                                </ul>
                            </ul>
                        </section>
                        <section>
                            <h2 id="disclosure-of-personal-information">Disclosure of personal information</h2>
                            <p>WSO2 Open Banking only discloses personal information to the relevant applications
                                (also known as “Payment Service Providers (PSPs)”) that are registered with WSO2
                                Open Banking. These applications are registered by the identity administrator of your
                                entity or organization. Personal information is disclosed only for the purposes for
                                which it was collected (or for a use identified as consistent with that purpose),
                                as controlled by such Service Providers, unless you have consented otherwise or where
                                it is required by law.</p>
                        </section>
                        <section>
                            <h3 id="legal-process">Legal process</h3>
                            <p>Please note that the organisation, entity or individual running WSO2 Open Banking may be
                                compelled to disclose your personal information with or without your consent when it is
                                required by law following due and lawful process.</p>
                        </section>
                        <section>
                            <h2 id="storage-of-personal-information">Storage of personal information</h2>
                        </section>
                        <section>
                            <h3 id="where-your-personal-information-stored">
                                Where your personal information is stored</h3>
                            <p>WSO2 Open Banking stores your personal information in secured databases. WSO2
                                Open Banking exercises proper industry accepted security measures to protect the
                                database where your personal information is held. WSO2 Open Banking as a product does
                                not transfer or share your data with any third parties or locations. </p>
                            <p>WSO2 Open Banking may use encryption to keep your personal data with an added level of
                                security.</p>
                        </section>
                        <section>
                            <h3 id="how-long-does-is-5.5-keep-your-personal-information">How long your personal
                                information is retained</h3>
                            <p>WSO2 Open Banking retains your personal data as long as you are an active user of our
                                system. You can update your personal data at any time using the given self-care
                                user portals.</p>
                            <p>WSO2 Open Banking may keep hashed secrets to provide you with an added level of security.
                                This includes:</p>
                            <ul>
                                <li>Current password</li>
                                <li>Previously used passwords</li>
                            </ul>
                        </section>
                        <section>
                            <h3 id="how-to-request-removal-of-your-personal-information">How to request removal of
                                your personal information</h3>
                            <p>You can request the administrator to delete your account. The administrator is the
                                administrator of the tenant you are registered under, or the super-administrator if
                                you do not use the tenant feature.</p>
                            <p>Additionally, you can request to anonymize all traces of your activities that WSO2
                                Open Banking may have retained in logs, databases or analytical storage.</p>
                        </section>
                        <section>
                            <h2 id="more-information">More information</h2>
                            <div></div>
                        </section>
                        <section>
                            <h3 id="changes-to-this-policy">Changes to this policy</h3>
                            <p>Upgraded versions of WSO2 Open Banking may contain changes to this policy and
                                revisions to this policy will be packaged within such upgrades. Such changes would
                                only apply to users who choose to use upgraded versions.</p>
                            <p>The organization running WSO2 Open Banking may revise the Privacy Policy from time to
                                time. You can find the most recent governing policy with the respective link provided
                                by the organization running WSO2 IS 5.5. The organization will notify any changes to
                                the privacy policy over our oficial public channels.</p>
                        </section>
                        <section>
                            <h3 id="your-choices">Your choices</h3>
                            <p>If you are already have a user account within WSO2 Open Banking, you have the right
                                to deactivate your account if you find that this privacy policy is unacceptable
                                to you.</p>
                            <p>If you do not have an account and you do not agree with our privacy policy, you can
                                chose not to create one.</p>
                        </section>
                        <section>
                            <h3 id="contact-us">Contact us</h3>
                            <p>Please contact WSO2 if you have any question or concerns regarding this
                                privacy policy.</p>
                            <p><a href="https://wso2.com/contact/">https://wso2.com/contact/</a></p>
                        </section>
                        <section>
                            <h2 id="disclaimer">Disclaimer</h2>
                            <ol>
                                <li>WSO2, its employees, partners, and affiliates do not have access to and do not
                                    require, store, process or control any of the data, including personal data
                                    contained in WSO2 Open Banking. All data, including personal data is controlled
                                    and processed by the entity or individual running WSO2 Open Banking.  WSO2, its
                                    employees partners and affiliates are not a data processor or a data controller
                                    within the meaning of any data privacy regulations.  WSO2 does not provide any
                                    warranties or undertake any responsibility or liability in connection with the
                                    lawfulness or the manner and purposes for which WSO2 Open Banking is used by such
                                    entities or persons.<br /></li>
                                <li>This privacy policy is for the informational purposes of the entity or persons
                                    running WOS2 IS 5.5.0 and sets out the processes and functionality contained within
                                    WSO2 Open Banking regarding personal data protection. It is the responsibility of
                                    entities and persons running WSO2 Open Banking to create and administer its own
                                    rules and processes governing users’ personal data, and such  rules and processes
                                    may change the use, storage and disclosure policies contained herein. Therefore
                                    users should consult the entity or persons running WSO2 Open Banking for its own
                                    privacy policy for details governing users’ personal data. </li>
                            </ol>
                        </section>
                    </div>
                    {/* /Costomizable content */}
                </div>
            </div>
            <Footer />
        </>
    );
};

export default PrivacyPolicy;

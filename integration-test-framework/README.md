# Integration Test Framework (integration-test-framework)

The Integration Test Framework is built as a common test framework for the BFSI domain. We have used layered architecture
in order to  archive this extensibility. Basically, the integration test framework consists of four main layers as follows.

- Level 1 - Configuration of the system under test
- Level 2 - Common function layer
- Level 3 - Domain Specific Capabilities layer
- Level 4 - Test Layer

Level 3 and Level 4 can be implemented by the user by using the Integration Test Framework to write specific test scenarios. 
This framework is used to write test cases for the all the specifications such as UK, CDS, NextGenPSD2 (Berlin), etc.

The test framework contains 2 modules which are as follows:
- [bfsi-test-framework](bfsi-test-framework) - Implementation of the common functional layer of the test framework 
architecture. It contains supportive features for the Open-Banking, Financial Services and Insurance domains. 
This module is placed in the “Financial-Open-Banking” repository, under the Integration test framework module.

- [open-banking-test-framework](open-banking-test-framework) - This layer is implemented as the lower section of the 
Domain specific capabilities layer. This module contains commonly used features for every Open-banking specification 
and “BFSI Test Framework” module is added to this module as a dependency.

# How to use integration-test-framework to write test cases.
1. Build the integration-test-framework module and install the jar.
2. Create a new maven project for your test scenarios and add the integration-test-framework jar as a dependency.
3. Create a new class and extend the BaseTest class.
4. Write your test scenarios in the new class.

/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.testobserve;

import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BalServer;
import org.ballerinalang.test.context.Utils;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Test cases for listener written to be used in unit tests.
 */
public class ListenerEndpointTest {

    private static BalServer balServer;
    private static BServerInstance servicesServerInstance;

    private static final String SERVICE_BASE_URL = "http://localhost:29091/testServiceOne";
    private static final Logger LOGGER = Logger.getLogger(ListenerEndpointTest.class.getName());

    @BeforeClass
    private void setup() throws Exception {

        balServer = new BalServer();

        // Don't use 9898 port here. It is used in metrics test cases.
        servicesServerInstance = new BServerInstance(balServer);

        String sourcesDir = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "listener_tests").getAbsolutePath();
        int[] requiredPorts = {29091};
        servicesServerInstance.startServer(sourcesDir, "listener_tests", null, new String[0], requiredPorts);
        Utils.waitForPortsToOpen(requiredPorts, 1000 * 60, false, InetAddress.getByName("localhost"));
    }

    @AfterClass
    private void cleanup() throws Exception {

        Path ballerinaInternalLog = Paths.get(balServer.getServerHome(), "ballerina-internal.log");
        if (Files.exists(ballerinaInternalLog)) {
            LOGGER.severe("=== Ballerina Internal Log Start ===");
            Files.lines(ballerinaInternalLog).forEach(LOGGER::severe);
            LOGGER.severe("=== Ballerina Internal Log End ===");
        }
        servicesServerInstance.removeAllLeechers();
        servicesServerInstance.shutdownServer();
        balServer.cleanup();
    }

    @Test
    public void testHelloWorldResponse() throws Exception {

        HttpResponse httpResponse = HttpClientRequest.doPost(SERVICE_BASE_URL + "/resourceOne",
                "dummy-ignored-input-1", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Hello from Resource One");
    }

    @Test
    public void testSuccessfulResponse() throws Exception {

        HttpResponse httpResponse = HttpClientRequest.doPost(SERVICE_BASE_URL + "/resourceTwo",
                "10", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Sum of numbers: 55");
    }

    @Test
    public void testErrorReturnResponse() throws Exception {

        HttpResponse httpResponse = HttpClientRequest.doPost(SERVICE_BASE_URL + "/resourceTwo",
                "invalid-number", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 500);
        Assert.assertEquals(httpResponse.getData(), "{ballerina/lang.int}NumberParsingError");
    }

    @Test
    public void testPanicResponse() throws Exception {

        HttpResponse httpResponse = HttpClientRequest.doPost(SERVICE_BASE_URL + "/resourceThree",
                "dummy-ignored-input-2", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 500);
        Assert.assertEquals(httpResponse.getData(), "Test Error");
    }
}

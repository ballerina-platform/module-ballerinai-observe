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

package io.ballerina.stdlib.observability;

import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BalServer;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.CONFIG_FILES_ENV_VARIABLE;

/**
 * Base Test Case of all Observability related test cases.
 */
public class ObservabilityBaseTest extends BaseTest {

    private static BServerInstance servicesServerInstance;

    protected static final String SERVER_CONNECTOR_NAME = "testobserve_listener";
    private static final Logger LOGGER = Logger.getLogger(ObservabilityBaseTest.class.getName());
    private static BalServer balServer;

    protected void setupServer(String testProject, String packageName, int[] requiredPorts) throws Exception {
        balServer = new BalServer();

        String sourcesDir = Paths.get("src", "test", "resources", "observability", testProject).toFile()
                .getAbsolutePath();

        String configFile = Paths.get("src", "test", "resources", "observability", testProject,
                "Config.toml").toFile().getAbsolutePath();
        Map<String, String> env = new HashMap<>();
        env.put(CONFIG_FILES_ENV_VARIABLE, configFile);

        // Don't use 9898 port here. It is used in metrics test cases.
        servicesServerInstance = new BServerInstance(balServer);
        servicesServerInstance.startServer(sourcesDir, packageName, null, null, env, requiredPorts);
        InetAddress address = InetAddress.getByName("localhost");
        Utils.waitForPortsToOpen(requiredPorts, 1000 * 60, false, address);
    }

    protected void cleanupServer() throws BallerinaTestException, IOException {
        Path ballerinaInternalLog = Paths.get(balServer.getServerHome(), "ballerina-internal.log");
        if (Files.exists(ballerinaInternalLog)) {
            LOGGER.severe("=== Ballerina Internal Log Start ===");
            Files.lines(ballerinaInternalLog).forEach(LOGGER::severe);
            LOGGER.severe("=== Ballerina Internal Log End ===");
        }
        servicesServerInstance.removeAllLeechers();
        servicesServerInstance.shutdownServer();
    }
}

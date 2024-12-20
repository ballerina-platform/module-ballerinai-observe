// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/jballerina.java;
import ballerina/observe;

function init() {
    if (observe:isMetricsEnabled()) {
        var err = externEnableMetrics(observe:getMetricsProvider());
        if (err is error) {
            externPrintError("failed to enable metrics");
        }
    }

    if (observe:isTracingEnabled()) {
        var err = externEnableTracing(observe:getTracingProvider());
        if (err is error) {
            externPrintError("failed to enable tracing");
        }
    }

    if (observe:isMetricsLogsEnabled()) {
        var err = externEnableMetricsLogging();
        if (err is error) {
            externPrintError("failed to enable tracing");
        }
    }
}

function externEnableMetrics(string provider) returns error? = @java:Method {
    'class: "io.ballerina.stdlib.observe.internal.NativeFunctions",
    name: "enableMetrics"
} external;

function externEnableTracing(string provider) returns error? = @java:Method {
    'class: "io.ballerina.stdlib.observe.internal.NativeFunctions",
    name: "enableTracing"
} external;

function externEnableMetricsLogging() returns error? = @java:Method {
    'class: "io.ballerina.stdlib.observe.internal.NativeFunctions",
    name: "enableMetricsLogging"
} external;

function externPrintError(string message) = @java:Method {
    'class: "io.ballerina.stdlib.observe.internal.NativeFunctions",
    name: "printError"
} external;

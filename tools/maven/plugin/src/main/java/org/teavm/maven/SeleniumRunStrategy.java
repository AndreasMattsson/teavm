/*
 *  Copyright 2015 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.teavm.tooling.testing.TestCase;

/**
 *
 * @author Alexey Andreev
 */
public class SeleniumRunStrategy implements TestRunStrategy {
    private URL url;
    private File directory;
    private ThreadLocal<WebDriver> webDriver = new ThreadLocal<>();
    private ThreadLocal<Integer> commandsSent = new ThreadLocal<>();

    public SeleniumRunStrategy(URL url, File directory) {
        this.url = url;
        this.directory = directory;
    }

    @Override
    public void beforeThread() {
        RemoteWebDriver driver = new RemoteWebDriver(url, DesiredCapabilities.firefox());
        webDriver.set(driver);
        commandsSent.set(0);
    }

    @Override
    public void afterThread() {
        webDriver.get().close();
        webDriver.get().quit();
        webDriver.remove();
    }

    @Override
    public String runTest(Log log, String runtimeScript, TestCase testCase) throws IOException {
        commandsSent.set(commandsSent.get() + 1);
        if (commandsSent.get().equals(20)) {
            commandsSent.set(0);
            webDriver.get().close();
            webDriver.get().quit();
            webDriver.set(new RemoteWebDriver(url, DesiredCapabilities.firefox()));
        }

        webDriver.get().manage().timeouts().setScriptTimeout(2, TimeUnit.SECONDS);
        JavascriptExecutor js = (JavascriptExecutor) webDriver.get();
        try {
            return (String) js.executeAsyncScript(
                    readResource("teavm-selenium.js"),
                    readFile(new File(directory, runtimeScript)),
                    readFile(new File(directory, testCase.getTestScript())),
                    readResource("teavm-selenium-adapter.js"));
        } catch (WebDriverException e) {
            log.error("Error occured running test " + testCase.getTestMethod(), e);
            @SuppressWarnings("unchecked")
            List<Object> errors = (List<Object>) js.executeScript("return window.jsErrors;");
            if (errors != null) {
                for (Object error : errors) {
                    log.error("  -- additional error: " + error);
                }
            }
            return null;
        }
    }

    private String readFile(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            return IOUtils.toString(input, "UTF-8");
        }
    }

    private String readResource(String resourceName) throws IOException {
        try (InputStream input = BuildJavascriptTestMojo.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (input == null) {
                return "";
            }
            return IOUtils.toString(input, "UTF-8");
        }
    }
}

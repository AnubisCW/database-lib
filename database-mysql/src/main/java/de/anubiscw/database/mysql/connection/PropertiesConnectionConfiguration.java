/*
 * MIT License
 *
 * Copyright (c) AnubisCW-Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.anubiscw.database.mysql.connection;

import de.anubiscw.database.api.connection.ConnectionConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class PropertiesConnectionConfiguration implements ConnectionConfiguration {

    public PropertiesConnectionConfiguration(@NotNull Path path) {
        this.path = path;
    }

    private final Path path;

    private String host;

    private String username;

    private String password;

    private String targetDatabase;

    private int port = -1;

    private boolean useSsl;

    @Override
    public @NotNull CompletableFuture<ConnectionConfiguration> load() {
        if (this.isLoaded()) {
            throw new RuntimeException("Configuration is already loaded");
        }

        return CompletableFuture.supplyAsync(() -> {
            Properties properties = new Properties();
            if (Files.notExists(this.path)) {
                properties.setProperty("host", "127.0.0.1");
                properties.setProperty("port", "3306");
                properties.setProperty("user", "user");
                properties.setProperty("database", "table");
                properties.setProperty("password", "password");
                properties.setProperty("ssl", "false");

                try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE)) {
                    properties.store(outputStream, "Default Configuration");
                } catch (final IOException exception) {
                    throw new RuntimeException(exception);
                }

                properties.clear();
            }

            try (InputStream inputStream = Files.newInputStream(path)) {
                properties.load(inputStream);
            } catch (final IOException exception) {
                throw new RuntimeException(exception);
            }

            this.host = Objects.requireNonNull(properties.getProperty("host"), "Host property missing in configuration " + path.toString());
            this.port = Integer.parseInt(Objects.requireNonNull(properties.getProperty("port"), "Port property missing in configuration " + path.toString()));
            this.username = Objects.requireNonNull(properties.getProperty("user"), "User property missing in configuration " + path.toString());
            this.targetDatabase = Objects.requireNonNull(properties.getProperty("database"), "Database property missing in configuration " + path.toString());
            this.password = Objects.requireNonNull(properties.getProperty("password"), "Password property missing in configuration " + path.toString());
            this.useSsl = Boolean.parseBoolean(Objects.requireNonNull(properties.getProperty("ssl"), "SSL property missing in configuration " + path.toString()));

            return this;
        });
    }

    @Override
    public @NotNull String getHost() {
        this.checkAccessible();
        return this.host;
    }

    @Override
    public @NotNull String getUserName() {
        this.checkAccessible();
        return this.username;
    }

    @Override
    public @NotNull String getPassword() {
        this.checkAccessible();
        return this.password;
    }

    @Override
    public @NotNull String getTargetDatabase() {
        this.checkAccessible();
        return this.targetDatabase;
    }

    @Override
    public int getPort() {
        this.checkAccessible();
        return this.port;
    }

    @Override
    public boolean useSSL() {
        this.checkAccessible();
        return this.useSsl;
    }

    @Override
    public boolean isLoaded() {
        return this.host != null && this.username != null && this.targetDatabase != null && this.password != null && this.port > 0;
    }

    private void checkAccessible() {
        if (!this.isLoaded()) {
            throw new RuntimeException("Configuration is not loaded yet");
        }
    }
}

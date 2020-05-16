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

import com.zaxxer.hikari.HikariDataSource;
import de.anubiscw.database.api.DatabaseProvider;
import de.anubiscw.database.api.connection.ConnectionConfiguration;
import de.anubiscw.database.api.connection.ConnectionProvider;
import de.anubiscw.database.mysql.MySQLDatabaseProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class MySQLConnectionProvider implements ConnectionProvider {

    private static final String CONNECT_URL = "jdbc:mysql://%s:%d/%s?serverTimezone=UTC&useSSL=%b&trustServerCertificate=%b";

    private final Collection<DatabaseProvider> providers = new CopyOnWriteArrayList<>();

    @Override
    public @NotNull CompletableFuture<Optional<DatabaseProvider>> connect(@NotNull ConnectionConfiguration connectionConfiguration) {
        if (!connectionConfiguration.isLoaded()) {
            throw new RuntimeException("Can only connect to a database using a loaded connection configuration");
        }

        return CompletableFuture.supplyAsync(() -> {
            HikariDataSource hikariDataSource = new HikariDataSource();

            hikariDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            hikariDataSource.setJdbcUrl(String.format(
                    CONNECT_URL,
                    connectionConfiguration.getHost(),
                    connectionConfiguration.getPort(),
                    connectionConfiguration.getTargetDatabase(),
                    connectionConfiguration.useSSL(),
                    connectionConfiguration.useSSL()
            ));

            hikariDataSource.setUsername(connectionConfiguration.getUserName());
            hikariDataSource.setPassword(connectionConfiguration.getPassword());

            hikariDataSource.setValidationTimeout(5000);
            hikariDataSource.setConnectionTimeout(5000);
            hikariDataSource.setMaximumPoolSize(20);

            hikariDataSource.validate();
            return hikariDataSource.isRunning() ? Optional.of(new MySQLDatabaseProvider(hikariDataSource)) : Optional.empty();
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> closeAllConnections() {
        return CompletableFuture.supplyAsync(() -> {
            for (DatabaseProvider provider : this.providers) {
                provider.closeConnection().join();
            }

            this.providers.clear();
            return null;
        });
    }
}

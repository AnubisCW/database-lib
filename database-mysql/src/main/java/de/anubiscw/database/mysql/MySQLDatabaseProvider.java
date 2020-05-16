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
package de.anubiscw.database.mysql;

import com.zaxxer.hikari.HikariDataSource;
import de.anubiscw.database.api.Database;
import de.anubiscw.database.api.DatabaseProvider;
import de.anubiscw.database.api.objects.DatabaseObject;
import de.anubiscw.database.mysql.util.SQLExceptionFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MySQLDatabaseProvider implements DatabaseProvider {

    private final Map<Class<? extends DatabaseObject>, Database<? extends DatabaseObject>> databases = new ConcurrentHashMap<>();

    public MySQLDatabaseProvider(@NotNull HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }

    private final HikariDataSource hikariDataSource;

    @Override
    public @NotNull CompletableFuture<Boolean> isConnected() {
        return CompletableFuture.supplyAsync(this.hikariDataSource::isRunning);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> closeConnection() {
        return CompletableFuture.supplyAsync(() -> {
            this.hikariDataSource.close();
            return this.hikariDataSource.isClosed();
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <V extends DatabaseObject> Database<V> getDatabase(@NotNull String name, @NotNull Class<V> type) {
        if (this.databases.containsKey(type)) {
            return (Database<V>) this.databases.get(type);
        }

        Database<V> database = new MySQLDatabase<>(this, name, type);
        this.databases.put(type, database);
        return database;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> existsDatabase(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> this.executeQuery(
                ResultSet::next, "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = " + name + " LIMIT 1"
        ));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> deleteDatabase(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> this.executeUpdate("DROP TABLE " + name) != -1);
    }

    @Override
    public @NotNull CompletableFuture<Collection<String>> getDatabaseNames() {
        return CompletableFuture.supplyAsync(() -> this.executeQuery(resultSet -> {
            Collection<String> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(resultSet.getString("table_name"));
            }

            return result;
        }, "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'"));
    }

    final int executeUpdate(@NotNull String query, @NotNull String key, @NotNull String identifier, @NotNull byte[] data, int dataIndex) {
        try (Connection connection = this.hikariDataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            if (dataIndex == 1) {
                statement.setBytes(1, data);
                statement.setString(2, key);
                statement.setString(3, identifier);
            } else {
                statement.setString(1, key);
                statement.setString(2, identifier);
                statement.setBytes(3, data);
            }

            return statement.executeUpdate();
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }

        return -1;
    }

    final int executeUpdate(@NotNull String query, @NotNull Object... objects) {
        try (Connection connection = this.hikariDataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            int i = 1;
            for (Object object : objects) {
                statement.setString(i++, object.toString());
            }

            return statement.executeUpdate();
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }

        return -1;
    }

    @Nullable
    final <T> T executeQuery(@NotNull SQLExceptionFunction<ResultSet, T> consumer, @NotNull String query, @NotNull Object... objects) {
        try (Connection connection = this.hikariDataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            int i = 1;
            for (Object object : objects) {
                statement.setString(i++, object.toString());
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                return consumer.apply(resultSet);
            }
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }

        return null;
    }
}

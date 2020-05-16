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
package de.anubiscw.database.api;

import de.anubiscw.database.api.objects.DatabaseEntry;
import de.anubiscw.database.api.objects.DatabaseObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Database<V extends DatabaseObject> {

    @NotNull
    CompletableFuture<Void> insert(@NotNull String key, @NotNull String identifier, @NotNull V value);

    @NotNull
    CompletableFuture<Optional<V>> get(@NotNull String key, @Nullable String identifier);

    @NotNull
    CompletableFuture<Void> updateIdentifier(@NotNull String key, @NotNull String identifier);

    @NotNull
    CompletableFuture<Void> remove(@NotNull String key);

    @NotNull
    CompletableFuture<Void> removeAll(@NotNull String identifier);

    @NotNull
    CompletableFuture<Collection<V>> sortByIdentifier(int limit);

    @NotNull
    CompletableFuture<Collection<String>> getKeys();

    @NotNull
    CompletableFuture<Collection<DatabaseEntry<V>>> getEntries();

    @NotNull
    CompletableFuture<Collection<DatabaseEntry<V>>> getEntries(@NotNull Predicate<String> identifierFilter);

    @NotNull
    CompletableFuture<Collection<DatabaseEntry<V>>> getEntriesFiltered(@NotNull Predicate<DatabaseEntry<V>> entryFilter);

    @NotNull
    CompletableFuture<Stream<DatabaseEntry<V>>> stream();

    @NotNull
    CompletableFuture<Void> clear();

    @NotNull
    CompletableFuture<Long> getSize();

    @NotNull
    CompletableFuture<Iterator<V>> iterator();

    @NotNull
    CompletableFuture<Spliterator<V>> spliterator();

    @NotNull
    CompletableFuture<Void> forEach(@NotNull Consumer<V> consumer);
}
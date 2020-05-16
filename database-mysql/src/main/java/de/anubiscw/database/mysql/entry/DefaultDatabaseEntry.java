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
package de.anubiscw.database.mysql.entry;

import de.anubiscw.database.api.Database;
import de.anubiscw.database.api.objects.DatabaseEntry;
import de.anubiscw.database.api.objects.DatabaseObject;
import org.jetbrains.annotations.NotNull;

public class DefaultDatabaseEntry<V extends DatabaseObject> implements DatabaseEntry<V> {

    public DefaultDatabaseEntry(String key, String identifier, V entry, Database<V> source) {
        this.key = key;
        this.identifier = identifier;
        this.entry = entry;
        this.source = source;
    }

    private final String key;

    private final String identifier;

    private final V entry;

    private final Database<V> source;

    @Override
    public @NotNull String getKey() {
        return this.key;
    }

    @Override
    public @NotNull String getIdentifier() {
        return this.identifier;
    }

    @NotNull
    @Override
    public V getEntry() {
        return this.entry;
    }

    @Override
    public @NotNull Database<V> getDatabase() {
        return this.source;
    }
}

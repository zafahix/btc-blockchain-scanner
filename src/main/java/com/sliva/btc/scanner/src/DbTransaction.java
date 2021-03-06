/*
 * Copyright 2018 Sliva Co.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sliva.btc.scanner.src;

import static com.google.common.base.Preconditions.checkArgument;
import com.sliva.btc.scanner.util.LazyInitializer;
import static com.sliva.btc.scanner.util.Utils.id2hex;
import java.util.Collection;
import javax.annotation.Nullable;
import lombok.NonNull;

/**
 *
 * @author Sliva Co
 */
public class DbTransaction implements SrcTransaction<DbInput, DbOutput> {

    private final int transactionId;
    private final LazyInitializer<String> txid;
    private final LazyInitializer<Collection<DbInput>> inputs;
    private final LazyInitializer<Collection<DbOutput>> outputs;

    public DbTransaction(DbBlockProvider blockProvider, int transactionId, @Nullable String txid) {
        checkArgument(blockProvider != null, "Afgument 'blockProvider' is nul");
        this.transactionId = transactionId;
        this.txid = new LazyInitializer<>(() -> blockProvider.psQueryTransactionHash
                .setParameters(p -> p.setInt(transactionId))
                .querySingleRow(rs -> id2hex(rs.getBytes(1)))
                .orElseThrow(() -> new IllegalStateException("Transaction #" + transactionId + " not found in DB")));
        this.inputs = new LazyInitializer<>(() -> blockProvider.psQueryTransactionInputs
                .setParameters(p -> p.setInt(transactionId))
                .executeQueryToList(rs -> new DbInput(blockProvider, rs.getShort(1), rs.getShort(2), rs.getInt(3), null, rs.getByte(4), rs.getBoolean(5), rs.getBoolean(6))));
        this.outputs = new LazyInitializer<>(() -> blockProvider.psQueryTransactionOutputs
                .setParameters(p -> p.setInt(transactionId))
                .executeQueryToList(rs -> new DbOutput(blockProvider, rs.getShort(1), rs.getInt(2), rs.getLong(3), rs.getByte(4))));
    }

    @NonNull
    @Override
    public String getTxid() {
        return txid.get();
    }

    @NonNull
    @Override
    public Collection<DbInput> getInputs() {
        return inputs.get();
    }

    @NonNull
    @Override
    public Collection<DbOutput> getOutputs() {
        return outputs.get();
    }

    public int getTransactionId() {
        return transactionId;
    }

}

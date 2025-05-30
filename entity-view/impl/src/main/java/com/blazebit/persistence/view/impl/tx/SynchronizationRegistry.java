/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.view.impl.tx;

import com.blazebit.persistence.view.spi.TransactionAccess;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The main purpose of a custom registry is to invoke synchronizations in reverse order when rolling back.
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class SynchronizationRegistry implements Synchronization, TransactionAccess {

    // We don't use a thread local because a TX could be rolled back from a different thread
    private static final ConcurrentMap<Thread, SynchronizationRegistry> REGISTRY = new ConcurrentHashMap<>();
    private final TransactionAccess transactionAccess;
    private final List<Synchronization> synchronizations;
    private final Thread key;

    public SynchronizationRegistry(TransactionAccess transactionAccess) {
        this.transactionAccess = transactionAccess;
        this.synchronizations = new ArrayList<>(1);
        this.key = Thread.currentThread();
        transactionAccess.registerSynchronization(this);
        REGISTRY.put(key, this);
    }

    public static SynchronizationRegistry getRegistry() {
        return REGISTRY.get(Thread.currentThread());
    }

    public TransactionAccess getTransactionAccess() {
        return transactionAccess;
    }

    @Override
    public boolean isActive() {
        return transactionAccess.isActive();
    }

    @Override
    public void markRollbackOnly() {
        transactionAccess.markRollbackOnly();
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        synchronizations.add(synchronization);
    }

    @Override
    public void beforeCompletion() {
        List<Exception> suppressedExceptions = null;
        for (int i = 0; i < synchronizations.size(); i++) {
            Synchronization synchronization = synchronizations.get(i);
            try {
                synchronization.beforeCompletion();
            } catch (Exception ex) {
                if (suppressedExceptions == null) {
                    suppressedExceptions = new ArrayList<>();
                }
                suppressedExceptions.add(ex);
            }
        }
        if (suppressedExceptions != null) {
            if (suppressedExceptions.size() == 1) {
                if (suppressedExceptions.get(0) instanceof RuntimeException) {
                    throw (RuntimeException) suppressedExceptions.get(0);
                }
                throw new RuntimeException("Error during beforeCompletion invocation of synchronizations", suppressedExceptions.get(0));
            }
            RuntimeException runtimeException = new RuntimeException("Error during beforeCompletion invocation of synchronizations");
            for (Exception supressedException : suppressedExceptions) {
                runtimeException.addSuppressed(supressedException);
            }
            throw runtimeException;
        }
    }

    @Override
    public void afterCompletion(int status) {
        List<Exception> suppressedExceptions = null;
        switch (status) {
            // We don't care about these statuses, only about committed and rolled back
            case Status.STATUS_ACTIVE:
            case Status.STATUS_COMMITTING:
            case Status.STATUS_MARKED_ROLLBACK:
            case Status.STATUS_NO_TRANSACTION:
            case Status.STATUS_PREPARED:
            case Status.STATUS_PREPARING:
                break;
            case Status.STATUS_COMMITTED:
                REGISTRY.remove(key);
                for (int i = 0; i < synchronizations.size(); i++) {
                    Synchronization synchronization = synchronizations.get(i);
                    try {
                        synchronization.afterCompletion(status);
                    } catch (Exception ex) {
                        if (suppressedExceptions == null) {
                            suppressedExceptions = new ArrayList<>();
                        }
                        suppressedExceptions.add(ex);
                    }
                }
                break;
            case Status.STATUS_ROLLING_BACK:
            case Status.STATUS_ROLLEDBACK:
            // We assume unknown means rolled back as Hibernate behaves this way with a local transaction coordinator
            case Status.STATUS_UNKNOWN:
            default:
                if (REGISTRY.remove(key) != null) {
                    for (int i = synchronizations.size() - 1; i >= 0; i--) {
                        Synchronization synchronization = synchronizations.get(i);
                        try {
                            synchronization.afterCompletion(status);
                        } catch (Exception ex) {
                            if (suppressedExceptions == null) {
                                suppressedExceptions = new ArrayList<>();
                            }
                            suppressedExceptions.add(ex);
                        }
                    }
                }
                break;
        }

        if (suppressedExceptions != null) {
            if (suppressedExceptions.size() == 1) {
                if (suppressedExceptions.get(0) instanceof RuntimeException) {
                    throw (RuntimeException) suppressedExceptions.get(0);
                }
                throw new RuntimeException("Error during afterCompletion invocation of synchronizations", suppressedExceptions.get(0));
            }
            RuntimeException runtimeException = new RuntimeException("Error during afterCompletion invocation of synchronizations");
            for (Exception supressedException : suppressedExceptions) {
                runtimeException.addSuppressed(supressedException);
            }
            throw runtimeException;
        }
    }
}

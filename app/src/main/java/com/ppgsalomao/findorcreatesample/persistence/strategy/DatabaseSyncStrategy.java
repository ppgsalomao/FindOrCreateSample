package com.ppgsalomao.findorcreatesample.persistence.strategy;

import java.util.List;

/**
 * Database Sync Strategy.
 * Created by ppgsalomao on 9/24/15.
 */
public interface DatabaseSyncStrategy<T, V> {

    void setNewObjectsList(List<T> newObjects);
    void setPersistedObjectsList(List<V> persistedObjects);

    void updateDatabase();

    interface NumericalIdExtractor<T> {
        int getNumericalId(T object);
    }

    interface DatabaseSyncDelegate<T, V> {
        void save(T newObject, V persistedObject);
        void delete(V persistedObject);
    }
}


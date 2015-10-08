package com.ppgsalomao.findorcreatesample.persistence.strategy;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Find-or-Create strategy for database sync.
 * Created by ppgsalomao on 9/24/15.
 */
public class FindOrCreateDatabaseSyncStrategy<T, V> implements DatabaseSyncStrategy<T, V> {

    private DatabaseSyncStrategy.DatabaseSyncDelegate<T, V> delegate;
    private DatabaseSyncStrategy.NumericalIdExtractor<T> newObjectIdExtractor;
    private DatabaseSyncStrategy.NumericalIdExtractor<V> persistedObjectIdExtractor;

    private List<T> newObjects;
    private List<V> persistedObjects;

    public FindOrCreateDatabaseSyncStrategy(
            DatabaseSyncDelegate<T, V> delegate,
            NumericalIdExtractor<T> newObjectIdExtractor,
            NumericalIdExtractor<V> persistedObjectIdExtractor) throws IllegalArgumentException {

        if(delegate == null || newObjectIdExtractor == null || persistedObjectIdExtractor == null)
            throw new IllegalArgumentException();

        this.delegate = delegate;
        this.newObjectIdExtractor = newObjectIdExtractor;
        this.persistedObjectIdExtractor = persistedObjectIdExtractor;
    }

    @Override
    public void setNewObjectsList(List<T> newObjects) {
        this.newObjects = newObjects;
    }

    @Override
    public void setPersistedObjectsList(List<V> persistedObjects) {
        this.persistedObjects = persistedObjects;
    }

    @Override
    public void updateDatabase() {
        Collections.sort(this.newObjects, this.newObjectsComparator);
        Collections.sort(this.persistedObjects, this.persistedObjectsComparator);

        Iterator<T> newObjectsIterator = this.newObjects.iterator();
        Iterator<V> persistedObjectsIterator = this.persistedObjects.iterator();

        T currentNewObject = this.getNextObject(newObjectsIterator);
        V currentPersistedObject = this.getNextObject(persistedObjectsIterator);

        while (currentNewObject != null || currentPersistedObject != null) {

            if(currentPersistedObject == null) {

                this.delegate.save(currentNewObject, null);

                currentNewObject = this.getNextObject(newObjectsIterator);
                continue;

            } else if(currentNewObject == null) {

                this.delegate.delete(currentPersistedObject);

                currentPersistedObject = this.getNextObject(persistedObjectsIterator);
                continue;
            }

            int newObjectId = this.newObjectIdExtractor.getNumericalId(currentNewObject);
            int persistedObjectId = this.persistedObjectIdExtractor.getNumericalId(
                    currentPersistedObject);

            if (newObjectId == persistedObjectId) {
                this.delegate.save(currentNewObject, currentPersistedObject);

                currentNewObject = this.getNextObject(newObjectsIterator);
                currentPersistedObject = this.getNextObject(persistedObjectsIterator);
            } else {
                if (newObjectId < persistedObjectId) {
                    this.delegate.save(currentNewObject, null);
                    currentNewObject = this.getNextObject(newObjectsIterator);
                } else {
                    this.delegate.delete(currentPersistedObject);
                    currentPersistedObject = this.getNextObject(persistedObjectsIterator);
                }
            }
        }
    }

    private <Z> Z getNextObject(Iterator<Z> iterator) {
        if(iterator.hasNext())
            return iterator.next();
        return null;
    }

    private Comparator<T> newObjectsComparator = new Comparator<T>() {
        @Override
        public int compare(T lhs, T rhs) {
            if(lhs == null)
                return -1;
            if(rhs == null)
                return 1;

            int lhsId = FindOrCreateDatabaseSyncStrategy.this.newObjectIdExtractor.getNumericalId(lhs);
            int rhsId = FindOrCreateDatabaseSyncStrategy.this.newObjectIdExtractor.getNumericalId(rhs);

            return Integer.valueOf(lhsId).compareTo(rhsId);
        }
    };

    private Comparator<V> persistedObjectsComparator = new Comparator<V>() {
        @Override
        public int compare(V lhs, V rhs) {
            if(lhs == null)
                return -1;
            if(rhs == null)
                return 1;

            int lhsId = FindOrCreateDatabaseSyncStrategy.this.persistedObjectIdExtractor.getNumericalId(lhs);
            int rhsId = FindOrCreateDatabaseSyncStrategy.this.persistedObjectIdExtractor.getNumericalId(rhs);

            return Integer.valueOf(lhsId).compareTo(rhsId);
        }
    };
}

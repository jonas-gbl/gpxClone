/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;



/**
 *
 * @author Jonas
 */
public abstract class GenericMapDecorator<K,V> implements Map<K,V>
{

    Map<K,V> decoratedMap;
        
    public GenericMapDecorator(Map<K, V> decoratedMap) {
        this.decoratedMap = decoratedMap;
    }
    
    public Map<K,V> getDecoratedMap()
    {
        return decoratedMap;
    }
    
    @Override
    public int size() {
        return decoratedMap.size();
    }

    @Override
    public boolean isEmpty() {
        return decoratedMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return decoratedMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return decoratedMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return decoratedMap.get(key);
    }

    @Override
    public V put(K key, V value) {
    return decoratedMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return decoratedMap.remove(key);
    }
    
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        decoratedMap.putAll(m);
    }

    @Override
    public void clear() {
        decoratedMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return decoratedMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return decoratedMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return decoratedMap.entrySet();
    }


}
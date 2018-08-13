package com.beoliver.internal;

import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentVector;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

import com.arangodb.velocypack.*;

public class VPackClojureDeserializers {

    private static PersistentVector deserialize_vec(VPackSlice parent,
                                                    VPackSlice vpack,
                                                    VPackDeserializationContext context) {
        Iterator<VPackSlice> iter = vpack.arrayIterator();
        ArrayList<Object> parsed = new ArrayList<>();

        VPackSlice slice;
        Type t = null;
        while (iter.hasNext()) {
            slice = iter.next();
            if (slice.isArray()) {
                parsed.add(context.deserialize(slice, t));
                continue;
            }
            if (slice.isObject()) {
                parsed.add(context.deserialize(slice, t));
            } else {
                ;
            }
        }
        return PersistentVector.create(parsed);
    }

    private static PersistentHashMap deserialize_map() {
        return PersistentHashMap.EMPTY;
    }

    public static final VPackDeserializer<PersistentVector> VECTOR = (VPackSlice parent,
                                                                      VPackSlice vpack,
                                                                      VPackDeserializationContext context) ->
    {
        return deserialize_vec(parent,vpack,context);
    };

    public static final VPackDeserializer<PersistentHashMap> HASHMAP = (VPackSlice parent,
                                                                        VPackSlice vpack,
                                                                        VPackDeserializationContext context) ->
    {
        Iterator<VPackSlice> iter = vpack.arrayIterator();
        ArrayList<Object> parsed = new ArrayList<>();

        VPackSlice slice;
        Type t = null;
        while (iter.hasNext()) {
            slice = iter.next();
            if (slice.isArray()) {
                parsed.add(context.deserialize(slice, t));
                continue;
            }
            if (slice.isObject()) {
                parsed.add(context.deserialize(slice, t));
            } else {
                ;
            }
        }
        return PersistentHashMap.create().create(parsed);
    };

}
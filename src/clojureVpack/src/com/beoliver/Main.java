package com.beoliver;
import com.arangodb.velocypack.*;
import clojure.lang.*;
import com.arangodb.velocypack.exception.VPackException;

import java.util.Iterator;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
	// write your code here

        VPack vpack = new VPack.Builder()
                .registerDeserializer(PersistentHashMap.class, new VPackDeserializer<PersistentHashMap>() {
                    @Override
                    public PersistentHashMap deserialize(
                            final VPackSlice parent,
                            final VPackSlice vpack,
                            final VPackDeserializationContext context) throws VPackException {

                        Iterator<Map.Entry<String, VPackSlice>> it = vpack.objectIterator();

                        Map.Entry en;
                        String key;
                        Object val;

                        while (it.hasNext()) {
                            en = it.next();
                            key = en.getKey().toString();
                            val = en.getValue();
                            val.getClass();
                        }
                        final PersistentHashMap obj = PersistentHashMap.EMPTY;
                        return obj;
                    }
                }).registerSerializer(MyObject.class, new VPackSerializer<MyObject>() {
                    @Override
                    public void serialize(
                            final VPackBuilder builder,
                            final String attribute,
                            final MyObject value,
                            final VPackSerializationContext context) throws VPackException {

                        builder.add(attribute, ValueType.OBJECT);
                        builder.add("name", value.getName());
                        builder.close();
                    }
                }).build();

    }
}

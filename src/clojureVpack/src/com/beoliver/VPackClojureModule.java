package com.beoliver;

import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentVector;
import com.arangodb.velocypack.VPackModule;
import com.arangodb.velocypack.VPackSetupContext;
import com.beoliver.internal.VPackClojureSerializers;
import com.beoliver.internal.VPackClojureDeserializers;
import com.arangodb.velocypack.VPackInstanceCreator;

public class VPackClojureModule implements VPackModule {

    @Override
    public <C extends VPackSetupContext<C>> void setup(C context) {
        context.registerDeserializer(PersistentVector.class, VPackClojureDeserializers.VECTOR);
        context.registerDeserializer(PersistentHashMap.class, VPackClojureDeserializers.HASHMAP);

        context.registerSerializer(PersistentVector.class, VPackClojureSerializers.VECTOR);
        context.registerSerializer(PersistentHashMap.class, VPackClojureSerializers.HASHMAP);
        context.registerEnclosingSerializer(PersistentHashMap.class, VPackClojureSerializers.HASHMAP);
    }
}

package org.apereo.cas.memcached.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoCallback;
import com.esotericsoftware.kryo.pool.KryoPool;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link CasKryoPool}. It provides pooling while allowing for try-with-resources to be used.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasKryoPool implements KryoPool {
    private final KryoPool kryoPoolRef;

    public CasKryoPool() {
        this(new ArrayList<>(), true, true, false, false);
    }
    
    public CasKryoPool(final Collection<Class> classesToRegister) {
        this(classesToRegister, true, true, false, false);
    }
    
    public CasKryoPool(final Collection<Class> classesToRegister,
                       final boolean warnUnregisteredClasses,
                       final boolean registrationRequired,
                       final boolean replaceObjectsByReferences,
                       final boolean autoReset) {
        
        final CloseableKryoFactory factory = new CloseableKryoFactory(this);
        factory.setWarnUnregisteredClasses(warnUnregisteredClasses);
        factory.setReplaceObjectsByReferences(replaceObjectsByReferences);
        factory.setAutoReset(autoReset);
        factory.setRegistrationRequired(registrationRequired);
        factory.setClassesToRegister(classesToRegister);
        
        this.kryoPoolRef = new KryoPool.Builder(factory).softReferences().build();
    }

    @Override
    public CloseableKryo borrow() {
        return (CloseableKryo) kryoPoolRef.borrow();
    }

    @Override
    public void release(final Kryo kryo) {
        kryoPoolRef.release(kryo);
    }

    @Override
    public <T> T run(final KryoCallback<T> callback) {
        try (CloseableKryo kryo = borrow()) {
            return callback.execute(kryo);
        }
    }
}

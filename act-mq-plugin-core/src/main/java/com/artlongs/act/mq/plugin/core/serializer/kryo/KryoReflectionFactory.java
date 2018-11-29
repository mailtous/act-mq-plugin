package com.artlongs.act.mq.plugin.core.serializer.kryo;

import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.artlongs.act.mq.plugin.core.MqEntity;
import de.javakaffee.kryoserializers.*;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.lang.reflect.InvocationHandler;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Kryo序列化需要先注册要序化的类才能快起来
 * Created by leeton on 8/30/17.
 */
public class KryoReflectionFactory extends KryoReflectionFactorySupport
{
    public KryoReflectionFactory()
    {
        setRegistrationRequired(false);//未经过注册的类,也可能通过
        setReferences(true);
       // setAutoReset(false);
       // setMaxDepth(5);
        setInstantiatorStrategy(new StdInstantiatorStrategy());
        register(MqEntity.class,new JavaSerializer());
        register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
        register(Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer());
        register(Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer());
        register(Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer());
        register(Collections.singletonList("").getClass(), new CollectionsSingletonListSerializer());
        register(Collections.singleton("").getClass(), new CollectionsSingletonSetSerializer());
        register(Collections.singletonMap("", "").getClass(), new CollectionsSingletonMapSerializer());
        register(Pattern.class, new RegexSerializer());
        register(BitSet.class, new BitSetSerializer());
        register(URI.class, new URISerializer());
        register(UUID.class, new UUIDSerializer());
        register(GregorianCalendar.class, new GregorianCalendarSerializer());
        register(InvocationHandler.class, new JdkProxySerializer());
        UnmodifiableCollectionsSerializer.registerSerializers(this);
        SynchronizedCollectionsSerializer.registerSerializers(this);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Serializer<?> getDefaultSerializer(Class clazz)
    {
        if(EnumSet.class.isAssignableFrom(clazz))
            return new EnumSetSerializer();

        if(EnumMap.class.isAssignableFrom(clazz))
            return new EnumMapSerializer();

        if(Collection.class.isAssignableFrom(clazz))
            return new CopyForIterateCollectionSerializer();

        if(Map.class.isAssignableFrom(clazz))
            return new CopyForIterateMapSerializer();

        if(Date.class.isAssignableFrom(clazz))
            return new DateSerializer( clazz );

        if (SubListSerializers.ArrayListSubListSerializer.canSerialize(clazz)
                || SubListSerializers.JavaUtilSubListSerializer.canSerialize(clazz))
            return SubListSerializers.createFor(clazz);

        return super.getDefaultSerializer(clazz);
    }
}
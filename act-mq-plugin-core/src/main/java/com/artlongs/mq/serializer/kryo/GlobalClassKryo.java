package com.artlongs.mq.serializer.kryo;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.MapReferenceResolver;
import org.apache.commons.lang3.StringUtils;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;

import static com.esotericsoftware.kryo.util.Util.getWrapperClass;

/**
 * Created by leeton on 8/31/17.
 */
public class GlobalClassKryo extends Kryo {

    private static Registration memoizedClassIdValue;
    private static Class memoizedClass;
    private static Registration memoizedClassValue;
    private static ClassResolver classResolver =  new ExternalizableClassResolver();

    public static class ExternalizableClassResolver implements ClassResolver {

        //local serializers
        final Map<Class, Registration> fromClass = new HashMap();
        final Map<Integer, Registration> fromId = new HashMap();

        public static class GlobalRegistration { int id; Class type; Class<? extends Serializer> serializer; }

        public final Map<Integer, GlobalRegistration> globalIds;
        public final Map<Class, GlobalRegistration> globalClasses;


        // I synchronize because I have one reader and one writer thread and
        // writer may break the reader when adds something into the map.
        public ExternalizableClassResolver() {this (
                Collections.synchronizedMap(new HashMap()),
                Collections.synchronizedMap(new HashMap())
        ) ;}

        public ExternalizableClassResolver(Map<Integer, GlobalRegistration> ids, Map<Class, GlobalRegistration> classes) {
            globalIds = ids;
            globalClasses = classes;
        }

        public ExternalizableClassResolver (DataInput in) throws ClassNotFoundException, IOException {
            this();
            int id;
            while ((id = in.readInt()) != -1) {
                GlobalRegistration e = new GlobalRegistration();
                globalIds.put(e.id = id, e);
                e.type = Class.forName(in.readUTF());
                e.serializer = (Class<? extends Serializer>) Class.forName(in.readUTF());
                globalClasses.put(e.type, e);
            }
        }

        public void save(DataOutput out) throws IOException {
            for (GlobalRegistration entry : globalIds.values()) {
                out.writeInt(entry.id);
                out.writeUTF(entry.type.getName());
                out.writeUTF(entry.serializer.getName());
            }
            out.writeInt(-1);
        }

        static final boolean TRACE = false;
        void log(String msg) {
            System.err.println(kryo != null ? StringUtils.defaultString(kryo.getDepth()+"", "  ")  + msg : msg);
        }
        @Override
        public Registration writeClass(Output output, Class type) {
            if (type == null) {output.writeInt(0, true); return null;}
            Registration registration = kryo.getRegistration(type);
            output.writeInt(registration.getId(), true);
            return registration;
        }
        @Override
        public Registration readClass(Input input) {
            int classID = input.readInt(true);
            if (classID == 0) return null;
            Registration registration = fromId.get(classID);
            if (registration == null) {
                registration = tryGetFromGlobal(globalIds.get(classID), classID + "");
            }
            if (registration == null) throw new KryoException("Encountered unregistered class ID: " + classID);
            return registration;
        }

        public Registration register(Registration registration) {
            throw new KryoException("register(registration) is not allowed. Use register(type, serializer)");
        }

        public Registration getRegistration(int classID) {
            throw new KryoException("getRegistration(id) is not implemented");
        }

        Registration tryGetFromGlobal(GlobalRegistration globalClass, String title) {
            if (globalClass != null) {
                kryo.addDefaultSerializer(globalClass.type,globalClass.serializer);
                Serializer serializer = kryo.getSerializer(globalClass.type);
                Registration registration = register(globalClass.type, serializer, globalClass.id, "local");
                if (TRACE) log("getRegistration(" + title + ") taken from global => " + registration);
                return registration;
            } else
            if (TRACE) log("getRegistration(" + title + ") was not found");
            return null;
        }
        public Registration getRegistration(Class type) {
            Registration registration = fromClass.get(type);
            if (registration == null) {
                registration = tryGetFromGlobal(globalClasses.get(type), type.getSimpleName());
            } else
            if (TRACE) log("getRegistration(" + type.getSimpleName() + ") => " + registration);

            return registration;
        }

        Registration register(Class type, Serializer serializer, int id, String title) {
            Registration registration = new Registration(type, serializer, id);
            fromClass.put(type, registration);
            fromId.put(id, registration);

            if (TRACE) log("new " + title + " registration, " + registration);

            //why dont' we put into fromId?
            if (registration.getType().isPrimitive()) fromClass.put(getWrapperClass(registration.getType()), registration);
            return registration;
        }

        int primitiveCounter = 1; // 0 is reserved for NULL
        static final int PRIMITIVE_MAX = 20;

        //here we register anything that is missing in the global map.
        // It must not be the case that something available is registered for the second time, particularly because we do not check this here
        // and use registered map size as identity counter. Normally, check is done prior to callig this method, in getRegistration
        public Registration register(Class type, Serializer serializer) {

            if (type.isPrimitive() || type.equals(String.class))
                return register(type, serializer, primitiveCounter++, "primitive");

            GlobalRegistration global = globalClasses.get(type);

            if (global != null )
                throw new RuntimeException("register(type,serializer): we have " + type + " in the global map, this method must not be called");

            global = new GlobalRegistration();
            globalIds.put(global.id = globalClasses.size() + PRIMITIVE_MAX, global);
            globalClasses.put(global.type = type, global);
            global.serializer= serializer.getClass();

            return register(global.type, serializer, global.id, "global");
        }

        public Registration registerImplicit(Class type) {
            throw new RuntimeException("registerImplicit is not needed since we register missing automanically in getRegistration");
        }

        @Override
        public void reset() {
            // super.reset(); //no need to reset the classes
        }

        Kryo kryo;
        public void setKryo(Kryo kryo) {
            this.kryo = kryo;
        }
    }




    public ExternalizableClassResolver ourClassResolver() {
        return (ExternalizableClassResolver) classResolver;
    }

    public GlobalClassKryo(ClassResolver resolver) {
        super(resolver, new MapReferenceResolver());
        setInstantiatorStrategy(new StdInstantiatorStrategy());
        this.setRegistrationRequired(true);
    }
    public GlobalClassKryo() {
        this(new ExternalizableClassResolver());
    }

    @Override
    public Registration getRegistration (Class type) {
        if (type == null) throw new IllegalArgumentException("type cannot be null.");

        if (type == memoizedClass) return memoizedClassValue;
        Registration registration = classResolver.getRegistration(type);
        if (registration == null) {
            if (Proxy.isProxyClass(type)) {
                // If a Proxy class, treat it like an InvocationHandler because the concrete class for a proxy is generated.
                registration = getRegistration(InvocationHandler.class);
            } else if (!type.isEnum() && Enum.class.isAssignableFrom(type)) {
                // This handles an enum value that is an inner class. Eg: enum A {b{}};
                registration = getRegistration(type.getEnclosingClass());
            } else if (EnumSet.class.isAssignableFrom(type)) {
                registration = classResolver.getRegistration(EnumSet.class);
            }
            if (registration == null) {
                //registration = classResolver.registerImplicit(type);
                return register(type, getDefaultSerializer(type));
            }
        }
        memoizedClass = type;
        memoizedClassValue = registration;
        return registration;
    }

    public Registration register(Class type, Serializer serializer) {
        Registration reg = ourClassResolver().register(type, serializer);
        return reg;
    }

    public Registration register(Registration registration) {
        throw new RuntimeException("only register(Class, Serializer) is allowed");}

    public Registration register(Class type) {
        throw new RuntimeException("only register(Class, Serializer) is allowed");}

    public Registration register(Class type, int id) {
        throw new RuntimeException("only register(Class, Serializer) is allowed");}

    public Registration register(Class type, Serializer serializer, int id) {
        throw new RuntimeException("only register(Class, Serializer) is allowed");
    }

    static void write(String title, Kryo k, ByteArrayOutputStream baos, Object obj) {
        Output output = new Output(baos);
        k.writeClassAndObject(output, obj);
        output.close();
        System.err.println(baos.size() + " bytes after " + title + " write");
    }
    static class A {
        String field = "abcABC";
        A a = this;
        //int b = 1; // adds 1 byte to serialization
        @Override
        public String toString() {
            return field
                    + " " + list.size()
                    //+ ", " + b
                    ;
        }

        // list adds 3 bytes to serialization, two 3-byte string items add additionally 10 bytes in total
        ArrayList list = new ArrayList(100); // capacity is trimmed in serialization
        {
            list.add("LLL");
            list.add("TTT");

        }
    }

    private static void test() throws IOException, ClassNotFoundException {
        GlobalClassKryo k = new GlobalClassKryo();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        write("first", k, baos, new A()); // write takes 24 byts

        //externalize the map
        ByteArrayOutputStream mapOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(mapOut);
        k.ourClassResolver().save(dataOut);
        dataOut.close();

        //deserizalize the map
        DataInputStream serialized = new DataInputStream(new ByteArrayInputStream(mapOut.toByteArray()));
        ExternalizableClassResolver resolver2 = new ExternalizableClassResolver(serialized);

        //use the map
        k = new GlobalClassKryo(resolver2);
        write("second", k, baos, new A()); // 24 bytes

        Input input = new Input(new ByteArrayInputStream(baos.toByteArray()));
        Object read = k.readClassAndObject(input);
        System.err.println("output " + read);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Kryo k = new Kryo();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        write("first", k, baos, new A()); // write takes 78 bytes
        write("second", k, baos, new A()); // +78 bytes
        System.err.println("----------------");

        test();
    }
}

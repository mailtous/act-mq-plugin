package com.artlongs.act.mq.plugin.core;

import act.app.App;
import act.app.AppByteCodeScanner;
import act.app.AppByteCodeScannerBase;
import act.app.AppSourceCodeScanner;
import act.asm.AnnotationVisitor;
import act.asm.MethodVisitor;
import act.asm.Type;
import act.util.AppCodeScannerPluginBase;
import act.util.ByteCodeVisitor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MQ消息接收器
 * Created by leeton on 9/22/17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MqReceiver {
    String value();                 // TOPIC,消息主题(通用)

    String exchange() default "";   // 交换器,RABBITMQ才会使用到

    String queue() default "";      // 队列

    class Plugin extends AppCodeScannerPluginBase {
        @Override
        public AppSourceCodeScanner createAppSourceCodeScanner(App app) {
            return null;
        }

        @Override
        public AppByteCodeScanner createAppByteCodeScanner(App app) {
            return new BytecodeScanner();
        }

        @Override
        public boolean load() {
            return true;
        }
    }

    // TODO implement the MqReceiver scanner logic
    class BytecodeScanner extends AppByteCodeScannerBase {

        @Override
        public ByteCodeVisitor byteCodeVisitor() {
            return new ByteCodeVisitor() {

                private String className;
                private String methodName;
                private String topic;
                private String exchange;
                private String queue;

                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    className = Type.getObjectType(name).getClassName();
                    super.visit(version, access, name, signature, superName, interfaces);
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                    return new MethodVisitor(ASM5, mv) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                            AnnotationVisitor av = super.visitAnnotation(desc, visible);
                            if (MqReceiver.class.getName().equals(Type.getObjectType(desc).getClassName())) {
                                return new AnnotationVisitor(ASM5, av) {
                                    @Override
                                    public void visit(String name, Object value) {
                                        super.visit(name, value);
                                    }
                                };
                            }
                            return av;
                        }
                    };
                }
            };
        }

        @Override
        public void scanFinished(String className) {

        }

        @Override
        protected boolean shouldScan(String className) {
            return true;
        }
    }
}

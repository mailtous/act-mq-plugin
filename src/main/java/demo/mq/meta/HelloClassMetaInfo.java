package demo.mq.meta;

/*-
 * #%L
 * ACT Framework
 * %%
 * Copyright (C) 2014 - 2017 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import act.asm.Type;
import act.util.DestroyableBase;
import org.osgl.util.C;
import org.osgl.util.S;

import javax.enterprise.context.ApplicationScoped;

import static act.Destroyable.Util.destroyAll;

/**
 * Stores all class level information to support generating of mailer method
 */
@ApplicationScoped
public final class HelloClassMetaInfo extends DestroyableBase {

    private Type type;
    private String configId;
    private boolean isAbstract = false;
    private String contextPath;
    private String templateContext;
    private boolean isThisAnno;

    public HelloClassMetaInfo className(String name) {
        this.type = Type.getObjectType(name);
        return this;
    }

    @Override
    protected void releaseResources() {
        super.releaseResources();
    }

    public String className() {
        return type.getClassName();
    }

    public HelloClassMetaInfo configId(String id) {
        configId = id;
        return this;
    }

    public String configId() {
        return configId;
    }

    public HelloClassMetaInfo templateContext(String templateContext) {
        this.templateContext = templateContext;
        return this;
    }

    public String templateContext() {
        return templateContext;
    }

    public String internalName() {
        return type.getInternalName();
    }

    public Type type() {
        return type;
    }

    public HelloClassMetaInfo setAbstract() {
        isAbstract = true;
        return this;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public String contextPath() {
        return contextPath;
    }

    public HelloClassMetaInfo contextPath(String path) {
        if (S.blank(path)) {
            contextPath = "/";
        } else {
            contextPath = path;
        }
        return this;
    }

    public boolean isThisAnno() {
        return isThisAnno;
    }

    public HelloClassMetaInfo isThisAnno(boolean thisAnno) {
        isThisAnno = thisAnno;
        return this;
    }
}

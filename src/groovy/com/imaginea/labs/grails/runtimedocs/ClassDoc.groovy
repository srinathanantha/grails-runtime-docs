package com.imaginea.labs.grails.runtimedocs

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import org.codehaus.groovy.runtime.metaclass.ClosureMetaMethod
import org.codehaus.groovy.runtime.metaclass.ClosureStaticMetaMethod
import org.springframework.core.io.UrlResource

class ClassDoc implements Comparable {
    Class targetClass;
    ExpandoMetaClass targetMetaClass;
    UrlResource urlResource;
    String classComments = "";

    public ClassDoc(Class targetClass, ExpandoMetaClass targetMetaClass, UrlResource urlResource) {
        this.targetClass = targetClass;
        this.targetMetaClass = targetMetaClass;
        this.urlResource = urlResource;
        readComments();
    }

    public void readComments() {
        if (urlResource != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlResource.getInputStream()));
            StringBuilder sourceCode = new StringBuilder();
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                if (inputLine.contains(targetClass.getSimpleName())) {
                    int start = sourceCode.lastIndexOf("/*") + 1;
                    int end = sourceCode.lastIndexOf("*/");
                    classComments = (start > 0 && end > 0 && end > start) ? sourceCode.toString().substring(start, end).replace('*', '').replace("\n", "<br/>") : "";
                    break;
                } else
                    sourceCode.append(inputLine).append("\n");
            }
            reader.close();
        }
    }

    def methodMissing(String name, args) {
        Class[] params = args.collect { it.class };
        Method method = targetClass.class.getMethod(name, params);
        if (method) {
            Object returnValue = method.invoke(targetClass, args);
            return returnValue;
        } else
            throw new MissingMethodException(name, targetMetaClass.delegate, args)
    }

    public boolean isClass() {
        return !(targetClass.isInterface() || targetClass.isAnnotation() || targetClass.isEnum());
    }

    public boolean isGroovy() {
        return GroovyObject.class.isAssignableFrom(targetClass);
    }

    public boolean isExternalClass(Class clazz) {
        return RootDoc.getClassDoc(clazz) ? false : true;
    }

    public boolean isProperty(Field field) {
        int modifiers = field.getModifiers()
        return !(Modifier.isPublic(modifiers) || Modifier.isPrivate(modifiers) || Modifier.isProtected(modifiers));
    }

    public def getAnnotations(def object) {
        if (object instanceof ClassDoc) {
            return targetClass.class.getAnnotations();
        } else if (object.metaClass.respondsTo(object, "getAnnotations"))
            return object.getAnnotations();
    }

    public String getClassComments() {
        return classComments;
    }

    // TODO
    public String getCommentText(def input) {
        return "";
    }

    public Field[] getDeclaredFields() {
        return targetClass.getDeclaredFields().findAll { !isProperty(it) && !it.isEnumConstant() };
    }

    public Field[] getDeclaredProperties() {
        return targetClass.getDeclaredFields().findAll { isProperty(it) && !it.isEnumConstant() };
    }

    public String getDocUrl(def type, boolean full) {
        if (type instanceof Class) {
            ClassDoc classDoc = RootDoc.getClassDoc(type)
            if (classDoc) {
                return "<a href='" + this.getRelativeRootPath() + classDoc.getFullPathName() + ".html'>" + (full ? classDoc.getName() : classDoc.getSimpleName()) + "</a>";
            } else
                return type.getName();
        }
        return type;
    }

    public List<MetaMethod> getExpandoMethods() {
        List<MetaMethod> unmodifiableList = targetMetaClass.getExpandoMethods();
        List<MetaMethod> newList = new ArrayList(unmodifiableList);
        return newList.sort { it.getName() }
    }

    public String getFullPathName() {
        if (targetClass.getPackage() == null)
            return RootDoc.DEFAULT_PACKAGE + '/' + targetClass.getName();
        else
            return targetClass.getName().replace('.', '/');
    }

    public String getMethodInfo(MetaMethod metaMethod) {
        Field field = null;
        Object value = null;
        try {
            field = metaMethod.getClass().getDeclaredField('callable');
            if (field != null) {
                field.setAccessible(true);
                value = field.get(metaMethod);
                String name = value.getClass().getSimpleName()
                if ((metaMethod instanceof ClosureMetaMethod || metaMethod instanceof ClosureStaticMetaMethod) &&
                    (name != 'StaticMethodInvokingClosure' && name != 'InstanceMethodInvokingClosure'))
                    return "Added by " + name;
                else
                    return "Added by " + value.getDelegate().getClass().getSimpleName();
            }
        } catch (NoSuchFieldException e) {
            try {
                field = metaMethod.getClass().getDeclaredField('val$closure');
                if (field != null) {
                    field.setAccessible(true);
                    value = field.get(metaMethod);
                    return "Added by " + value.getDelegate().getSimpleName();
                }
            } catch (NoSuchFieldException e1) {
                if (metaMethod.respondsTo("getClosure")) {
                    def delegate = metaMethod.getClosure().getDelegate();
                    if(delegate instanceof Class)
                        return "Added by " + delegate.getSimpleName();
                    else
                        return "Added by " + delegate.getClass().getSimpleName();
                } else {
                    return "";
                }
            }
        }
    }

    public String getRelativeRootPath() {
        StringTokenizer tokenizer = new StringTokenizer(getFullPathName(), "/");
        StringBuffer sb = new StringBuffer();
        if (tokenizer.hasMoreTokens()) {
            tokenizer.nextToken(); // ignore the first token, as we want n-1 parent dirs
        }
        while (tokenizer.hasMoreTokens()) {
            tokenizer.nextToken();
            sb.append("../");
        }
        return sb.toString();
    }

    public String getPackageName() {
        return targetClass.getPackage() ? targetClass.getPackage().getName() : RootDoc.DEFAULT_PACKAGE;
    }

    public List<Class> getParentClasses() {
        List<Class> parentClasses = new ArrayList<Class>();
        Class superClass = targetClass.getSuperclass();
        while (superClass != null) {
            parentClasses.add(superClass);
            superClass = superClass.getSuperclass();
        }
        return parentClasses;
    }

    public Set<Class> getParentInterfaces() {
        Set<Class> parentInterfaces = getParentInterfaces(new HashSet<Class>(), targetClass);
        return parentInterfaces;
    }

    private Set<Class> getParentInterfaces(Set<Class> parentInterfaces, Class targetClass) {
        Class[] interfaces = targetClass.getInterfaces();
        parentInterfaces.addAll(interfaces);
        for (Class clazz: interfaces) {
            getParentInterfaces(parentInterfaces, clazz);
        }
        return parentInterfaces;
    }

    public String getTypeDescription() {
        if (targetClass.isInterface()) return "Interface";
        if (targetClass.isAnnotation()) return "Annotation Type";
        if (targetClass.isEnum()) return "Enum";
        return "Class";
    }

    public String getTypeSourceDescription() {
        if (targetClass.isInterface()) return "interface";
        if (targetClass.isAnnotation()) return "@interface";
        if (targetClass.isEnum()) return "enum";
        return "class";
    }

    int compareTo(Object obj) {
        ClassDoc classDoc = (ClassDoc) obj;
        return this.targetClass.getSimpleName().compareTo(classDoc.targetClass.getSimpleName());
    }
}

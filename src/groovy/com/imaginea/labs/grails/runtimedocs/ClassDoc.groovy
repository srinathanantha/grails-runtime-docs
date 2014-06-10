package com.imaginea.labs.grails.runtimedocs

import org.codehaus.groovy.runtime.metaclass.ClosureMetaMethod
import org.codehaus.groovy.runtime.metaclass.ClosureStaticMetaMethod

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class ClassDoc implements Comparable {
    Class targetClass;
    ExpandoMetaClass targetMetaClass;

    public ClassDoc(Class targetClass, ExpandoMetaClass targetMetaClass) {
        this.targetClass = targetClass;
        this.targetMetaClass = targetMetaClass;
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

    public def getAnnotations(def object) {
        if (object instanceof ClassDoc) {
            return targetClass.class.getAnnotations();
        } else if (object.metaClass.respondsTo(object, "getAnnotations"))
            return object.getAnnotations();
    }

    public String getClassComments() {
        return "";   // TODO
    }

    public String getCommentText(def input) {
        return "";     // TODO
    }

    public Field[] getDeclaredFields() {
        return targetClass.getDeclaredFields().findAll { !isProperty(it) && !it.isEnumConstant() };
    }

    public Field[] getDeclaredProperties() {
        return targetClass.getDeclaredFields().findAll { isProperty(it) && !it.isEnumConstant() };
    }

    public String getDocUrl(def type, boolean full) {
        if (type instanceof Class) {
            String title = (type.isAnnotation() ? "Annotation in " : (type.isInterface() ? "Interface in " : (type.isEnum() ? "Enum in " : "Class in "))) + type.getPackage();
            ClassDoc classDoc = RootDoc.getClassDoc(type)
            if (classDoc) {
                def href = this.getRelativeRootPath() + classDoc.getFullPathName() + ".html";
                return "<a href='" + href + "' title='" + title + "'>" + (full ? classDoc.getName() : classDoc.getSimpleName()) + "</a>";
            } else
                return "<a href='javascript:void()' style='cursor:text' title='" + title + "'>" + (full ? type.getName() : type.getSimpleName()) + "</a>";
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
                    if (delegate instanceof Class)
                        return "Added by " + delegate.getSimpleName();
                    else
                        return "Added by " + delegate.getClass().getSimpleName();
                } else {
                    return "";
                }
            }
        }
    }

    public String getQualifiedTypeName() {
        String qtnWithSlashes = fullPathName.startsWith(RootDoc.DEFAULT_PACKAGE + '/') ? fullPathName.substring((RootDoc.DEFAULT_PACKAGE + '/').length()) : fullPathName;
        return qtnWithSlashes.replace('/', '.');
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

    public String getResourcesPath() {
        return getRelativeRootPath() + GroovyRuntimeDocWriter.RESOURCES + '/';
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

    public List<Class> getInheritanceHierarchy() {
        List<Class> classes = new ArrayList<Class>();
        classes.add(targetClass);
        Class superClass = targetClass.getSuperclass();
        while (superClass != null) {
            classes.add(0, superClass);
            superClass = superClass.getSuperclass();
        }
        return classes;
    }

    public Set<Class> getParentInterfaces() {
        Set<Class> parentInterfaces = getParentInterfacesSet(new HashSet<Class>(), targetClass);
        return parentInterfaces;
    }

    private Set<Class> getParentInterfacesSet(Set<Class> parentInterfaces, Class targetClass) {
        Class[] interfaces = targetClass.getInterfaces();
        parentInterfaces.addAll(interfaces);
        for (Class clazz : interfaces) {
            getParentInterfacesSet(parentInterfaces, clazz);
        }
        return parentInterfaces;
    }

    public String getSimpleTypeName() {
        String typeName = getQualifiedTypeName();
        int lastDot = typeName.lastIndexOf('.');
        if (lastDot < 0) return typeName;
        return typeName.substring(lastDot + 1);
    }

    public String getTypeDescription() {
        if (targetClass.isAnnotation()) return "Annotation";
        if (targetClass.isInterface()) return "Interface";
        if (targetClass.isEnum()) return "Enum";
        return "Class";
    }

    public String getTypeSourceDescription() {
        if (targetClass.isAnnotation()) return "@interface";
        if (targetClass.isInterface()) return "interface";
        if (targetClass.isEnum()) return "enum";
        return "class";
    }

    int compareTo(Object obj) {
        ClassDoc classDoc = (ClassDoc) obj;
        return this.targetClass.getSimpleName().compareTo(classDoc.targetClass.getSimpleName());
    }

    public boolean isClass() {
        return !(targetClass.isInterface() || targetClass.isAnnotation() || targetClass.isEnum());
    }

    public boolean isInterface() {
        return targetClass.isInterface();
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
}

package com.imaginea.labs.grails.runtimedocs

class PackageDoc implements Comparable {
    String packageName;
    Set<ClassDoc> allClasses = new TreeSet<ClassDoc>();

    public PackageDoc(String packageName) {
        this.packageName = packageName ?: RootDoc.DEFAULT_PACKAGE;
    }

    int compareTo(Object obj) {
        PackageDoc packageDoc = (PackageDoc) obj;
        return this.getName().compareTo(packageDoc.getName());
    }

    public void addClassDoc(ClassDoc classDoc) {
        allClasses.add(classDoc);
    }

    public String getDescription() {
        return "";
    }

    public String getName() {
        return packageName;
    }

    public String getSummary() {
        return "";
    }

    public TreeSet<ClassDoc> getAllClasses() {
        return allClasses;
    }

    public String getPackagePath() {
        return packageName.replace('.', '/');
    }

    public String getRelativeRootPath() {
        StringTokenizer tokenizer = new StringTokenizer(getPackagePath(), '/');
        StringBuffer sb = new StringBuffer();
        while (tokenizer.hasMoreTokens()) {
            tokenizer.nextToken();
            sb.append("../");
        }
        return sb.toString();
    }
}

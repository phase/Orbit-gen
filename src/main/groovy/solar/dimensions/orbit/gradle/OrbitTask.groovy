package solar.dimensions.orbit.gradle

import com.sun.codemodel.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.tree.*
import solar.dimensions.orbit.API
import solar.dimensions.orbit.util.SignatureNode

import static org.objectweb.asm.Opcodes.*

class OrbitTask extends DefaultTask {
    private String apiPackage = "api";
    private String sourcePackage = "solar.dimensions";

    private Map<String, JType> directClassCache = new HashMap<>();
    private JCodeModel model
    private JDefinedClass definedClass


    @TaskAction
    def generate() {
        File classesDir = project.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).output.classesDir;
        processDirectory(classesDir);
    }

    private String toApiNamespace(String type) {
        if (type.startsWith('L')) {
            type = type.substring(1, type.length() - 1);
        }
        return type.replace('/', '.').replace(sourcePackage, apiPackage);
    }

    private static String getSignature(Class clazz) {
        return 'L' + clazz.getCanonicalName().replace('.', '/') + ';';
    }

    private static String sigToType(String sig) {
        switch (sig.toUpperCase()) {
            case "C": return "char";
            case "B": return "byte";
            case "S": return "short";
            case "I": return "int";
            case "L": return "long";
            case "F": return "float";
            case "D": return "double";
            case "Z": return "boolean";
            case "V": return "void";
            default: return sig;
        }
    }

    private static JExpression getDefaultValue(String type) {
        type = sigToType(type);
        switch (type) {
            case "char": return JExpr.lit("\0".charAt(0));
            case "byte":
            case "short":
            case "int":
            case "long":
            case "float":
            case "double": return JExpr.lit(0);
            case "boolean": return JExpr.lit(false);
            default: JExpr._null();
        }
    }

    private JType directClass(String type) {
        type = sigToType(type);
        switch (type) {
            case "char": return model._ref(char.class);
            case "byte": return model._ref(byte.class);
            case "short": return model._ref(short.class);
            case "int": return model._ref(int.class);
            case "long": return model._ref(long.class);
            case "float": return model._ref(float.class);
            case "double": return model._ref(double.class);
            case "boolean": return model._ref(boolean.class);
        }

        if (definedClass.fullName().equals(type)) {
            return definedClass;
        }

        JType result = directClassCache.get(type);
        if (result == null) {
            result = model.directClass(type);
            directClassCache.put(type, result);
        }

        return result;
    }

    private void processClass(File classFile) {
        ClassReader classReader = new ClassReader(new FileInputStream(classFile))

        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, ASM5)

        String apiAnnotation = getSignature(API.class)

        boolean api = false;
        for (AnnotationNode annotationNode : classNode.visibleAnnotations) {
            if (annotationNode.desc.equals(apiAnnotation)) {
                api = true;
                break;
            }
        }

        if (api) {
            File orbitDir = new File(project.getBuildDir(), "orbit");
            File orbitSource = new File(new File(new File(orbitDir, "src"), "api"), "java");
            orbitSource.mkdirs();

            if ((classNode.access & ACC_INTERFACE) != 0) {

            } else {
                model = new JCodeModel();
                String selfClass = toApiNamespace(classNode.name);
                definedClass = this.model._class(selfClass);

                for (FieldNode fieldNode : classNode.fields) {
                    boolean isPublic = (fieldNode.access & ACC_PUBLIC) != 0;
                    boolean isProtected = (fieldNode.access & ACC_PROTECTED) != 0;
                    boolean isStatic = (fieldNode.access & ACC_STATIC) != 0;
                    boolean isFinal = (fieldNode.access & ACC_FINAL) != 0;

                    if (isPublic || isProtected) {
                        int mods = isPublic ? JMod.PUBLIC : JMod.PROTECTED;
                        mods |= isStatic ? JMod.STATIC : 0;
                        mods |= isFinal ? JMod.FINAL : 0;

                        String fieldName = toApiNamespace(fieldNode.name);
                        String fieldType = toApiNamespace(fieldNode.desc);

                        JFieldVar fieldVar = this.definedClass.field(mods, directClass(fieldType), fieldName);
                        if (isFinal) {
                            fieldVar.init(getDefaultValue(fieldType));
                        }
                    }
                }

                for (MethodNode methodNode : classNode.methods) {
                    if (methodNode.name.equals("<init>")) {
                        continue;
                    }

                    boolean isPublic = (methodNode.access & ACC_PUBLIC) != 0;
                    boolean isProtected = (methodNode.access & ACC_PROTECTED) != 0;
                    boolean isStatic = (methodNode.access & ACC_STATIC) != 0;

                    if (isPublic || isProtected) {
                        int mods = isPublic ? JMod.PUBLIC : JMod.PROTECTED;
                        mods |= isStatic ? JMod.STATIC : 0;

                        SignatureReader signatureReader = new SignatureReader(methodNode.desc)
                        SignatureNode signatureNode = new SignatureNode();
                        signatureReader.accept(signatureNode);

                        String methodName = toApiNamespace(methodNode.name);
                        String returnType = toApiNamespace(signatureNode.getReturnType().getType());

                        JMethod methodDef = definedClass.method(mods, directClass(returnType), methodName);

                        for (int i = 0; i < signatureNode.getArguments().size(); i++) {
                            SignatureNode argument = signatureNode.getArguments().get(i);
                            String argumentName = methodNode.localVariables.get(i + (isStatic ? 0 : 1)).name;
                            if (argumentName.equals("this")) {
                                argumentName = methodNode.localVariables.get(i + (isStatic ? 1 : 2)).name;
                            }

                            String argumentType = toApiNamespace(argument.type);
                            methodDef.param(directClass(argumentType), argumentName);
                        }

                        JBlock body = methodDef.body()
                        body.directStatement("/* API stub */");

                        if (!returnType.equals("void")) {
                            body._return(getDefaultValue(returnType));
                        }
                    }
                }

                this.model.build(orbitSource);
            }
            return;
        }

        api = false;
        for (MethodNode methodNode : classNode.methods) {
            for (AnnotationNode annotationNode : methodNode.visibleAnnotations) {
                if (annotationNode.desc.equals(apiAnnotation)) {
                    api = true;
                    break;
                }
            }
        }

        if (api) {
            File orbitDir = new File(project.getBuildDir(), "orbit");
            File orbitSource = new File(new File(new File(orbitDir, "src"), "api"), "java");
            orbitSource.mkdirs();

            model = new JCodeModel();
            String selfClass = toApiNamespace(classNode.name);
            definedClass = this.model._class(selfClass, ClassType.INTERFACE);

            for (MethodNode methodNode : classNode.methods) {
                api = false;
                for (AnnotationNode annotationNode : methodNode.visibleAnnotations) {
                    if (annotationNode.desc.equals(apiAnnotation)) {
                        api = true;
                        break;
                    }
                }

                boolean isPublic = (methodNode.access & ACC_PUBLIC) != 0;
                if (api && isPublic) {
                    SignatureReader signatureReader = new SignatureReader(methodNode.desc)
                    SignatureNode signatureNode = new SignatureNode();
                    signatureReader.accept(signatureNode);

                    String methodName = toApiNamespace(methodNode.name);
                    String returnType = toApiNamespace(signatureNode.getReturnType().getType());

                    JMethod methodDef = definedClass.method(JMod.PUBLIC, directClass(returnType), methodName);

                    for (int i = 0; i < signatureNode.getArguments().size(); i++) {
                        SignatureNode argument = signatureNode.getArguments().get(i);
                        LocalVariableNode localVariable = methodNode.localVariables.get(i + (isStatic ? 0 : 1)).name;

                        String argumentType = toApiNamespace(argument.type);
                        methodDef.param(directClass(argumentType), localVariable.name);
                    }
                }
            }

            this.model.build(orbitSource);
        }
    }

    private void processDirectory(File classesDir) {
        for (File file : classesDir.listFiles()) {
            if (file.isDirectory()) {
                processDirectory(file);
            } else {
                processClass(file);
            }
        }
    }
}

package solar.dimensions.orbit.util;

import lombok.Getter;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ASM5;

public class SignatureNode extends SignatureVisitor {
    @Getter
    private SignatureNode returnType;

    @Getter
    private List<SignatureNode> arguments = new ArrayList<>();

    @Getter
    private String type;

    public SignatureNode() {
        super(ASM5);
    }

    @Override
    public SignatureVisitor visitReturnType() {
        returnType = new SignatureNode();
        return returnType;
    }

    @Override
    public void visitBaseType(char c) {
        type = baseTypeToName(c);
    }

    @Override
    public void visitClassType(String s) {
        type = s;
    }

    @Override
    public SignatureVisitor visitParameterType() {
        SignatureNode parameter = new SignatureNode();
        arguments.add(parameter);
        return parameter;
    }



    private String baseTypeToName(char c) {
        switch (c) {
            case 'B':
                return "byte";
            case 'C':
                return "char";
            case 'D':
                return "double";
            case 'F':
                return "float";
            case 'I':
                return "integer";
            case 'J':
                return "long";
            case 'S':
                return "short";
            case 'V':
                return "void";
            case 'Z':
                return "boolean";
        }
        return "<error>";
    }
}

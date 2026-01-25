package dev.spacetivity.tobi.database.hytale.config;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("dev.spacetivity.tobi.database.hytale.config.AutoCodec")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class CodecProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(AutoCodec.class)) {

            TypeElement clazz = (TypeElement) element;
            String className = clazz.getSimpleName().toString();
            String packageName = processingEnv.getElementUtils().getPackageOf(clazz).toString();

            String generatedClassName = className + "_Codec";

            StringBuilder code = new StringBuilder();
            code.append("package ").append(packageName).append(";\n\n");
            code.append("import com.hypixel.hytale.codec.*;\n");
            code.append("import com.hypixel.hytale.codec.builder.*;\n\n");

            code.append("public final class ").append(generatedClassName).append(" {\n");
            code.append("  public static final BuilderCodec<").append(className).append("> CODEC =\n");
            code.append("    BuilderCodec.builder(").append(className).append(".class, ").append(className).append("::new)\n");

            for (Element field : clazz.getEnclosedElements()) {
                CodecField ann = field.getAnnotation(CodecField.class);
                if (ann == null) continue;

                String fieldName = field.getSimpleName().toString();
                String key = ann.value();
                String type = field.asType().toString();

                String codec = switch (type) {
                    case "java.lang.String" -> "Codec.STRING";
                    case "int" -> "Codec.INTEGER";
                    default -> throw new RuntimeException("Unsupported type: " + type);
                };

                code.append("      .append(new KeyedCodec<>(\"").append(key).append("\", ").append(codec).append("),\n");
                code.append("          (obj, val, info) -> obj.set")
                        .append(cap(fieldName)).append("(val),\n");
                code.append("          (obj, info) -> obj.get")
                        .append(cap(fieldName)).append("())\n");
                code.append("      .add()\n");
            }

            code.append("      .build();\n");
            code.append("}\n");

            writeFile(packageName + "." + generatedClassName, code.toString());
        }
        return true;
    }

    private String cap(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private void writeFile(String qualifiedClassName, String code) {
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(qualifiedClassName);
            try (Writer writer = file.openWriter()) {
                writer.write(code);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write generated codec class", e);
        }
    }
}

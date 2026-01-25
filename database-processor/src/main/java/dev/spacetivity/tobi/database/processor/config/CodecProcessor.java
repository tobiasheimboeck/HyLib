package dev.spacetivity.tobi.database.processor.config;

import dev.spacetivity.tobi.database.api.config.AutoCodec;
import dev.spacetivity.tobi.database.api.config.CodecField;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("dev.spacetivity.tobi.database.api.config.AutoCodec")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class CodecProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(AutoCodec.class)) {

            TypeElement clazz = (TypeElement) element;
            String className = clazz.getSimpleName().toString();
            String packageName = processingEnv.getElementUtils().getPackageOf(clazz).toString();
            String generatedName = className + "_Codec";

            StringBuilder code = new StringBuilder("""
                package %s;

                import com.hypixel.hytale.codec.*;
                import com.hypixel.hytale.codec.builder.*;
                import com.hypixel.hytale.codec.KeyedCodec;

                public final class %s {
                    private static final BuilderCodec<%s> CODEC_BASE =
                        BuilderCodec.builder(%s.class, %s::new)
                """.formatted(packageName, generatedName, className, className, className));

            for (Element field : clazz.getEnclosedElements()) {
                CodecField ann = field.getAnnotation(CodecField.class);
                if (ann == null) continue;

                String fieldName = field.getSimpleName().toString();
                String key = ann.value();
                String type = field.asType().toString();

                String codec = resolveCodec(type);
                String setter = "set" + cap(fieldName);
                String getter = "get" + cap(fieldName);

                String defaultCode;
                if (ann.hasDefault()) {
                    String defaultValue = parseDefault(type, ann.defaultValue());
                    // Apply default in lambda if value is null
                    defaultCode = "val != null ? val : " + defaultValue;
                } else {
                    defaultCode = "val";
                }

                code.append("""
                        .append(new KeyedCodec<>("%s", %s),
                            (obj, val, info) -> obj.%s(%s),
                            (obj, info) -> obj.%s())
                        .add()
                    """.formatted(key, codec, setter, defaultCode, getter));
            }

            code.append("            .build();\n");
            
            // Note: Defaults should be initialized as field initializers in the config class
            // to ensure they're applied even when keys are missing from the config file
            code.append("    public static final BuilderCodec<%s> CODEC = CODEC_BASE;\n".formatted(className));
            
            code.append("}\n");

            writeFile(packageName + "." + generatedName, code.toString());
        }
        return true;
    }

    private String cap(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }


    private String resolveCodec(String type) {
        return switch (type) {
            case "java.lang.String" -> "Codec.STRING";
            case "int", "java.lang.Integer" -> "Codec.INTEGER";
            case "long", "java.lang.Long" -> "Codec.LONG";
            case "double", "java.lang.Double" -> "Codec.DOUBLE";
            case "float", "java.lang.Float" -> "Codec.FLOAT";
            case "boolean", "java.lang.Boolean" -> "Codec.BOOLEAN";
            case "byte", "java.lang.Byte" -> "Codec.BYTE";
            case "short", "java.lang.Short" -> "Codec.SHORT";
            case "java.util.UUID" -> "Codec.UUID_STRING";
            case "java.time.Duration" -> "Codec.DURATION";
            case "java.time.Instant" -> "Codec.INSTANT";
            case "java.nio.file.Path" -> "Codec.PATH";
            case "java.util.logging.Level" -> "Codec.LOG_LEVEL";
            default -> {
                if (processingEnv.getTypeUtils().asElement(
                                processingEnv.getTypeUtils().erasure(
                                        processingEnv.getElementUtils().getTypeElement(type).asType()))
                        .getKind() == ElementKind.ENUM) {
                    yield "new FunctionCodec<>(Codec.STRING, " + type + "::valueOf, Enum::name)";
                }
                throw new RuntimeException("Unsupported type: " + type);
            }
        };
    }

    private String parseDefault(String type, String val) {
        return switch (type) {
            case "java.lang.String" -> "\"" + val + "\"";
            case "int", "java.lang.Integer",
                 "long", "java.lang.Long",
                 "double", "java.lang.Double",
                 "float", "java.lang.Float",
                 "boolean", "java.lang.Boolean" -> val;
            case "java.util.UUID" -> "java.util.UUID.fromString(\"" + val + "\")";
            case "java.time.Duration" -> "java.time.Duration.parse(\"" + val + "\")";
            case "java.time.Instant" -> "java.time.Instant.parse(\"" + val + "\")";
            default -> "null";
        };
    }

    private void writeFile(String name, String code) {
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(name);
            try (Writer w = file.openWriter()) {
                w.write(code);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

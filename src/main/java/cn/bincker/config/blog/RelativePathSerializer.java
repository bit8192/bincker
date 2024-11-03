package cn.bincker.config.blog;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import java.io.IOException;
import java.nio.file.Path;

public class RelativePathSerializer extends StdScalarSerializer<Path> {
    protected RelativePathSerializer() {
        super(Path.class);
    }

    @Override
    public void serialize(Path path, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(path.toString());
    }

    public void serializeWithType(Path value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, Path.class, JsonToken.VALUE_STRING));
        this.serialize(value, g, provider);
        typeSer.writeTypeSuffix(g, typeIdDef);
    }
}

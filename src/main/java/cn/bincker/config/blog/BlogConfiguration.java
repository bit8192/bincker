package cn.bincker.config.blog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.text.SimpleDateFormat;

@Configuration
public class BlogConfiguration {
    @Bean
    public ObjectMapper blogMetaObjectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
                .dateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .serializers(new RelativePathSerializer())
                .build();
    }
}

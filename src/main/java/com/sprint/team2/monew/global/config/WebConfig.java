package com.sprint.team2.monew.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
  private final MDCLoggingInterceptor mdcLoggingInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(mdcLoggingInterceptor)
            .addPathPatterns("/**"); // 모든 요청에 적용
  }
  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    for (HttpMessageConverter<?> converter : converters) {
      // JSON 변환기만 찾아서
      if (converter instanceof MappingJackson2HttpMessageConverter) {
        MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;

        // 기본 문자셋을 UTF-8로 설정
        jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);

        // 지원하는 미디어 타입에 charset=UTF-8 명시
        jsonConverter.setSupportedMediaTypes(
            Collections.singletonList(new MediaType("application", "json", StandardCharsets.UTF_8))
        );
      }
    }
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(String.class, LocalDateTime.class, source -> {
      if (source == null || source.isEmpty()) return null;

      if (source.endsWith("Z")) {
        return OffsetDateTime.parse(source)
                .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime();
      } else {
        return LocalDateTime.parse(source);
      }
    });
  }
}

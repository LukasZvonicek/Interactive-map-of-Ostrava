package cz.osu.informatika.mapbackend;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Povolí všechny cesty (i vyhledávání, i analýzu)
                .allowedOrigins("http://127.0.0.1:5500", "http://localhost:5500") // Povolí oba zápisy Live Serveru
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
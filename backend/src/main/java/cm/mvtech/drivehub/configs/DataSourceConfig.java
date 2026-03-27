package cm.mvtech.drivehub.configs;


import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    /**
     * creation d'un dataSource personalisé
     * contenant la logique du multitenant
     * @return DataSourceObject
     */
    @Value("${app.dbname}")
    private String dbname;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/"+dbname);
        ds.setUsername("postgres");
        ds.setPassword("root");
        return ds;
    }
}

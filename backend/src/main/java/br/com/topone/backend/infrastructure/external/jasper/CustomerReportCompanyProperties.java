package br.com.topone.backend.infrastructure.external.jasper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.reports.company")
public class CustomerReportCompanyProperties {

    private String legalName;
    private String tradeName;
    private String taxId;
    private String phone;
    private String email;
    private String addressLine;
}


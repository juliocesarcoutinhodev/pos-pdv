package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.cnpj.CnpjLookupResult;
import br.com.topone.backend.application.usecase.cnpj.CnpjLookupUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CnpjControllerTest {

    @Mock
    private CnpjLookupUseCase cnpjLookupUseCase;

    @InjectMocks
    private CnpjController controller;

    @Test
    void shouldReturnCnpjDataFromUseCase() {
        when(cnpjLookupUseCase.execute("37335118000180")).thenReturn(sampleResult());

        var response = controller.lookup("37335118000180");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().taxId()).isEqualTo("37335118000180");
        assertThat(response.getBody().name()).isEqualTo("CNPJA TECNOLOGIA LTDA");
    }

    private CnpjLookupResult sampleResult() {
        return new CnpjLookupResult(
                OffsetDateTime.parse("2026-04-09T12:32:05Z"),
                "37335118000180",
                "CNPJA TECNOLOGIA LTDA",
                "Cnpja",
                LocalDate.parse("2020-06-05"),
                BigDecimal.valueOf(1000),
                true,
                new CnpjLookupResult.Nature(2062, "Sociedade Empresaria Limitada"),
                new CnpjLookupResult.CompanySize(1, "ME", "Microempresa"),
                LocalDate.parse("2020-06-05"),
                new CnpjLookupResult.CompanyStatus(2, "Ativa"),
                new CnpjLookupResult.Address(
                        3550308,
                        "Avenida Brig Faria Lima",
                        "2369",
                        "Conj 1102",
                        "Jardim Paulistano",
                        "Sao Paulo",
                        "SP",
                        "01452922",
                        new CnpjLookupResult.Address.Country(76, "Brasil")
                ),
                List.of(new CnpjLookupResult.Phone("MOBILE", "11", "71564144")),
                List.of(new CnpjLookupResult.Email("CORPORATE", "fazenda@cnpja.com", "cnpja.com")),
                new CnpjLookupResult.Activity(6311900, "Tratamento de dados"),
                List.of(new CnpjLookupResult.Activity(6201501, "Desenvolvimento de programas")),
                List.of(
                        new CnpjLookupResult.Member(
                                LocalDate.parse("2020-06-05"),
                                new CnpjLookupResult.Member.Person("Etienne Rodrigues Bechara", "NATURAL", "***538418**", "31-40"),
                                new CnpjLookupResult.Member.MemberRole(49, "Socio-Administrador")
                        )
                )
        );
    }
}

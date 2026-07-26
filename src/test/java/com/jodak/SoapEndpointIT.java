package com.jodak;

import com.jodak.entities.Athlete;
import com.jodak.entities.Country;
import com.jodak.entities.Discipline;
import com.jodak.entities.Epreuve;
import com.jodak.entities.Resultat;
import com.jodak.enums.Gender;
import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.CountryRepository;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.repositories.EpreuveRepository;
import com.jodak.repositories.ResultatRepository;
import com.jodak.soap.xml.GetAthleteRequest;
import com.jodak.soap.xml.GetAthleteResponse;
import com.jodak.soap.xml.GetMedalTableRequest;
import com.jodak.soap.xml.GetMedalTableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.ws.client.core.WebServiceTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration HTTP du Web Service SOAP : valide de bout en bout le servlet, le WSDL et le
 * marshalling JAXB, en réutilisant le conteneur PostgreSQL singleton des tests d'intégration.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SoapEndpointIT {

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", AbstractIntegrationTest.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", AbstractIntegrationTest.POSTGRES::getUsername);
        registry.add("spring.datasource.password", AbstractIntegrationTest.POSTGRES::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private ResultatRepository resultatRepository;
    @Autowired
    private AthleteRepository athleteRepository;
    @Autowired
    private EpreuveRepository epreuveRepository;
    @Autowired
    private DisciplineRepository disciplineRepository;
    @Autowired
    private CountryRepository countryRepository;

    private Long athleteId;

    @BeforeEach
    void seed() {
        resultatRepository.deleteAll();
        athleteRepository.deleteAll();
        epreuveRepository.deleteAll();
        disciplineRepository.deleteAll();

        Discipline discipline = disciplineRepository.save(Discipline.builder().name("Athlétisme").build());
        Country country = countryRepository.findAll().get(0);
        Epreuve epreuve = epreuveRepository.save(Epreuve.builder()
                .label("100 m").discipline(discipline).eventDate(LocalDate.of(2024, 8, 4)).build());
        Athlete athlete = athleteRepository.save(Athlete.builder()
                .lastName("Bolt").firstName("Usain").gender(Gender.MALE)
                .birthDate(LocalDate.of(1986, 8, 21)).country(country).discipline(discipline)
                .heightCm(195).weightKg(94).build());
        athleteId = athlete.getId();

        Resultat resultat = Resultat.builder()
                .epreuve(epreuve).athlete(athlete).rankPosition(1).build();
        resultat.assignMedalFromRank();
        resultatRepository.save(resultat);
    }

    private WebServiceTemplate template() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(
                GetAthleteRequest.class, GetAthleteResponse.class,
                GetMedalTableRequest.class, GetMedalTableResponse.class,
                com.jodak.soap.xml.Athlete.class, com.jodak.soap.xml.MedalTableRow.class);
        return new WebServiceTemplate(marshaller);
    }

    private String uri() {
        return "http://localhost:" + port + "/ws";
    }

    @Test
    @DisplayName("GetAthleteRequest renvoie l'athlète via SOAP")
    void getAthleteViaSoap() {
        GetAthleteRequest request = new GetAthleteRequest();
        request.setId(athleteId);

        GetAthleteResponse response = (GetAthleteResponse) template().marshalSendAndReceive(uri(), request);

        assertThat(response.getAthlete().getLastName()).isEqualTo("Bolt");
        assertThat(response.getAthlete().getDiscipline()).isEqualTo("Athlétisme");
        assertThat(response.getAthlete().getNationalityCode()).isNotBlank();
    }

    @Test
    @DisplayName("GetMedalTableRequest renvoie le tableau des médailles via SOAP")
    void getMedalTableViaSoap() {
        GetMedalTableResponse response =
                (GetMedalTableResponse) template().marshalSendAndReceive(uri(), new GetMedalTableRequest());

        assertThat(response.getRow()).hasSize(1);
        assertThat(response.getRow().get(0).getGold()).isEqualTo(1);
        assertThat(response.getRow().get(0).getTotal()).isEqualTo(1);
    }
}

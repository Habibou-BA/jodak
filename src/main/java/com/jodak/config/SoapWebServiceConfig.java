package com.jodak.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.jodak.soap.xml.Athlete;
import com.jodak.soap.xml.GetAthleteRequest;
import com.jodak.soap.xml.GetAthleteResponse;
import com.jodak.soap.xml.GetMedalTableRequest;
import com.jodak.soap.xml.GetMedalTableResponse;
import com.jodak.soap.xml.MedalTableRow;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Configuration du Web Service SOAP (Spring WS) destiné au système d'information historique.
 * Le WSDL est publié à partir du contrat XSD ; le marshalling utilise les classes JAXB générées.
 */
@EnableWs
@Configuration
public class SoapWebServiceConfig extends WsConfigurerAdapter {

    private static final String NAMESPACE = "http://jodak.com/olympics/soap";

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "olympics")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema olympicsSchema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("OlympicsPort");
        definition.setLocationUri("/ws");
        definition.setTargetNamespace(NAMESPACE);
        definition.setSchema(olympicsSchema);
        return definition;
    }

    @Bean
    public XsdSchema olympicsSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/olympics.xsd"));
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(
                GetAthleteRequest.class, GetAthleteResponse.class,
                GetMedalTableRequest.class, GetMedalTableResponse.class,
                Athlete.class, MedalTableRow.class);
        return marshaller;
    }
}

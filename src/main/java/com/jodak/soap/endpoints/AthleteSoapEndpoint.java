package com.jodak.soap.endpoints;

import com.jodak.services.interfaces.AthleteService;
import com.jodak.soap.generated.GetAthleteRequest;
import com.jodak.soap.generated.GetAthleteResponse;
import com.jodak.soap.mappers.SoapMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * Endpoint SOAP (lecture seule) de consultation d'un athlète. Réutilise le service applicatif.
 */
@Endpoint
@RequiredArgsConstructor
public class AthleteSoapEndpoint {

    static final String NAMESPACE = "http://jodak.com/olympics/soap";

    private final AthleteService athleteService;
    private final SoapMapper soapMapper;

    @PayloadRoot(namespace = NAMESPACE, localPart = "GetAthleteRequest")
    @ResponsePayload
    public GetAthleteResponse getAthlete(@RequestPayload GetAthleteRequest request) {
        GetAthleteResponse response = new GetAthleteResponse();
        response.setAthlete(soapMapper.toSoapAthlete(athleteService.getById(request.getId())));
        return response;
    }
}

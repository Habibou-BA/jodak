package com.jodak.soap.endpoints;

import com.jodak.services.interfaces.MedalTableService;
import com.jodak.soap.generated.GetMedalTableRequest;
import com.jodak.soap.generated.GetMedalTableResponse;
import com.jodak.soap.mappers.SoapMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * Endpoint SOAP (lecture seule) du tableau des médailles. Réutilise le service applicatif.
 */
@Endpoint
@RequiredArgsConstructor
public class MedalTableSoapEndpoint {

    static final String NAMESPACE = "http://jodak.com/olympics/soap";

    private final MedalTableService medalTableService;
    private final SoapMapper soapMapper;

    @PayloadRoot(namespace = NAMESPACE, localPart = "GetMedalTableRequest")
    @ResponsePayload
    public GetMedalTableResponse getMedalTable(@RequestPayload GetMedalTableRequest request) {
        GetMedalTableResponse response = new GetMedalTableResponse();
        medalTableService.getMedalTable().forEach(row -> response.getRow().add(soapMapper.toSoapRow(row)));
        return response;
    }
}

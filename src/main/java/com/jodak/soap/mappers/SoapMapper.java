package com.jodak.soap.mappers;

import com.jodak.dtos.athlete.AthleteResponse;
import com.jodak.dtos.medaltable.MedalTableRowResponse;
import com.jodak.soap.generated.Athlete;
import com.jodak.soap.generated.MedalTableRow;
import org.springframework.stereotype.Component;

/**
 * Conversion des DTO applicatifs vers les objets XML (JAXB) exposés par le service SOAP.
 */
@Component
public class SoapMapper {

    public Athlete toSoapAthlete(AthleteResponse source) {
        Athlete target = new Athlete();
        target.setId(source.id());
        target.setLastName(source.lastName());
        target.setFirstName(source.firstName());
        target.setGender(source.gender().name());
        target.setBirthDate(source.birthDate().toString());
        target.setNationalityCode(source.country().code());
        target.setNationalityName(source.country().name());
        target.setDiscipline(source.discipline().name());
        target.setHeightCm(source.heightCm());
        target.setWeightKg(source.weightKg());
        return target;
    }

    public MedalTableRow toSoapRow(MedalTableRowResponse source) {
        MedalTableRow target = new MedalTableRow();
        target.setRank(source.rank());
        target.setCountryCode(source.country().code());
        target.setCountryName(source.country().name());
        target.setGold(source.gold());
        target.setSilver(source.silver());
        target.setBronze(source.bronze());
        target.setTotal(source.total());
        return target;
    }
}

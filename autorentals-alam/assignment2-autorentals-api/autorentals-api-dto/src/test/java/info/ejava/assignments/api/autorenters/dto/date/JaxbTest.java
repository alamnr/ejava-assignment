package info.ejava.assignments.api.autorenters.dto.date;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import info.ejava.examples.common.dto.MessageDTO;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JaxbTest extends MarshallingTestBase {

    
    public void init(){

    }
    @Override
    protected <T> String marshal(T object) throws Exception {
        if(object == null){
            return "";
        }
        JAXBContext jbx =JAXBContext.newInstance(object.getClass());

        Marshaller marshaller = jbx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter buffer = new StringWriter();
        marshaller.marshal(object, buffer);
        log.info("{} toXML {} : ", object, buffer);
        return buffer.toString();
    }

    @Override
    protected <T> T unmarshal(Class<T> type, String buffer) throws Exception {
        if(buffer == null) {
            return null;
        }

        JAXBContext jbx =JAXBContext.newInstance(type);
        Unmarshaller unmarshaller = jbx.createUnmarshaller();

        ByteArrayInputStream bis = new ByteArrayInputStream(buffer.getBytes(StandardCharsets.UTF_8));
        Object obj = unmarshaller.unmarshal(bis);
        T result = type.cast(obj);
        log.info("{} fromXML {} : ",buffer, result);
        return result;

    }


    private <T> T marshal_and_unmarshal(T object,Class<T> type) throws Exception {

        String jaxbXml = marshal(object);
        T result = unmarshal(type, jaxbXml);
        return result;
    }

   

    @Test
    void renter_dto_marshal() throws Exception {
        // given / arrange
        RenterDTO actual = renterDTOFactory.make();

        // when / act
        RenterDTO expected = marshal_and_unmarshal(actual, RenterDTO.class);
        
        // then / avaluate / assert

        BDDAssertions.then(expected.getDob()).isEqualTo(actual.getDob());
        BDDAssertions.then(expected.getEmail()).isEqualTo(actual.getEmail());
        BDDAssertions.then(expected.getFirstName()).isEqualTo(actual.getFirstName());
        BDDAssertions.then(expected.getLastName()).isEqualTo(actual.getLastName());
        BDDAssertions.then(expected.getUsername()).isNull();
    }

    

    @Test
    void renterList_dto_marshal() throws Exception {

        // given / arrange
        RenterListDTO renterList = renterDTOFactory.listBuilder().make(3, 3, RenterDTOFactory.withId);

        // when / act
        RenterListDTO result  = marshal_and_unmarshal(renterList, RenterListDTO.class);

        // then  / evaluate / assert
        BDDAssertions.then(result.getCount()).isEqualTo(renterList.getCount());
        Map<String,RenterDTO> renterMap = renterList.getRenters().stream().collect(Collectors.toMap(RenterDTO::getId,q->q));
        for (RenterDTO actual : renterList.getRenters()) {
            RenterDTO expected = renterMap.get(actual.getId());

            BDDAssertions.then(expected.getDob()).isEqualTo(actual.getDob());
            BDDAssertions.then(expected.getEmail()).isEqualTo(actual.getEmail());
            BDDAssertions.then(expected.getFirstName()).isEqualTo(actual.getFirstName());
            BDDAssertions.then(expected.getLastName()).isEqualTo(actual.getLastName());
            BDDAssertions.then(expected.getUsername()).isNull();
        }
    }


    @Test
    void msg_dto_marshal() throws Exception {
        // given / arrange
        //MessageDTO msg = MessageDTO.builder().message("a msg").url("/api/msgs").build();
        MessageDTO msg = new MessageDTO("http://testing", "POST", 200 , 
                                        "OK", "ok msg", "default msg", Instant.now());

        // when / act
        MessageDTO expected = marshal_and_unmarshal(msg, MessageDTO.class);

        // then / evaluate / assert

        BDDAssertions.then(expected.getMessage()).isEqualTo(msg.getMessage());
        BDDAssertions.then(expected.getUrl()).isEqualTo(msg.getUrl());

    }


    @ParameterizedTest
    @MethodSource("read_from_formats")
    public void parse_date(String dateText, String name, Date date) throws Exception {
        //given - a known date with a specific format added to the marshalled body
        String body = get_marshalled_adate(dateText);
        log.info("{} => {}", name, dateText);

        //when unmarshalled
        ADate dates=null;
        try {
            dates = unmarshal(ADate.class, body);
        } catch (Exception ex) {
            log.debug("{}", ex.toString());
            fail(ex.toString());
        }
        Assertions.assertThat(dates.getDate()).isEqualTo(date);
    }

    

    @ParameterizedTest
    @MethodSource("read_by_formats")
    public void marshal_dates(ZonedDateTime zdt, String name, Object format) throws Exception {
        //marshall an object with a date using the baseline parser
        ADate dates = ADate.of(zdt);
        String text = marshal(dates);
        name = (format instanceof DateTimeFormatter) ? name : (String)format;

        //extract the date out of the text payload
        String dateText = get_date(text);
        log.info("{} {}", dateText, dates);
        //log.info("{} {} parsed {}", format, (tz==null? "null":tz.getID()), dateText);
        log.info("{} parsed {}", name, dateText);

        //parse it with a variable DTF format
        DateTimeFormatter dtf = null;
        if (format instanceof DateTimeFormatter) {
            dtf = (DateTimeFormatter) format;
        } else {
            dtf = DateTimeFormatter.ofPattern((String)format);
        }
        Date date = Date.from(ZonedDateTime.parse(dateText, dtf).toInstant());
        Assertions.assertThat(date).isEqualTo(Date.from(zdt.toInstant()));
    }


 @Override
    protected String get_marshalled_adate(String dateText) {
        return String.format(DATES_XML,dateText);
    }

    @Override
    public String get_date(String marshalledQuote) {
        Pattern pattern = Pattern.compile(".*<date>(.+)<\\/date>.*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(marshalledQuote);

        if (matcher.matches()) {
            String date = matcher.group(1);
            return date;
        }
        return null;
    }

    @Override
    protected boolean canParseFormat(String format, ZoneOffset tzo) {
        //can parse Z and +05:00 but cannot parse +05
//        if (format.equals(ISO_8601_DATETIME5_FORMAT) ||
//                (tz!=null && tz.getID().equals(TimeZone.getTimeZone("UTC").getID()))) {
//            return true;
//        }
//        return false;
        return true;
    }    
}

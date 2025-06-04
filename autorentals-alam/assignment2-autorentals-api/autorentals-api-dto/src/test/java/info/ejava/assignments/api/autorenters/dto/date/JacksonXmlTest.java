package info.ejava.assignments.api.autorenters.dto.date;

import java.io.StringWriter;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import info.ejava.assignments.api.autorenters.dto.renters.RenterDTO;
import info.ejava.assignments.api.autorenters.dto.renters.RenterDTOFactory;
import info.ejava.assignments.api.autorenters.dto.renters.RenterListDTO;
import info.ejava.examples.common.dto.MessageDTO;
import info.ejava.examples.common.dto.adapters.ISODateFormat;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class JacksonXmlTest extends MarshallingTestBase {

    private ObjectMapper mapper;

    @BeforeEach
    public void init() {
        mapper = new Jackson2ObjectMapperBuilder()
                        .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
                        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                        .dateFormat(new ISODateFormat())
                        .createXmlMapper(true)
                        .build();
    }

    @Override
    protected <T> String marshal(T object) throws Exception {
        StringWriter buffer = new StringWriter();
        mapper.writeValue(buffer, object);
        log.info("{} toXML: {}",object, buffer);
        return buffer.toString();    
    }

    @Override
    protected <T> T unmarshal(Class<T> type, String buffer) throws Exception {
        T result = mapper.readValue(buffer,type);
        log.info("{} fromXML: {}", buffer, result);
        return result;
    }

    private <T> T marshall_and_unmarshal(T object, Class<T> type) throws Exception {
        String jacksonXml  = marshal(object);
        T result = unmarshal(type, jacksonXml);
        return result;
    }

    @Test
    void renter_dto_marshal() throws Exception {
        // given / arrange 
        RenterDTO renter = renterDTOFactory.make();
        // when / act
        RenterDTO result = marshall_and_unmarshal(renter, RenterDTO.class);

        // then / evaluate / assert

        BDDAssertions.then(result.getId()).isEqualTo(renter.getId());
        BDDAssertions.then(result.getDob()).isEqualTo(renter.getDob());
        BDDAssertions.then(result.getEmail()).isEqualTo(renter.getEmail());
        BDDAssertions.then(result.getFirstName()).isEqualTo(renter.getFirstName());
        BDDAssertions.then(result.getLastName()).isEqualTo(renter.getLastName());
        BDDAssertions.then(result.getUsername()).isNull();

        log.info(" date = {} ", renter.getDob());
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE;
        log.info("dtf = {} ", dtf.format(renter.getDob()));
    }

    @Test
    void renterList_dto_marshal() throws Exception {
        // given / arrange
        RenterListDTO rentersList = renterDTOFactory.listBuilder().make(3,3,RenterDTOFactory.withId);

        // when / act
        RenterListDTO result = marshall_and_unmarshal(rentersList, RenterListDTO.class);

        // then
        BDDAssertions.then(result.getCount()).isEqualTo(rentersList.getCount());
        Map<String,RenterDTO> renterMap = rentersList.getRenters().stream().collect(Collectors.toMap(RenterDTO::getId, q->q));
        for (RenterDTO actual : rentersList.getRenters()) {
                RenterDTO expected  = renterMap.get(actual.getId());
                BDDAssertions.then(expected.getDob()).isEqualTo(actual.getDob());
                BDDAssertions.then(expected.getEmail()).isEqualTo(actual.getEmail());
                BDDAssertions.then(expected.getFirstName()).isEqualTo(actual.getFirstName());
                BDDAssertions.then(expected.getLastName()).isEqualTo(actual.getLastName());
                BDDAssertions.then(expected.getUsername()).isNull();
        }

    }


    @Test
    void message_dto_marshal() throws Exception {
        // given / arrange
        //MessageDTO msg = MessageDTO.builder().message("a message").url("/api/msgs").build();
        MessageDTO msg = new MessageDTO("http://testing", "POST", 200 , 
                                        "OK", "ok msg", "default msg", Instant.now());

        // when / act
        MessageDTO result = marshall_and_unmarshal(msg, MessageDTO.class);

        // then / evaluae / assert

        BDDAssertions.then(result.getMessage()).isEqualTo(msg.getMessage());

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
        Pattern pattern = Pattern.compile(".*<date xmlns=\"\">(.+)<\\/date>.*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(marshalledQuote);

        if (matcher.matches()) {
            String date = matcher.group(1);
            return date;
        }
        return null;
    }


    @Override
    protected boolean canParseFormat(String format, ZoneOffset tzo) {
        return tzo==ZoneOffset.UTC;
    }

    
}


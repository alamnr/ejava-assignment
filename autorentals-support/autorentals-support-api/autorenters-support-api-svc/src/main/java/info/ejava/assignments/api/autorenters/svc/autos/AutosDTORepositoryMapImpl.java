package info.ejava.assignments.api.autorenters.svc.autos;

import info.ejava.assignments.api.autorenters.dto.autos.AutoDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class AutosDTORepositoryMapImpl implements AutosDTORepository {
    private final AtomicInteger ID = new AtomicInteger(new SecureRandom().nextInt(1000));
    private final Map<String, AutoDTO> autos = new ConcurrentHashMap<>();

    @Override
    public AutoDTO save(AutoDTO auto) {
        if (auto!=null) {
            String id = auto.getId()!=null ?
                    auto.getId() :
                    "auto-" + Integer.valueOf(ID.incrementAndGet()).toString();
            auto.setId(id);
            autos.put(auto.getId(), auto);
        }
        return auto;
    }

    @Override
    public Optional<AutoDTO> findById(String id) {
        AutoDTO auto = autos.get(id);
        return auto!=null ? Optional.of(auto) : Optional.empty();
    }

    @Override
    public boolean existsById(String id) {
        return id!=null && autos.containsKey(id);
    }

    @Override
    public Page<AutoDTO> findAll(Pageable pageable) {
        Predicate<AutoDTO> matchAll = candidate -> true;
        return find(matchAll, pageable);
    }

    /**
     * Returns autos that match the provided example prototype.
     * @param example containing prototype values to match
     * @param pageable containing offset and limit unless unpaged
     * @return page of matching autos ordered by ID
     */
    @Override
    public Page<AutoDTO> findAll(Example<AutoDTO> example, Pageable pageable) {
        AutoDTO probe = example.getProbe();
        List<Predicate<AutoDTO>> predicates = new ArrayList<>();
        if (probe.getId()!=null) {
            predicates.add(candidate-> Objects.equals(probe.getId(), candidate.getId()));
        }
        if (probe.getPassengers()!=null) {
            predicates.add(candidate-> Objects.equals(probe.getPassengers(), candidate.getPassengers()));
        }
        if (probe.getMake()!=null) {
            predicates.add(candidate-> Objects.equals(probe.getMake(), candidate.getMake()));
        }
        if (probe.getModel()!=null) {
            predicates.add(candidate-> Objects.equals(probe.getModel(), candidate.getModel()));
        }
        if (probe.getFuelType()!=null) {
            predicates.add(candidate-> Objects.equals(probe.getFuelType(), candidate.getFuelType()));
        }
        if (probe.getDailyRate()!=null) {
            predicates.add(candidate-> Objects.equals(probe.getDailyRate(), candidate.getDailyRate()));
        }

        Predicate<AutoDTO> query = predicates.stream().reduce(Predicate::and).orElse(x->true);
        return find(query, pageable);
    }

    /**
     * Return autos matching the passenger count.
     * @param minPassengers inclusive
     * @param maxPassengers inclusive
     * @param pageable with offset and limit unless unpaged
     * @return page of matching autos ordered by ID
     */
    @Override
    public Page<AutoDTO> findByPassengersBetween(int minPassengers, int maxPassengers, Pageable pageable) {
        Predicate<AutoDTO> min = candidate->candidate.getPassengers()>=minPassengers;
        Predicate<AutoDTO> max = candidate->candidate.getPassengers()<=maxPassengers;

        Predicate<AutoDTO> query = Stream.of(min, max).reduce(Predicate::and).orElse(x->true);
        return find(query, pageable);
    }

    /**
     * Return autos matching the dailyRate.
     * @param minDailyRate inclusive
     * @param maxDailyRate inclusive
     * @param pageable with offset and limit unless unpaged
     * @return page of matching autos ordered by ID
     */
    @Override
    public Page<AutoDTO> findByDailyRateBetween(BigDecimal minDailyRate, BigDecimal maxDailyRate, Pageable pageable) {
        Predicate<AutoDTO> min = candidate-> candidate.getDailyRate().compareTo(minDailyRate) >= 0;
        Predicate<AutoDTO> max = candidate->candidate.getDailyRate().compareTo(maxDailyRate) <= 0;

        Predicate<AutoDTO> query = Stream.of(min, max).reduce(Predicate::and).orElse(x->true);
        return find(query, pageable);
    }

    /**
     * Return a page of matching autos.
     * @param predicate decision whether to include or exclude the canidate auto
     * @param pageable with offset and limit unless unpaged. Client-provided sort is not
     *                 supported.
     * @return page of matching autos ordered by ID
     */
    protected Page<AutoDTO> find(Predicate<AutoDTO> predicate, Pageable pageable) {
        List<AutoDTO> foundObjects = pageable.isUnpaged() ?
                autos.values().stream()
                        .filter(predicate)
                        .toList() :
                autos.values().stream()
                        .filter(predicate)
                        //TODO: sort
                        .sorted((l,r)->StringUtils.compare(l.getId(), r.getId()))
                        .skip(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .toList();
        long total = pageable.isUnpaged() ?
                autos.size() :
                autos.values().stream().filter(predicate).count();

        return new PageImpl<>(foundObjects, pageable, total);
    }

    @Override
    public long count() {
        return autos.size();
    }

    @Override
    public void deleteById(String id) {
        if (id!=null) {
            autos.remove(id);
        }
    }

    @Override
    public void deleteAll() {
        autos.clear();
    }
}

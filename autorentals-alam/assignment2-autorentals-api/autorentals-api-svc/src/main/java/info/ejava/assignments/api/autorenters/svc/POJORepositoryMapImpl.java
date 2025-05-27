package info.ejava.assignments.api.autorenters.svc;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.RequiredArgsConstructor;

/*
 * This class implements the full spring data CRUD repository interface
 * for a template object type using a String primary key
 */

@RequiredArgsConstructor
public class POJORepositoryMapImpl<T> implements POJORepository<T> {
    
    private final AtomicInteger ID = new AtomicInteger(new SecureRandom().nextInt(1000));
    private final Map<String,T> objects = new ConcurrentHashMap<>();
    private final Function<T,String> getId;
    private final BiConsumer<T,String> setId;
    private final String idPrefix;
    
    protected String nextId() {
        String prefix = null==idPrefix ? "" : idPrefix;
        return prefix + ID.incrementAndGet();
    }

    protected Page<T> findAll(Predicate<T> predicate, Pageable pageable){
        List<T> foundObjects =  pageable.isUnpaged() ?
                            objects.values().stream().filter(predicate).toList() :
                            objects.values().stream().filter(predicate)
                                // TO DO - sort, defaulting to ID        
                            .sorted((l,r) -> StringUtils.compare(getId.apply(l), getId.apply(r)))
                            .skip(pageable.getOffset()).limit(pageable.getPageSize())
                            .toList();
        
        long total = pageable.isUnpaged() ? objects.size(): objects.values().stream().filter(predicate).count();
        return new PageImpl<>(foundObjects,pageable,total);
    }

    protected List<T> findAll(Predicate<T> predicate, long offset, long limit) {
        return objects.values().stream().filter(predicate)
                            // default sort according to ID
                            .sorted((l,r)-> StringUtils.compare(getId.apply(l), getId.apply(r)))
                            .skip(offset).skip(limit).toList();
    }

    protected Optional<T> findFirst(Predicate<T> predicate){
        return objects.values().stream().filter(predicate).findFirst();
    }

    @Override
    public long count() {
        return objects.size();
    }

    @Override
    public <S extends T> S save(S obj) {
        if(null != obj) {
            String id = getId.apply(obj)!=null ? 
                            getId.apply(obj) : nextId();
            setId.accept(obj, id);
            objects.put(id, obj);
        }
        return obj;
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        if(null !=entities) {
            List<S> saved = new ArrayList<>();
            entities.forEach(e -> saved.add(this.save(e)));
            return saved;
        }
        return null;
    }

    @Override
    public Optional<T> findById(String id){
        T obj = objects.get(id);
        return null!=obj ?  Optional.of(obj) : Optional.empty();
    }

    @Override
    public boolean existsById(String id) {
        return null!=id ?  objects.containsKey(id) : false;
    }

    @Override
    public List<T> findAll(){
        return new ArrayList<>(objects.values());
    }

    @Override
    public List<T> findAllById(Iterable<String> strings){
    
        if(null!=strings){
            List<T> lists = new ArrayList<>();
            strings.forEach(id -> lists.add(objects.get(id)));
            return lists;
        }
        return null;
    }

    @Override
    public Page<T> findAll(Pageable pageable){
        return findAll((v) -> true, pageable);
    }

    @Override
    public void deleteById(String Id){
        if (null != Id){
            objects.remove(Id);
        }
    }

    @Override
    public void deleteAll(){
        objects.clear();
    }

    // methods to round out the interface

    @Override
    public void delete(T obj){
        if(null!=obj){
            deleteById(getId.apply(obj));
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids){
        if(null!=ids){
            ids.forEach(id -> deleteById(id));
        }
    }

    @Override
    public void deleteAll(Iterable<? extends T> objects) {
        if(null != objects){
            for (T t : objects) {
                delete(t);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<T> findAll(Sort sort){

        List<T> list = new ArrayList<>(objects.values());
        if (sort != null && sort.isSorted()) {
        Comparator<T> comparator = null;

        for (Sort.Order order : sort) {
            Comparator<T> fieldComparator = Comparator.comparing(obj -> {
                try {
                    return (Comparable) PropertyUtils.getProperty(obj, order.getProperty());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            if (order.isDescending()) {
                fieldComparator = fieldComparator.reversed();
            }

            comparator = (comparator == null)
                ? fieldComparator
                : comparator.thenComparing(fieldComparator);
        }

        list.sort(comparator);
    }
    return list;
}






}

package info.ejava.alamnr.assignment1.propertysource.rentals;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import info.ejava.assignments.propertysource.autorentals.PropertyCheck;
import lombok.extern.slf4j.Slf4j;

//@SpringBootApplication(scanBasePackages = {"info.ejava.assignments.propertysource.autorentals"})
@SpringBootApplication(scanBasePackageClasses = PropertyCheck.class)
@Slf4j
public class PropertySourceApp {
    
    private static ApplicationContext applicationContext;
    public static void main(String[] args) {
        applicationContext =  SpringApplication.run(PropertySourceApp.class, args);
        //displayAllBeans();
        List<Integer> numbers = Arrays.asList(10,20,30,40,50);
        int sum = numbers.stream().mapToInt(i->i).sum();
        int count  = (int) numbers.stream().count();
        OptionalDouble avg = numbers.stream().mapToInt(i->i).average();
        Optional<Integer> max = numbers.stream().max((i,j) -> i.compareTo(j));
        Optional<Integer> min = numbers.stream().min((i,j)-> i.compareTo(j));
        int total  = numbers.stream().reduce(0, (i,j)-> i+j);

        log.info("sum - {}, average - {}, max - {}, min - {}, total - {}", sum, avg.getAsDouble(), max.get(), min.get(), total);

        printNames("Alice","Jane","Bob");
        printNames(new String[]{"Alice","Jane","Joe"});
        /*
         * Rules of var args 
         * You can pass zero or more arguments
         * Must be last param in the method
         * Only one var arg is allowed per method
         */

         // Thre are   java 8's built in functioonal interfaces that can be used with lambda
         /*
          *    Interface                           Signature                               Use Case
          *    Predicate<T>                        boolean test(T t)                      boolean value function of one argument / Filtering
          *    Function<T, R>                      R apply(T t)                          takes one argument and  produce result  / Transforming
          *    Consumer<T>                          void accept(T t)                      takes one argument and do something / 
          *    Supplier<T>                          T get()                                takes no argument / produces / generating values
          *    Comparator<T>                        int compare(T a, T b)                 sorting
          */

          Predicate<String> startsWithA = s -> s.startsWith("a");
          System.out.println(startsWithA.test("apple"));
          System.out.println(startsWithA.test("banana"));

          List<String> fruits = Arrays.asList("applle","banana","avocado");

          fruits.stream().filter(startsWithA).forEach(i -> System.out.println(i));

          Function<String, Integer>  stringLength = s -> s.length();
          System.out.println(stringLength.apply("hello"));

          List<String> name = Arrays.asList("Tom", "Jerry");
          List<Integer> length = name.stream().map(stringLength).collect(Collectors.toList());
          System.out.println("length - "+ length);

          Consumer<String> print = s -> System.out.println("Hello " + s);
          print.accept("Jeremi");
          name.forEach(print);

          Supplier<String> greeting = () -> "Hello World";

          System.out.println(greeting.get());

          Comparator<String> byLength = (s1,s2) -> s1.compareTo(s2);
          //name.sort((s1,s2)-> s1.compareTo(s2));
          name.sort(byLength);
          System.out.println(name);

          // you can compose functions
          Function<String,String> toUpper = s -> s.toUpperCase();
          Function<String,String> addExcliam = s -> s + "!";
          Function<String,String> excited = toUpper.andThen(addExcliam);
          System.out.println(excited.apply("mokles"));
          
    }
    private static void displayAllBeans() {
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("======================================");
        for(String beanName : allBeanNames) {
            System.out.println(beanName);   
        }        
    }

    private static void printNames(String... names) {
        for (String name : names) {
            log.info("name- {}",name);
        }
    }
}

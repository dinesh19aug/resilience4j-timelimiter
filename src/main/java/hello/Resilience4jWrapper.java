package hello;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.vavr.control.Try;
import org.springframework.stereotype.Component;

@Component
public class Resilience4jWrapper
{
    private final Callable<PaymentVO> callable;
    private final CircuitBreaker circuitBreaker;
    private final TimeLimiter timeLimiter;

    public Resilience4jWrapper()  {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                                                                        .failureRateThreshold(20)
                                                                        .ringBufferSizeInClosedState(5)
                                                                        .waitDurationInOpenState(
                                                                                Duration.ofSeconds(10)).build();
        timeLimiter = TimeLimiter.of(Duration.ofMillis(500));
        Callable<PaymentVO> timeRestricted =
                TimeLimiter.decorateFutureSupplier(timeLimiter, () -> executorService.submit(() -> PaymentController.getPayment()));

        circuitBreaker = CircuitBreaker.of("test", circuitBreakerConfig);
        circuitBreaker.getEventPublisher()
                      .onSuccess(event -> {
                          System.out.println("SUCCESS: ");
                          printSuccessLogs();
                      })
        .onError(event -> {
            System.out.println("ERROR: ");
            printErrorLogs();
        });
        callable = CircuitBreaker.decorateCallable(circuitBreaker, timeRestricted);
    }

    public Try<PaymentVO> run()

    {
        System.out.println("BEFORE: \n");
        printLogs();
        Try<PaymentVO> greeting =  Try.ofCallable(callable);


        return greeting;
    }

    public void printLogs(){
        System.out.println("State: " + circuitBreaker.getState());
        System.out.println("Threshold: " + circuitBreaker.getMetrics().getFailureRate());
        System.out.println("FailedCalls: " + circuitBreaker.getMetrics().getNumberOfFailedCalls());
        System.out.println("Passed Call: " + circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
    }

    public void printSuccessLogs(){
        System.out.println("State: " + circuitBreaker.getState());
        System.out.println("Threshold: " + circuitBreaker.getMetrics().getFailureRate());
        System.out.println("FailedCalls: " + circuitBreaker.getMetrics().getNumberOfFailedCalls());
        System.out.println("Passed Call: " + (circuitBreaker.getMetrics().getNumberOfSuccessfulCalls() + 1));
    }

    public void printErrorLogs(){
        System.out.println("State: " + circuitBreaker.getState());
        System.out.println("Threshold: " + circuitBreaker.getMetrics().getFailureRate());
        System.out.println("FailedCalls: " + (circuitBreaker.getMetrics().getNumberOfFailedCalls() + 1));
        System.out.println("Passed Call: " + circuitBreaker.getMetrics().getNumberOfSuccessfulCalls() );
    }
}

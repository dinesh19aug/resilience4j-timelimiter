package hello.r4j.warppers;

import java.time.Duration;
import java.util.concurrent.Callable;

import hello.PaymentController;
import hello.PaymentVO;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

import io.vavr.control.Try;
import org.springframework.stereotype.Component;

@Component
public class Resilience4jRateLimiterWrapper
{
    private final Callable<PaymentVO> callable;
    private final RateLimiter rateLimiter;

    public Resilience4jRateLimiterWrapper()
    {

        RateLimiterConfig config = RateLimiterConfig.custom()
                                                    .limitRefreshPeriod(Duration.ofSeconds(5))
                                                    .limitForPeriod(2)
                                                    .timeoutDuration(Duration.ofMillis(2000))
                                                    .build();

        // Create registry
        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);
        // Use registry
        rateLimiter = rateLimiterRegistry.rateLimiter("backend");

        rateLimiter.getEventPublisher()
                   .onSuccess(event -> {
                       System.out.println("SUCCESS: ");
                       printLogs();
                   })
                   .onFailure(event -> {
                       System.out.println("ERROR: ");
                       printLogs();
                   });
        callable=RateLimiter.decorateCallable(rateLimiter,  PaymentController::getRateLimit);

    }

    public Try<PaymentVO> run()

    {
        System.out.println("BEFORE:");
        printLogs();
        Try<PaymentVO> greeting =  Try.ofCallable(callable).onFailure(throwable-> printLogs());
        ///rateLimiter.changeLimitForPeriod(100); Explain situation when backend system CPU is 75 then decrease else increase

        return greeting;
    }

    public void printLogs(){

        System.out.println("Available Permission: " + rateLimiter.getMetrics().getAvailablePermissions());
        System.out.println("Waiting Threads: " + rateLimiter.getMetrics().getNumberOfWaitingThreads());

    }
}

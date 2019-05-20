# resilience4j-timelimiter
This project shows you how to implement Resilience4j with Time Limiter

### Steps
- Download the project - git clone https://github.com/dinesh19aug/resilience4j-timelimiter.git 
- cd to project
- mvn clean install
- Run the application - mvn spring-boot:run
- Access the application using postman - GET: http://localhost:8080/push-to-card/v2/payment

### Default _CircuitBreakerConfig_ Setting
The circuit breaker is set for 
- Failure Threshold 20% (If the number of failed calls is more than 20%, it will go in OPEN MODE)
- RingBufferInClosedState : 5 (Threashhold is counted on evry 5 consecutive calls)
- waitDurationInOpenState : 10 seconds (Time after which #failed calls and #success calls will be set to 0)
```
CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                                                                        .failureRateThreshold(20)
                                                                        .ringBufferSizeInClosedState(5)
                                                                        .waitDurationInOpenState(
                                                                                Duration.ofSeconds(10)).build();
```

### Default _TimeLimiterConfig_ Setting
- Duration : 500ms (If the response from backend is > 500ms, it will throw TimeoutEception.class), which will be counted towards failed calls)

### Behind the scenes
- There are two kind of exception that you will see as you continue to hit
  - InterruptedException: Every 3 seconds, forcing the call to sleep for 3 seconds. You will see that TimeoutException is thrown and counted
  - CircuitBreakerOpenException - When the threshold is above 20%, you will see CircuitBreakerOpenException.class exception.
  
 ### Happy Path Response
 ```
 {
    "status": "APPROVED",
    "transaction_id": "ID-6"
}
 ```
 
### Backend or process timed out and response took > 500 ms throws TimeoutException in logs
```
{
    "timestamp": "2019-05-20T02:04:40.505+0000",
    "status": 500,
    "error": "Internal Server Error",
    "message": "No message available",
    "path": "/push-to-card/v2/payment"
}
```

### Circuit Breaker is open
```
{
    "timestamp": "2019-05-20T02:06:21.577+0000",
    "status": 500,
    "error": "Internal Server Error",
    "message": "CircuitBreaker 'test' is open",
    "path": "/push-to-card/v2/payment"
}
```

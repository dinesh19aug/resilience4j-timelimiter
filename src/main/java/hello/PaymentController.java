package hello;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController
{
    @Autowired
    Resilience4jWrapper r4j;
    //static int count =0;
    private static final String template = "ID-%s";
    private static final AtomicLong counter = new AtomicLong();

    @RequestMapping("/push-to-card/v2/payment/timelimit")
    public PaymentVO greeting()

    {
        printCount();
        PaymentVO paymentVO = r4j.run().get();
        //r4j.printLogs();
        return paymentVO;
    }

    public static PaymentVO getPayment()
    {
        int seconds = (LocalTime.now()).getSecond();
        try
        {
            if (seconds%3 ==0)
            {
                Thread.sleep(1000);
            }
        }
        catch (InterruptedException e)
        {
            System.out.println("*** Throwing InterruptedException ***");
        }
        return new PaymentVO("APPROVED",
                             String.format(template, getId()));
    }

    private static long getId()
    {
        return counter.incrementAndGet();
    }

    private void printCount()
    {
        System.out.println("**********************************************");
        System.out.println("CALLED : " + counter);
        System.out.println("**********************************************");
    }
}

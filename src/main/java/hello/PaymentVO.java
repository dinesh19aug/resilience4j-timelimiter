package hello;

public class PaymentVO
{
    private final String status;
    private final String transaction_id;


    public PaymentVO(String status,
                     String transaction_id)
    {
        this.status = status;
        this.transaction_id = transaction_id;

    }

    public String getStatus()
    {
        return status;
    }

    public String getTransaction_id()
    {
        return transaction_id;
    }
}

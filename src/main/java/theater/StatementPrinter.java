package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    public Invoice invoice;
    public Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        StringBuilder result = new StringBuilder("Statement for " + invoice.getCustomer() + System.lineSeparator());
        NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);

        // loop for building result string
        for (Performance p: invoice.getPerformances()) {
            int rslt = getThisAmount(p);
            result.append(String.format("  %s: %s (%s seats)%n", getPlay(p).name, frmt.format(rslt / 100), p.audience));
        }

        // loop for calculating total amount
        int totalAmount = 0;
        for (Performance p: invoice.getPerformances()) {
            totalAmount += getThisAmount(p);
        }

        // loop for calculating volume credits
        int volumeCredits = 0;
        for (Performance p: invoice.getPerformances()) {
            volumeCredits += getVolumeCredits(p);
        }

        result.append(String.format("Amount owed is %s%n", frmt.format(totalAmount / 100)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    private int getVolumeCredits(Performance performance) {
        int result = 0;
        result += Math.max(performance.audience - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        // add extra credit for every five comedy attendees
        if ("comedy".equals(getPlay(performance).type)) result += performance.audience / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        return result;
    }

    private Play getPlay(Performance p) {
        return plays.get(p.playID);
    }

    private  int getThisAmount(Performance p) {
        int thisAmount;
        switch (getPlay(p).type) {
            case "tragedy":
                thisAmount = 40000;
                if (p.audience > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += 1000 * (p.audience - 30);
                }
                break;
            case "comedy":
                thisAmount = Constants.COMEDY_BASE_AMOUNT;
                if (p.audience > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (p.audience - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                thisAmount += Constants.COMEDY_AMOUNT_PER_AUDIENCE * p.audience;
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", getPlay(p).type));
        }
        return thisAmount;
    }
}

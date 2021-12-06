package logging;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.Date;

public class CustomFormatter extends Formatter {
    private final Date date = new Date();

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    private static final String separator = ": ";

    public synchronized String format(LogRecord record) {
        date.setTime(record.getMillis());
        return dateFormatter.format(date) + separator + record.getMessage() + "\n";
    }
}

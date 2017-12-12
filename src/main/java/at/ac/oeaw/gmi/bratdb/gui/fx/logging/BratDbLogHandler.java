package at.ac.oeaw.gmi.bratdb.gui.fx.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

//public class MyLogger{
//	private final Log log;
//    private String context;
//
//    public MyLogger(Log log, String context) {
//        this.log = log;
//        this.context = context;
//    }
//
//    public void setContext(String context) {
//    	this.context = context;
//    }
//
//    public void log(MyLogRecord record) {
//        log.offer(record);
//    }
//
//    public void debug(String msg) {
//        log(new MyLogRecord(MyLevel.DEBUG, context, msg));
//    }
//
//    public void info(String msg) {
//        log(new MyLogRecord(MyLevel.INFO, context, msg));
//    }
//
//    public void warn(String msg) {
//        log(new MyLogRecord(MyLevel.WARN, context, msg));
//    }
//
//    public void error(String msg) {
//        log(new MyLogRecord(MyLevel.ERROR, context, msg));
//    }
//
//    Log getLog() {
//        return log;
//    }
//}

public class BratDbLogHandler extends Handler {
    private Log logQueue;

    public BratDbLogHandler(Log logQueue) {
        LogManager manager = LogManager.getLogManager();
        String className = this.getClass().getName();
        String level = manager.getProperty(className + ".level");
        setLevel(level != null ? Level.parse(level) : Level.INFO);

        this.logQueue = logQueue;
    }

    public Log getLogQueue(){
        return logQueue;
    }

    @Override
    public void setLevel(Level level){
        super.setLevel(level);
    }

    @Override
    public void publish(final LogRecord logRecord) {
        logRecord.getSourceClassName();
        logRecord.getSourceMethodName();
        if (isLoggable(logRecord))
            logQueue.offer(logRecord);
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}
}

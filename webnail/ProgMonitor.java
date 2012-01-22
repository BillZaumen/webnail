package webnail;

public interface ProgMonitor {
    void startProgress(final int nfiles);
    void incrProgressCount();
    void stopProgress();
}
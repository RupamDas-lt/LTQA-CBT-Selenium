package utility;

import java.util.concurrent.locks.ReentrantLock;

public class FileLockUtility {
    public static final ReentrantLock fileLock = new ReentrantLock();
}

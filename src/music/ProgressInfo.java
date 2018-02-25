package music;

class ProgressInfo {
    long currentProgressValue;
    long completeFileSize;
    long downloadedFileSize;
    long totalFilesCount;
    long downloadedFilesCount;

    ProgressInfo(long totalFilesCount,long downloadedFilesCount,long completeFileSize, long downloadedFileSize, long currentProgressValue) {
        this.currentProgressValue = currentProgressValue;
        this.completeFileSize = completeFileSize;
        this.downloadedFileSize = downloadedFileSize;
        this.totalFilesCount = totalFilesCount;
        this.downloadedFilesCount = downloadedFilesCount;
    }
}

package music;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import java.io.BufferedOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class DownloadManager {
    private Consumer<String> console;
    private Consumer<ProgressInfo> onProgress;
    private Action onComplete;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<String> links = new ArrayList<>();
    private boolean started = false;
    private int downloadedFilesCount;
    private int totalFilesCount;

    DownloadManager(Consumer<String> console, Consumer<ProgressInfo> onProgress, Action onComplete) {
        this.onProgress = onProgress;
        this.onComplete = onComplete;
        this.console = console;
    }

    @SuppressWarnings("unused")
    void add(String link) {
        this.links.add(link);
    }

    void addAll(List<String> links) {
        this.links.addAll(links);
    }

    @SuppressWarnings("unused")
    void reset() {
        started = false;
        links.clear();
        compositeDisposable.dispose();
        compositeDisposable = new CompositeDisposable();
    }

    void start() {
        if (started) {
            return;
        } else {
            started = true;
        }
        totalFilesCount = links.size();
        download(links.get(0));
        links.remove(0);
    }

    private void download(String link, Consumer<ProgressInfo> onProgress, Consumer<Throwable> onError, Action onComplete) {
        Observable<ProgressInfo> observable = Observable.create(emitter -> {
            downloadedFilesCount++;
            URL url = new URL(link);
            HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
            long completeFileSize = httpConnection.getContentLength();
            java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
            String name = link.substring(link.lastIndexOf("/") + 1);
            console.accept("downloading " + name);
            java.io.FileOutputStream fos = new java.io.FileOutputStream("storage/songs/" + name);
            BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
            byte[] data = new byte[1024];
            long downloadedFileSize = 0;
            int x;
            while ((x = in.read(data, 0, 1024)) >= 0) {
                downloadedFileSize += x;
                final int currentProgress = (int) ((((double) downloadedFileSize) / ((double) completeFileSize)) * 100d);
                emitter.onNext(new ProgressInfo(totalFilesCount, downloadedFilesCount, completeFileSize, downloadedFileSize, currentProgress));
                bout.write(data, 0, x);
            }
            bout.close();
            in.close();
            emitter.onComplete();
        });
        Disposable subscribe = observable.subscribeOn(Schedulers.newThread()).subscribe(
                onProgress,
                onError,
                onComplete
        );
        compositeDisposable.add(subscribe);
    }

    private int retries = 5;
    private int retriesCount = 0;

    private void download(String link) {
        download(link,
                integer -> onProgress.accept(integer),
                throwable -> {
                    if (retriesCount == retries) {
                        return;
                    }
                    retriesCount++;
                    download(link);
                    throwable.printStackTrace();
                },
                () -> {
                    retriesCount = 0;
                    if (links.size() == 0) {
                        onComplete.run();
                        started = false;
                    } else {
                        download(links.get(0));
                        links.remove(0);
                    }
                });
    }

}

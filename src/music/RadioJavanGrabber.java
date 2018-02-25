package music;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

class RadioJavanGrabber {
    private Consumer<String> console;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String playlist;

    RadioJavanGrabber(String playlist, Consumer<String> console) {
        this.console = console;
        this.playlist = playlist;
    }

    void getDownloadLinks(Consumer<List<String>> onSongs) {
        List<String> links = new ArrayList<>();
        getPlaylistSongsLinks(songs -> {
            for (String url : songs) {
                getSongDownloadLink(url, song -> {
                    links.add(song);
                    if (songs.size() == links.size()) {
                        onSongs.accept(links);
                    }
                });
            }
        });
    }

    private void getSongDownloadLink(String url, Consumer<String> onSong) {
        Observable<String> observable = Observable.create(emitter -> {
            Document doc = Jsoup.parse(HttpHelper.getInstance().fetch(url));
            String song = doc.select("a[href=javascript:void(0)]").first().attr("link");
            String name = song.substring(song.lastIndexOf("/") + 1);
            console.accept("added " + name);
            emitter.onNext(song);
            emitter.onComplete();
        });
        Disposable subscribe = observable.subscribeOn(Schedulers.newThread()).subscribe(
                onSong,
                Throwable::printStackTrace
        );
        compositeDisposable.add(subscribe);
    }

    private void getPlaylistSongsLinks(Consumer<List<String>> onSongs) {
        Observable<List<String>> observable = Observable.create(emitter -> {
            console.accept("fetching songs.");
            HashSet<String> distinctSongs = new HashSet<>();
            Document doc = Jsoup.parse(HttpHelper.getInstance().fetch("https://radiojavan.com/playlists/playlist/" + playlist));
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String str = link.attr("href");
                if (str.contains("playlist_start") & str.contains("index")) {
                    distinctSongs.add("https://radiojavan.com/" + str);
                }
            }
            console.accept("found " + distinctSongs.size() + " songs.");
            emitter.onNext(new ArrayList<>(distinctSongs));
            emitter.onComplete();
        });
        Disposable subscribe = observable.subscribeOn(Schedulers.newThread()).subscribe(
                onSongs,
                Throwable::printStackTrace
        );
        compositeDisposable.add(subscribe);
    }

}

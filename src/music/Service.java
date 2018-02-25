package music;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.util.*;

class Service {
    private Consumer<String> console;
    private RadioJavanGrabber radioJavanGrabber;
    private DownloadManager downloadManager;

    Service(String playlist, Consumer<String> console, Consumer<ProgressInfo> onProgress) {
        this.console = console;
        radioJavanGrabber = new RadioJavanGrabber(playlist, console);
        downloadManager = new DownloadManager(console, onProgress, null);
        createDirs();
    }

    void start(Action onDialog) {
        ProxyHelper.enable();
        radioJavanGrabber.getDownloadLinks(songs -> {
            moveOldSongs(getFileNames(songs));
            List<String> filterSongs = filterSongs(songs);
            Platform.runLater(() -> {
                if (filterSongs.size() == 0) {
                    try {
                        console.accept("up to date!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                try {
                    onDialog.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {

                       Platform.runLater(() -> {
                           Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                           alert.setTitle("Download");
                           alert.setHeaderText("Download " + filterSongs.size() + " new songs");

                           StringBuilder content = new StringBuilder();
                           for (String link:filterSongs){
                               String name = link.substring(link.lastIndexOf("/") + 1);
                               name = name.substring(0,name.indexOf(".mp3"));
                               content.append(name).append("  ");
                           }

                           alert.setContentText(content.toString());
                           Optional<ButtonType> result = alert.showAndWait();
                           if (result.isPresent()) {
                               if (result.get() == ButtonType.OK) {
                                   ProxyHelper.disable();
                                   downloadManager.addAll(filterSongs);
                                   downloadManager.start();
                               }
                           }
                       });

                    }
                }, 500);


            });
        });
    }

    private void createDirs() {
        String[] subDirs = new String[]{"/storage", "/storage/songs", "/storage/old"};
        for (String subDir : subDirs) {
            File dir = new File(System.getProperty("user.dir") + subDir);
            if (!dir.exists()) {
                if (dir.mkdir()) {
                    System.out.println("created " + dir + " dir.");
                }
            }
        }
    }

    private List<String> getFiles(String dir) {
        File folder = new File(System.getProperty("user.dir") + "/storage/" + dir);
        List<String> files = new ArrayList<>();
        for (final File file : Objects.requireNonNull(folder.listFiles())) {
            files.add(file.getName());
        }
        return files;
    }

    private void moveOldSongs(List<String> currentSongs) {
        List<String> songs = getFiles("songs");
        for (String song : songs) {
            if (fileNotExists(currentSongs, song)) {
                File file = new File(System.getProperty("user.dir") + "/storage/songs/" + song);
                File newFile = new File(System.getProperty("user.dir") + "/storage/songs/" + song);
                if (file.renameTo(newFile)) {
                    System.out.println("file moved " + song);
                }
            }
        }
    }

    private List<String> filterSongs(List<String> currentSongs) {
        List<String> oldSongs = getFiles("old");
        List<String> songs = getFiles("songs");
        List<String> filteredSongs = new ArrayList<>();
        for (String currentSong : currentSongs) {
            String name = currentSong.substring(currentSong.lastIndexOf("/") + 1);
            if (fileNotExists(songs, name) & fileNotExists(oldSongs, name) & currentSong.contains(".mp3")) {
                filteredSongs.add(currentSong);
            }
        }
        return filteredSongs;
    }

    private List<String> getFileNames(List<String> links) {
        List<String> fileNames = new ArrayList<>();
        for (String link : links) {
            String name = link.substring(link.lastIndexOf("/") + 1);
            fileNames.add(name);
        }
        return fileNames;
    }

    private boolean fileNotExists(List<String> songs, String currentSong) {
        for (String song : songs) {
            if (song.contains(currentSong)) {
                return false;
            }
        }
        return true;
    }


}

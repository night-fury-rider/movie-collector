/***
 *
 */
package com.yuvrajpatil.collections.movies;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.yuvrajpatil.movies.beans.Movie;

import java.io.FileReader;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

/**
 * @author Yuvraj.Patil TODO: change 'folder' to 'directory' TODO: Add a file
 * logger instead of System.log TODO: Sort movies in a series as per year
 */
/**
 * @author Yuvraj.Patil
 *
 */
class MovieCollectorClient {

    final static String ROOT_DIRECTORY;
    final static String DIRECTORY_SEPARATOR;
    final static String LANG_DIRECTORY;
    final static String EXPORT_DIRECTORIES[];

    static {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;

        try {

            Object obj = parser.parse(new FileReader(
                    "config.json"));

            jsonObject = (JSONObject) obj;

        } catch (Exception e) {
            System.out.println(e);
        }

        ROOT_DIRECTORY = (String) jsonObject.get("ROOT_DIRECTORY");
        //  DIRECTORY_SEPARATOR = (String) jsonObject.get("DIRECTORY_SEPARATOR");
        DIRECTORY_SEPARATOR = System.getProperties().get("file.separator").toString();
        LANG_DIRECTORY = (String) jsonObject.get("LANG_DIRECTORY");

        JSONArray exportDirectories = (JSONArray) jsonObject.get("EXPORT_DIRECTORIES");

        EXPORT_DIRECTORIES = new String[exportDirectories.size()];

        for (int i = 0; i < exportDirectories.size(); i++) {
            EXPORT_DIRECTORIES[i] = (String) exportDirectories.get(i);
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            final File folder = new File(ROOT_DIRECTORY + DIRECTORY_SEPARATOR + LANG_DIRECTORY);
            for (final File fileEntry : folder.listFiles()) {
                printDirectories(ROOT_DIRECTORY + DIRECTORY_SEPARATOR + LANG_DIRECTORY,
                        fileEntry.getName());
            }
            log("Movies Collection Analysis Successful");
        } catch (Exception e) {
            log("ERROR: " + e);
        }
    }

    private static void printDirectories(String rootDirectory, String category) {
        final File folder = new File(rootDirectory + DIRECTORY_SEPARATOR + category);

        List<Movie> movieCollection = new ArrayList<Movie>();
        List<Movie> tmpSeries = new ArrayList<Movie>();

        for (final File fileEntry : folder.listFiles()) {
            try {
                tmpSeries = getSeries(fileEntry);
            } catch (Exception e) {
                log("ERROR in File name" + e + fileEntry.getName());
            }
            movieCollection.addAll(tmpSeries);
        }

        for (String exportDirectory : EXPORT_DIRECTORIES) {
            exportCollection(movieCollection, exportDirectory, category);
        }

    }

    private static List<Movie> getSeries(File folder) {
        //  Series movieSeries = new Series();

        List<Movie> movies = new ArrayList<Movie>();

        Movie tmpMovie = new Movie();
        String folderName = folder.getName();

        if (!isSeries(folderName)) {		// If it is a movie ( Non-series )
            boolean isIMDBPagePresent = false;
            for (final File fileEntry : folder.listFiles()) {
                String fileName = fileEntry.getName();
                if (fileName.contains("IMDb.rar") || fileName.contains("IMDb.zip")) {
                    isIMDBPagePresent = true;
                    continue;
                }
                if (isValidFile(fileName)) {
                    tmpMovie = getRefactoredMovie(new Movie(folderName, fileEntry.getName()));
                    tmpMovie.setSeriesName("");
                    movies.add(tmpMovie);
                }
            }

            if (!isIMDBPagePresent) {
                warn("IMDB information not present in directory : " + folderName);
            }
            return movies;
        } else {						// If it is a series

            for (final File seriesFolder : folder.listFiles()) {

                if (!seriesFolder.isDirectory()) {
                    if (seriesFolder.getName().equals("Series.rar") || seriesFolder.getName().equals("Thumbs.db")) {
                        continue;
                    }
                    logOut("Naming standards are not followed by series : " + seriesFolder.getName()
                            + "\n\t\t Only movie folders and \"Series.rar\" is allowed inside a movie series folder");
                }
                boolean isIMDBPagePresent = false;
                for (final File movieFolder : seriesFolder.listFiles()) {
                    // Movies of a series

                    String fileName = movieFolder.getName();
                    if (fileName.contains("IMDb.rar") || fileName.contains("IMDb.zip")) {
                        isIMDBPagePresent = true;
                        continue;
                    }
                    if (isValidFile(movieFolder.getName())) {
                        tmpMovie = getRefactoredMovie(new Movie(seriesFolder.getName(), movieFolder.getName()));
                        tmpMovie.setSeriesName(folderName);
                        movies.add(tmpMovie);
                    }
                }
                if (!isIMDBPagePresent) {
                    warn("IMDB information not present in directory : " + seriesFolder);
                }
            }
        }

        return movies;

    }

    private static void exportCollection(List<Movie> collection, String exportDirectory, String category) {
        try {
            File directory = new File(exportDirectory + LANG_DIRECTORY);

            directory.mkdir();  //  create a new directory, will do nothing if directory exists

            File file = new File(directory, category + ".json");

            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter buffWriter = new BufferedWriter(fileWriter);
            buffWriter.write(collection.toString());
            buffWriter.close();
        } catch (Exception e) {
            log("ERROR in exporting data : " + e);
        }
    }

    private static Movie getRefactoredMovie(Movie movie) {
        String movieName = movie.getName();
        int startIndex = movieName.indexOf("(");
        int endIndex = movieName.indexOf(")");

        if (startIndex == -1 || endIndex == -1 || startIndex == 0 || endIndex == 0) {
            log("Naming standards are not followed by: " + movieName);
            logOut("Year of release is not specified as per standards");

        }
        String fileName = movie.getFileName().toLowerCase();

        if (fileName.contains("hindi")) {
            movie.setHindi(true);
        }
        assignPrintQuality(movie, movie.getFileName());

        try {
            movie.setName(movieName.substring(0, startIndex - 1));
            movie.setYear(Integer.parseInt(movieName.substring(startIndex + 1,
                    endIndex)));
        } catch (IndexOutOfBoundsException exception) {
            log(exception);
        } catch (NumberFormatException e) {
            log(e);
        }
        return movie;
    }

    /**
     * @description Function to assign print quality from File Name
     * @param movie	Movie object
     * @param fileName	File name
     */
    private static void assignPrintQuality(Movie movie, String fileName) {

        if (fileName.contains("1080p")) {
            movie.setPrintQuality("1080p");
        } else if (fileName.contains("720p")) {
            movie.setPrintQuality("720p");
        } else if (fileName.contains("TC")) {
            movie.setPrintQuality("TC");
        } else if (fileName.contains("480p")) {
            movie.setPrintQuality("480p");
        } else if (fileName.contains("DVD") || fileName.contains("dvd")) {
            movie.setPrintQuality("DVD");
        } else {
            movie.setPrintQuality("");
        }
    }

    private static boolean isSeries(String fileName) {
        if (fileName.indexOf("(") == -1 && fileName.indexOf("(") == -1) {
            return true;
        }
        return false;
    }

    private static boolean isValidFile(String fileName) {
        if (fileName.contains(".srt")
                || fileName.contains(".rar")
                || fileName.contains(".db")
                || fileName.contains(".txt")
                || fileName.contains(".jpg")
                || fileName.contains(".png")) {
            return false;
        }
        return true;
    }

    private static void logOut(Object msg) {
        System.out.println("\nFATAL ERROR: " + msg);
        System.exit(0);
    }

    private static void warn(Object msg) {
        System.out.println("WARNING: " + msg);
    }

    private static void log(Object msg) {
        System.out.println("INFO: " + msg);
    }
}

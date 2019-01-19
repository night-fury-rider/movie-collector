/***
 *
 */
package com.yuvrajpatil.collections.movies;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import com.yuvrajpatil.movies.beans.Movie;

import java.io.FileReader;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

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
    static boolean isExportFailed;

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
        
        JFrame frame = new JFrame("Movie Collector");
        frame.setSize(500, 200);
        frame.setLocation(300, 200);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        JLabel sourceLabel = new JLabel("Source");
        sourceLabel.setBounds(25, 31, 100, 14);
        frame.getContentPane().add(sourceLabel);
        
        final JTextField sourcePathValue = new JTextField(ROOT_DIRECTORY + DIRECTORY_SEPARATOR + LANG_DIRECTORY);
        sourcePathValue.setBounds(128, 28, 300, 20);
        frame.getContentPane().add(sourcePathValue);
        sourcePathValue.setColumns(10);
        
        JLabel destinationLabel = new JLabel("Destination");
        destinationLabel.setBounds(25, 80, 100, 14);
        frame.getContentPane().add(destinationLabel);
        
        final JTextField destinationPathValue = new JTextField(EXPORT_DIRECTORIES[0]);
        destinationPathValue.setBounds(128, 80, 300, 20);
        frame.getContentPane().add(destinationPathValue);
        destinationPathValue.setColumns(10);
        
        final JTextArea textArea = new JTextArea(10, 40);
        textArea.setEditable(false);
        frame.getContentPane().add(BorderLayout.CENTER, textArea);
        
        final JButton button = new JButton("Collect Movies Info");
        frame.getContentPane().add(BorderLayout.SOUTH, button);
        
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                isExportFailed = false;
                try {
                    final File folder = new File(sourcePathValue.getText());
                    for (final File fileEntry : folder.listFiles()) {
                        if(isExportFailed) {
                            break;
                        }
                        printDirectories(ROOT_DIRECTORY + DIRECTORY_SEPARATOR + LANG_DIRECTORY,
                                fileEntry.getName(), destinationPathValue.getText());
                    }
                    if(!isExportFailed) {
                        log("Movies Collection Analysis Successful");
                        JOptionPane.showMessageDialog(null, "Movies Collection Analysis Successful");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Movies Collection Failure.\nPlease make sure Source Device is connected.\nPlease make sure Paths are correct.", null, JOptionPane.ERROR_MESSAGE);
                    error(e);
                }
            }
        });
        frame.setVisible(true);
    }

    private static void printDirectories(String rootDirectory, String category, String exportDirectory) {
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

        exportCollection(movieCollection, exportDirectory, category);

    }

    private static List<Movie> getSeries(File folder) {

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
            error("ERROR in exporting data : " + e);
            isExportFailed = true;
        }
    }

    private static Movie getRefactoredMovie(Movie movie) {
        String movieName = movie.getName();
        int startIndex = movieName.indexOf("(");
        int endIndex = movieName.indexOf(")");

        if (startIndex == -1 || endIndex == -1 || startIndex == 0 || endIndex == 0) {
            logOut("Naming standards are not followed by: " + movieName);
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
            logOut(exception);
        } catch (NumberFormatException e) {
            logOut("ERROR in Number parsing" + e + "\n" + "Source: " + fileName);
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
        fileName = fileName.toLowerCase();
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
        JOptionPane.showMessageDialog(null, "\nFATAL ERROR: " + msg, null, JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }

    private static void warn(Object msg) {
        System.out.println("WARNING: " + msg);
        JOptionPane.showMessageDialog(null, "\nWARNING ERROR: " + msg, null, JOptionPane.WARNING_MESSAGE);
    }

    private static void log(Object msg) {
        System.out.println("INFO: " + msg);
    }
    private static void error(Object msg) {
        System.out.println("ERROR: " + msg);
        JOptionPane.showMessageDialog(null, "ERROR: " + msg, null, JOptionPane.ERROR_MESSAGE);
    }
}

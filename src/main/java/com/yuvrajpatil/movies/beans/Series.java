/***
 * 
 */
package com.yuvrajpatil.movies.beans;

import java.util.ArrayList;
import java.util.List;

/***
 * @author Yuvraj.Patil
 *
 */
public class Series {
	String name;
	List <Movie> movies = new ArrayList<Movie>();
	
	 
	public Series() {
		super();
	}
	
	/**
	 * @param name
	 * @param movies
	 */
	public Series(String name, List<Movie> movies) {
		super();
		this.name = name;
		this.movies = movies;
	}
	/***
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/***
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/***
	 * @return the series
	 */
	public List<Movie> getSeries() {
		return movies;
	}
	/***
	 * @param movies the series to set
	 */
	public void setSeries(List<Movie> movies) {
		this.movies = movies;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\"Series\": { \"name\": \"" + name + "\", \"movies\": " + movies + " }}" ;
	}
	
	public boolean addMovie(Movie movie){
		return this.movies.add(movie);
	}
}

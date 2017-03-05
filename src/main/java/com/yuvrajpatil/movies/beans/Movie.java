/***
 * 
 */
package com.yuvrajpatil.movies.beans;

/***
 * @author Yuvraj.Patil
 *
 */
public class Movie {
	
	String name;
	Integer year;
	String fileName;
	String seriesName;
	String printQuality;
	boolean isHindi;
	/**
	 * 
	 */
	public Movie() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 * @param fileName
	 */
	public Movie(String name, String fileName) {
		super();
		this.name = name;
		this.fileName = fileName;
	}
	
	/**
	 * @param name
	 * @param year
	 */
	public Movie(String name, Integer year) {
		super();
		this.name = name;
		this.year = year;
	}


	/**
	 * @param name
	 * @param year
	 * @param fileName
	 */
	public Movie(String name, Integer year, String fileName) {
		super();
		this.name = name;
		this.year = year;
		this.fileName = fileName;
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
	 * @return the year
	 */
	public Integer getYear() {
		return year;
	}
	/***
	 * @param year the year to set
	 */
	public void setYear(Integer year) {
		this.year = year;
	}
	/***
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	/***
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	

	public String getSeriesName() {
		return seriesName;
	}

	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}
	

	public String getPrintQuality() {
		return printQuality;
	}

	public void setPrintQuality(String printQuality) {
		this.printQuality = printQuality;
	}
	

	public boolean isHindi() {
		return isHindi;
	}

	public void setHindi(boolean isHindi) {
		this.isHindi = isHindi;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\"name\": \"" + name +  
						"\", \"year\": " + year + 
						", \"printQuality\": \"" + printQuality +
						"\", \"fileName\": \"" + fileName + 
						"\",\"seriesName\": \"" + seriesName + 
						"\",\"isHindi\": " + isHindi +
				"}";
	}	
	
	
}
